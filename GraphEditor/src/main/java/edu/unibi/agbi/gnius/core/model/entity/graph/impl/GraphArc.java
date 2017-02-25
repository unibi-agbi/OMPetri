/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph.impl;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gravisfx.graph.entity.impl.GravisArc;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;

/**
 *
 * @author PR
 */
@Deprecated
public class GraphArc extends GravisArc implements IGraphArc
{
    private DataArc dataArc;
    
    private String activeStyleClass;
    
    private static final String PSEUDO_CLASS_SELECTED_IDENT = "selected";
    private static final String PSEUDO_CLASS_HIGHLIGHT_IDENT = "highlighted";
    
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass(PSEUDO_CLASS_SELECTED_IDENT);
    private static final PseudoClass PSEUDO_CLASS_HIGHLIGHT = PseudoClass.getPseudoClass(PSEUDO_CLASS_HIGHLIGHT_IDENT);
    
    private final BooleanProperty isSelected = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_SELECTED , get());
        }
        @Override
        public Object getBean() {
            return GraphArc.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_SELECTED_IDENT;
        }
    };
    
    private final BooleanProperty isHighlighted = new BooleanPropertyBase(false) {
        @Override 
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_HIGHLIGHT , get());
        }
        @Override
        public Object getBean() {
            return GraphArc.this;
        }
        @Override
        public String getName() {
            return PSEUDO_CLASS_HIGHLIGHT_IDENT;
        }
    };
    
    public GraphArc(IGraphNode source, IGraphNode target) {
        super(source , target);
    }

    public GraphArc(IGraphNode source , IGraphNode target , IDataArc dataArc) throws AssignmentDeniedException {
        this(source , target);
        if (!(dataArc instanceof DataArc)) {
            throw new AssignmentDeniedException("Must assign DataArc to GraphArc! Action denied.");
        }
        this.dataArc = (DataArc) dataArc;
    }

    @Override
    public void setRelatedElement(IDataArc dataArc) throws AssignmentDeniedException {
        if (this.dataArc != null) {
            throw new AssignmentDeniedException("Related data object has already been assigned! Action denied.");
        } else if (!(dataArc instanceof DataArc)) {
            throw new AssignmentDeniedException("Must assign DataArc to GraphArc! Action denied.");
        }
        this.dataArc = (DataArc) dataArc;
    }
    
    @Override
    public DataArc getRelatedDataArc() {
        return dataArc;
    }
    
    @Override
    public IDataElement getRelatedDataElement() {
        return getRelatedDataArc();
    }

    @Override
    public String getActiveStyleClass() {
        return activeStyleClass;
    }

    @Override
    public void setActiveStyleClass(String name) {
        getShape().getStyleClass().remove(activeStyleClass);
        activeStyleClass = name;
        getShape().getStyleClass().add(name);
    }

    @Override
    public void setSelected(boolean value) {
        isSelected.set(value);
    }
    
    @Override
    public void setHighlighted(boolean value) {
        isHighlighted.set(value);
    }
    
    @Override
    public boolean isSelected() {
        return isSelected.get();
    }
    
    @Override
    public boolean isHighlighted() {
        return isHighlighted.get();
    }
    
    @Override
    public void putOnTop() {
        EdgeLayer edgeLayer = (EdgeLayer) getParent();
        edgeLayer.getChildren().remove(this);
        edgeLayer.getChildren().add(this);
    }

    @Override
    public void setTarget(IGraphNode target) throws AssignmentDeniedException {
        throw new AssignmentDeniedException("Cannot assign new target to curved arc! Change denied.");
    }

    @Override
    public DoubleProperty endXProperty() {
        throw new UnsupportedOperationException("Arcs have no endpoint.");
    }

    @Override
    public DoubleProperty endYProperty() {
        throw new UnsupportedOperationException("Arcs have no endpoint.");
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