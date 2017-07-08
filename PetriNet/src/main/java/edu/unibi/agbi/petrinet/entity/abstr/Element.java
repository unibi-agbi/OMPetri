/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pr
 */
public abstract class Element implements IElement
{
    protected Type type;

    protected String id;
    protected String name;

    protected boolean isDisabled = false;

    protected final Set<String> parameter;

    public Element() {
        parameter = new TreeSet();
    }

    @Override
    public Type getElementType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<String> getRelatedParameterIds() {
        return parameter;
    }

    @Override
    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    @Override
    public boolean isDisabled() {
        return isDisabled;
    }
    
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    public enum Type
    {
        ARC, CLUSTER, CLUSTERARC, PLACE, TRANSITION;
    }
}
