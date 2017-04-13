/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.connection;

import edu.unibi.agbi.gravisfx.entity.child.GravisArrow;
import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.util.ElementHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisEdgeArrow extends Path implements IGravisConnection
{
    private final List<ElementHandle> elementHandles = new ArrayList();
    
    private final GravisArrow arrow;

    private final IGravisNode source;
    private final IGravisNode target;
    
    public GravisEdgeArrow(IGravisNode source , IGravisNode target) {

        super();
        
        arrow = new GravisArrow(this);
        
        elementHandles.add(new ElementHandle(this));
        elementHandles.add(arrow.getElementHandles().get(0));

        this.source = source;
        this.target = target;

        MoveTo mv = new MoveTo();
        mv.xProperty().bind(source.translateXProperty().add(source.getOffsetX()));
        mv.yProperty().bind(source.translateYProperty().add(source.getOffsetY()));
        
        LineTo lt = new LineTo();
        
        /**
         * Line's end X coordinate. Changes it's value on any coordinate changes
         * for the source or target.
         */
        DoubleBinding bindingLineEndX = new DoubleBinding()
        {
            {
                super.bind(
                        source.translateXProperty() ,
                        target.translateXProperty() ,
                        source.translateYProperty() ,
                        target.translateYProperty()
                );
            }

            /**
             * Computes the X coordinate. Computes the intersection point of
             * the line with a circle through the target coordinates.
             * @return 
             */
            @Override
            protected double computeValue() {
                
                double x1 = source.translateXProperty().get() + source.getOffsetX();
                double y1 = source.translateYProperty().get() + source.getOffsetY();
                
                double x2 = target.translateXProperty().get() + target.getOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getOffsetY() + 0.0001;
                
                double x = (x2 - x1);
                double y = (y2 - y1);
                
                if (x < 1 && x > -1) {
                    if (x >= 0) {
                        x = 1;
                    } else {
                        x = -1;
                    }
                }
                
                if (y < 1 && y > -1) {
                    if (y >= 0) {
                        y = 1;
                    } else {
                        y = -1;
                    }
                }
                
                double b = y1 - y / x * x1;
                double p = 2 * (y / x * b - y / x * y2 - x2) / (1 + y / x * y / x);
                double q = (x2 * x2 + b * b + y2 * y2 - 2 * b * y2 - GravisProperties.ARROW_TARGET_DISTANCE * GravisProperties.ARROW_TARGET_DISTANCE) / (1 + y / x * y / x);

                if (x2 <= x1) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q);
                } else {
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };
        lt.xProperty().bind(bindingLineEndX);

        /**
         * Line's end Y coordinate. Changes it's value on changes of the related
         * X coordinate.
         */
        DoubleBinding bindingLineEndY = new DoubleBinding()
        {
            {
                super.bind(bindingLineEndX);
            }

            /**
             * Uses the previously computed X value to determine the Y value. 
             * Uses line function.
             * @return 
             */
            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getOffsetX();
                double y1 = source.translateYProperty().get() + source.getOffsetY();

                double x2 = target.translateXProperty().get() + target.getOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getOffsetY() + 0.0001;
                
                double x = (x2 - x1);
                double y = (y2 - y1);
                
                if (x < 1 && x > -1) {
                    if (x >= 0) {
                        x = 1;
                    } else {
                        x = -1;
                    }
                }
                
                if (y < 1 && y > -1) {
                    if (y >= 0) {
                        y = 1;
                    } else {
                        y = -1;
                    }
                }

                return y / x * bindingLineEndX.get() + y1 - y / x * x1;
            }
        };
        lt.yProperty().bind(bindingLineEndY);
        
        DoubleBinding arrowAngle = new DoubleBinding()
        {
            {
                super.bind(bindingLineEndX);
            }
            
            @Override
            protected double computeValue() {
                
                double x1 = source.translateXProperty().get() + source.getOffsetX();
                double y1 = source.translateYProperty().get() + source.getOffsetY();
                
                double x2 = target.translateXProperty().get() + target.getOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getOffsetY() + 0.0001;
                
                double x = (x2 - x1);
                double y = (y2 - y1);
                
                if (x < 1 && x > -1) {
                    if (x >= 0) {
                        x = 1;
                    } else {
                        x = -1;
                    }
                }
                
                if (y < 1 && y > -1) {
                    if (y >= 0) {
                        y = 1;
                    } else {
                        y = -1;
                    }
                }
                
                /**
                 * Winkelverlauf im Uhrzeigersinn:
                 * Oben links: 0 bis 90     +180
                 * Oben rechts: -90 bis 0
                 * Unten rechts: 0 bis 90
                 * Unten links: -90 bis 0   +180
                 */
                
                if (x2 < x1) {
                    return Math.toDegrees(Math.atan(y / x)) + 180;
                } else {
                    return Math.toDegrees(Math.atan(y / x));
                }
            }
        };
        
        getElements().add(mv);
        getElements().add(lt);
        
        arrow.translateXProperty().bind(bindingLineEndX.subtract(GravisProperties.ARROW_WIDTH / 2));
        arrow.translateYProperty().bind(bindingLineEndY.subtract(GravisProperties.ARROW_HEIGHT / 2));
        arrow.rotateProperty().bind(arrowAngle);
    }
    
    @Override
    public Object getBean() {
        return GravisEdgeArrow.this;
    }

    @Override
    public List<ElementHandle> getElementHandles() {
        return elementHandles;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
    
    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        shapes.add(arrow);
        return shapes;
    }

    @Override
    public IGravisNode getSource() {
        return source;
    }

    @Override
    public IGravisNode getTarget() {
        return target;
    }
}
