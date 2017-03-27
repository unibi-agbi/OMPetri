/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.graph.entity.parent.connection.GravisCurveArrow;

/**
 *
 * @author PR
 */
public class GraphCurveArrow extends GravisCurveArrow implements IGraphArc
{
    private final DataArc dataArc;

    public GraphCurveArrow(IGraphNode source , IGraphNode target , DataArc dataArc) {
        super(source , target);
        this.dataArc = dataArc;
    }
    
    @Override
    public DataArc getDataElement() {
        return dataArc;
    }

    @Override
    public IGraphNode getSource() {
        return (IGraphNode) super.getSource();
    }

    @Override
    public IGraphNode getTarget() {
        return (IGraphNode) super.getTarget();
    }
}
