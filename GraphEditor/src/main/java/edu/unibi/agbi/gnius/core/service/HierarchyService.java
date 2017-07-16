/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.editor.graph.HierarchyController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.ZoomController;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.exception.DataException;
import edu.unibi.agbi.gnius.core.model.entity.data.DataType;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.IGravisCluster;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class HierarchyService
{
    @Autowired private Calculator calculator;
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private HierarchyController hierarchyController;
    @Autowired private SelectionService selectionService;
    @Autowired private ZoomController zoomController;

    /**
     * Climbs one level in the graph layer hierarchy.
     */
    public void climb() {
        if (dataService.getGraph().getParentGraph() != null) {
            show(dataService.getGraph().getParentGraph());
        }
    }

    /**
     * Opens a cluster, showing its graph layers.
     *
     * @param cluster
     */
    public void open(IGraphCluster cluster) {
        show(cluster.getGraph());
    }

    /**
     * Shows a graph layer.
     *
     * @param graph
     */
    public void show(Graph graph) {
        dataService.setGraph(graph);
        dataService.UpdateClusterShapes();
        hierarchyController.update();
        selectionService.unselectAll();
        zoomController.CenterNodes();
    }

    /**
     * Groups and hides elements in a new cluster.
     *
     * @param dao
     * @param selected
     * @param clusterId
     * @return
     * @throws DataException
     */
    public synchronized IGraphCluster cluster(DataDao dao, List<IGraphElement> selected, String clusterId) throws DataException {

        if (selected.isEmpty()) {
            messengerService.addWarning("Nothing was selected for clustering!");
            return null;
        }

        Set<GraphCluster> clusters = new HashSet();
        Set<IGraphNode> nodes = new HashSet();

        for (IGraphElement element : selected) {
            switch (element.getType()) {
                case CLUSTER:
//                    nodes.add((IGraphNode) element);
                    clusters.add((GraphCluster) element);
                    break;
                case NODE:
                    nodes.add((IGraphNode) element);
                    break;
                default:
                    messengerService.addWarning("Element '" + element.getId() + "' cannot be selected for clustering! Skipping.");
                    break;
            }
        }
        if (nodes.size() + clusters.size() <= 1) {
            messengerService.printMessage("Invalid selection for clustering!");
            return null;
        }

        IGraphCluster cluster = create(dao, nodes, clusters, clusterId);
        dao.setHasChanges(true);

        return cluster;
    }

    /**
     * Restores and shows all the nodes stored within the given cluster(s).
     *
     * @param selected
     */
    public synchronized void restore(List<IGraphElement> selected) {

        if (selected.isEmpty()) {
            return;
        }

        List<IGraphCluster> clusters = new ArrayList();

        for (IGraphElement element : selected) {
            switch (element.getType()) {
                case CLUSTER:
                    clusters.add((IGraphCluster) element);
                    break;
                default:
                    messengerService.addWarning("Element '" + element.getId() + "' is not a cluster! Skipping.");
            }
        }

        for (IGraphCluster cluster : clusters) {
            try {
                remove(cluster);
            } catch (DataException ex) {
                messengerService.addException("Cannot ungroup cluster!", ex);
            }
        }
    }

    /**
     * Creates a cluster, moving all given nodes and related arcs to a new graph
     * layer.
     *
     * @param nodes
     * @param clusters
     * @return
     * @throws DataException
     */
    private IGraphCluster create(DataDao dao, Set<IGraphNode> nodes, Set<GraphCluster> clusters, String clusterId) throws DataException {

        DataCluster dataCluster;
        GraphCluster cluster;
        IGraphArc arc;
        List<IGravisConnection> nodeConnections;
        List<IGraphArc> clusterArcs, arcs;

        Graph graphChild;
        Graph graph;

        nodes.addAll(clusters);

        /**
         * Create cluster data and shape.
         */
        dataCluster = new DataCluster(clusterId);
        cluster = new GraphCluster(dataCluster.getId(), dataCluster);
        clusterArcs = new ArrayList();
        arcs = new ArrayList();

        /**
         * Reassign graph hierarchy.
         */
        graph = dao.getGraph();
        graphChild = dataCluster.getGraph();
        graphChild.nameProperty().set(dataCluster.getId());
        graphChild.setParentGraph(graph);
        clusters.forEach(c -> c.getGraph().setParentGraph(graphChild));

        /**
         * Add and style the cluster node.
         */
        graph.add(cluster);
        dataService.styleElement(cluster);

        Point2D pos = calculator.getCenterN(nodes);
        if (dataService.isGridEnabled()) {
            pos = calculator.getPositionInGrid(pos, graph);
        }
        cluster.setTranslateX(pos.getX() - cluster.getCenterOffsetX());
        cluster.setTranslateY(pos.getY() - cluster.getCenterOffsetY());

        /**
         * Move nodes and determine wether node is connected to node in or
         * outside of the cluster. All arcs connecting to nodes outside the
         * cluster will be stored in cluster arcs.
         */
        for (IGraphNode node : nodes) {

            nodeConnections = new ArrayList();
            nodeConnections.addAll(node.getConnections()); // copy, references are lost after removing from graph

            graph.remove(node);
            graphChild.add(node);

            for (IGravisConnection connection : nodeConnections) {
                arc = (IGraphArc) connection;

                if (!nodes.contains(arc.getTarget())) { // target OUTSIDE
                    getClusterArc(clusterArcs, cluster, arc.getTarget(), arc);
                } else if (!nodes.contains(arc.getSource())) { // source OUTSIDE
                    getClusterArc(clusterArcs, arc.getSource(), cluster, arc);
                } else {
                    arcs.add(arc); // connection INSIDE
                }
            }
        }

        for (IGraphArc conn : arcs) {
            graphChild.add(conn);
        }

        for (IGraphArc conn : clusterArcs) {
            graph.add(conn);
            dataService.styleElement(conn);
        }

        return cluster;
    }

    /**
     * Removes the given cluster and un-groups all elements stored inside.
     *
     * @param cluster
     * @return
     */
    private void remove(IGraphCluster cluster) throws DataException {

        Graph graphChild = cluster.getGraph();
        Graph graph = graphChild.getParentGraph();

        DataClusterArc dca;
        IGraphNode source, target;
        List<IGravisConnection> clusterConnections;
        Set<IGraphArc> arcs;
        IGraphArc arc;

        /**
         * Calculate translation offset.
         */
        Point2D pos = calculator.getCenter(graphChild.getNodes());
        if (dataService.isGridEnabled()) {
            pos = calculator.getPositionInGrid(pos, dataService.getGraph());
        }
        double translateX = cluster.getShape().getTranslateX() + cluster.getCenterOffsetX() - pos.getX();
        double translateY = cluster.getShape().getTranslateY() + cluster.getCenterOffsetY() - pos.getY();

        /**
         * Add all nodes and connections from the child graph (cluster) to the
         * parent graph.
         */
        for (IGravisNode n : graphChild.getNodes()) {
            graph.add(n);
            n.translateXProperty().set(n.translateXProperty().get() + translateX);
            n.translateYProperty().set(n.translateYProperty().get() + translateY);
        }
        for (IGravisConnection c : graphChild.getConnections()) {
            graph.add(c);
        }
        graphChild.clear();

        /**
         * Create copy of cluster arcs.
         */
        arcs = new HashSet();
        clusterConnections = new ArrayList();
        clusterConnections.addAll(cluster.getConnections());

        /**
         * Remove cluster and reassign graph hierarchy.
         */
        graph.remove(cluster);
        graph.getChildGraphs().remove(graphChild);
        while (!graphChild.getChildGraphs().isEmpty()) {
            graphChild.getChildGraphs().get(0).setParentGraph(graph);
        }

        /**
         * Restore stored arcs or create cluster arcs.
         */
        for (IGravisConnection clusterArc : clusterConnections) {

            /**
             * Check if nodes related to a stored arc are present in the graph.
             * If so, add arc to graph. If not, create new cluster arc.
             */
            dca = (DataClusterArc) ((IGraphArc) clusterArc).getData();

            for (IGraphArc storedArc : dca.getStoredArcs().values()) {

                if (graph.contains(storedArc.getSource()) && graph.contains(storedArc.getTarget())) {

                    arcs.add(storedArc);
                    storedArc.getData().getShapes().clear();
                    storedArc.getData().getShapes().add(storedArc);

                } else { // must be a connection to a lower level cluster
                    
                    Set<IGraphArc> clusterArcs = new HashSet();

                    if (clusterArc.getSource().equals(cluster)) { // cluster arc target remains

                        if (graph.contains(storedArc.getSource())) {
                            source = storedArc.getSource();
                        } else {
                            source = getClusterContainingNode(graph, storedArc.getSource());
                            source.getConnections().forEach(conn -> clusterArcs.add((IGraphArc) conn));
                        }
                        target = (IGraphNode) clusterArc.getTarget();

                    } else { // cluster arc source remains

                        if (graph.contains(storedArc.getTarget())) {
                            target = storedArc.getTarget();
                        } else {
                            target = getClusterContainingNode(graph, storedArc.getTarget());
                            target.getConnections().forEach(conn -> clusterArcs.add((IGraphArc) conn));
                        }
                        source = (IGraphNode) clusterArc.getSource();

                    }
                    arc = getClusterArc(clusterArcs, source, target, storedArc);
                    if (arc != null) {
                        graph.add(arc);
                        dataService.styleElement(arc);
                    }
                }
            }
        }

        for (IGraphArc a : arcs) {
            graph.add(a);
//            dataService.styleElement(a);
        }
    }

    /**
     *
     * @param clusterArcs collection of cluster arcs that connect a cluster to
     *                    outside nodes
     * @param source      source of the connection
     * @param target      target of the connection
     * @param arcRelated  arc that will be stored in the cluster arc
     * @throws DataException
     */
    private IGraphArc getClusterArc(Collection<IGraphArc> clusterArcs, IGraphNode source, IGraphNode target, IGraphArc arcRelated) throws DataException {

        IGraphArc shape, shapeNew = null;
        DataClusterArc data, dca;
        Optional<IGraphArc> existingArc;

        if (source.getType() != GravisType.CLUSTER && target.getType() != GravisType.CLUSTER) {
            throw new DataException("Trying to create cluster arc without source or target being a cluster!");
        }

        String idShape = dataService.getConnectionId(source, target);
        String idData = dataService.getArcId(source.getData(), target.getData());

        /**
         * Check if connection already exists.
         */
        existingArc = clusterArcs.stream()
                .filter(a -> a.getSource().equals(source) && a.getTarget().equals(target))
                .findFirst();

        if (existingArc.isPresent()) {

            shape = existingArc.get();
            data = (DataClusterArc) shape.getData();

        } else {

            data = new DataClusterArc(idData);
            shape = new GraphArc(idShape, source, target, data);
            
            shapeNew = shape;
            clusterArcs.add(shapeNew);
        }

        /**
         * Check if the related arc is also a cluster arc. Add all stored arcs
         * to the higher level cluster arc.
         */
        if (arcRelated.getData().getType() == DataType.CLUSTERARC) {
            dca = (DataClusterArc) arcRelated.getData();
            dca.getStoredArcs().values().forEach(storedArc -> {
                data.getStoredArcs().put(storedArc.getId(), storedArc);
                storedArc.getData().getShapes().clear(); // remove reference to currently hidden shape
                storedArc.getData().getShapes().add(shape); // replace by reference to cluster arc shape
            });
//            dca.getStoredArcs().clear();
        } else {
            data.getStoredArcs().put(arcRelated.getId(), arcRelated);
            arcRelated.getData().getShapes().clear(); // remove reference to currently hidden shape
            arcRelated.getData().getShapes().add(shape); // replace by reference to cluster arc shape
        }
        
        return shapeNew;
    }

    private IGraphCluster getClusterContainingNode(Graph graph, IGraphNode node) {
        for (IGravisCluster cluster : graph.getClusters()) {
            if (isClusterContainingNode(cluster, node)) {
                return (IGraphCluster) cluster;
            }
        }
        return null;
    }

    private boolean isClusterContainingNode(IGravisCluster cluster, IGraphNode node) {
        if (cluster.getGraph().contains(node)) {
            return true;
        } else {
            for (IGravisCluster subCluster : cluster.getGraph().getClusters()) {
                if (isClusterContainingNode(subCluster, node)) {
                    return true;
                }
            }
            return false;
        }
    }
}
