/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.impl;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCurveArrow extends Path implements IGravisConnection
{
    private final GravisArrow arrow;
    
    private final IGravisNode source;
    private final IGravisNode target;
    
    public GravisCurveArrow(IGravisNode source, IGravisNode target) {
        
        super();
        
        this.arrow = new GravisArrow();
        
        this.source = source;
        this.target = target;

        MoveTo mv = new MoveTo();
        mv.xProperty().bind(source.translateXProperty().add(source.getOffsetX()));
        mv.yProperty().bind(source.translateYProperty().add(source.getOffsetY()));
        
        QuadCurveTo qct = new QuadCurveTo();
        
        /**
         * Control point X coordinate.
         */
        DoubleBinding bindingCurveControlX = new DoubleBinding()
        {
            {
                super.bind(
                        source.translateXProperty(), 
                        source.translateYProperty(), 
                        target.translateXProperty(), 
                        target.translateYProperty()
                );
            }
            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getOffsetX();
                double y1 = source.translateYProperty().get() + source.getOffsetY();

                double x2 = target.translateXProperty().get() + target.getOffsetX();
                double y2 = target.translateYProperty().get() + target.getOffsetY();
                
                double x3, y3;
                x3 = (x1 + x2) / 2 ;
                y3 = (y1 + y2) / 2 ;
                
                double x = (x1 - x2);
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
                    
                double m2 = x / y;
                
                double b2 = y3 - m2 * x3;
                double r = GravisProperties.ARC_GAP / 2;
                
//                System.out.println("####");
//                System.out.println("P1 ( " + x1 + " | " + y1 + " )");
//                System.out.println("P2 ( " + x2 + " | " + y2 + " )");
//                System.out.println("m2 = " + m2);
//                System.out.println("b2 = " + b2);
                
                double p = 2 * (m2 * b2 - m2 * y3 - x3) / (1 + m2 * m2);
                double q = (x3 * x3 + b2 * b2 + y3 * y3 - 2* b2 * y3 - r * r) / (1 + m2 * m2);
                
//                System.out.println("p = " + p);
//                System.out.println("q = " + q);
                
                if (y2 >= y1) {
                    return - p / 2 + Math.sqrt(p * p / 4 - q);
                } else {
                    return - p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };
        qct.controlXProperty().bind(bindingCurveControlX);
        
        /**
         * Control point Y coordinate.
         */
        DoubleBinding bindingCurveControlY = new DoubleBinding()
        {
            {
                super.bind(bindingCurveControlX);
            }
            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getOffsetX();
                double y1 = source.translateYProperty().get() + source.getOffsetY();

                double x2 = target.translateXProperty().get() + target.getOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getOffsetY() - 0.0001;
                
                double x = (x1 - x2);
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
                
                return x / y * bindingCurveControlX.get() + ((y1 + y2) - x / y * (x1 + x2)) / 2;
            }
        };
        qct.controlYProperty().bind(bindingCurveControlY);
        
        
        /**
         * Line's end X coordinate. Changes it's value on any coordinate changes
         * for the source or target.
         */
        DoubleBinding bindingCurveEndX = new DoubleBinding()
        {
            {
                super.bind(
                        bindingCurveControlY
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
                
                double x2 = target.translateXProperty().get() + target.getOffsetX();
                double y2 = target.translateYProperty().get() + target.getOffsetY();
                
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
                
//                double m = y / x;
                double b = y1 - y / x * x1;
//                double r = PropertiesController.ARROW_DISTANCE;
                
//                System.out.println("####");
//                System.out.println("P1 ( " + x1 + " | " + y1 + " )");
//                System.out.println("P2 ( " + x2 + " | " + y2 + " )");
//                System.out.println("m = " + m);
//                System.out.println("b = " + b);
//                System.out.println("r = " + r);
                
                double p = 2 * (y / x * b - y / x * y2 - x2) / (1 + y / x * y / x);
                double q = (x2 * x2 + b * b + y2 * y2 - 2 * b * y2 - GravisProperties.ARROW_DISTANCE * GravisProperties.ARROW_DISTANCE) / (1 + y / x * y / x);

//                System.out.println("p = " + p);
//                System.out.println("q = " + q);

                if (x2 <= x1) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q);
                } else {
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };
        qct.xProperty().bind(bindingCurveEndX);
        arrow.translateXProperty().bind(bindingCurveEndX.subtract(GravisProperties.ARROW_WIDTH / 2));

        /**
         * Line's end Y coordinate. Changes it's value on changes of the related
         * X coordinate.
         */
        DoubleBinding bindingCurveEndY = new DoubleBinding()
        {
            {
                super.bind(bindingCurveEndX);
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

                double x2 = target.translateXProperty().get() + target.getOffsetX();
                double y2 = target.translateYProperty().get() + target.getOffsetY();
                
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

                return y / x * bindingCurveEndX.get() + y1 - y / x * x1;
            }
        };
        qct.yProperty().bind(bindingCurveEndY);
        arrow.translateYProperty().bind(bindingCurveEndY.subtract(GravisProperties.ARROW_HEIGHT / 2));
        
        DoubleBinding arrowAngle = new DoubleBinding()
        {
            {
                super.bind(bindingCurveEndY);
            }
            
            @Override
            protected double computeValue() {
                
                double x1 = bindingCurveControlX.get() + source.getOffsetX();
                double y1 = bindingCurveControlY.get() + source.getOffsetY();
                
                double x2 = target.translateXProperty().get() + target.getOffsetX();
                double y2 = target.translateYProperty().get() + target.getOffsetY();
                
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
        arrow.rotateProperty().bind(arrowAngle);
        
        getElements().add(mv);
        getElements().add(qct);
    }
    
    
    @Override
    public IGravisNode getSource() {
        return source;
    }
    
    @Override
    public IGravisNode getTarget() {
        return target;
    }
    
    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        shapes.add(arrow);
        return shapes;
    }
    
    @Override
    public void putOnTop() {
        EdgeLayer edgeLayer = (EdgeLayer) getParent();
        edgeLayer.getChildren().remove(this);
        edgeLayer.getChildren().remove(arrow);
        edgeLayer.getChildren().add(this);
        edgeLayer.getChildren().add(arrow);
    }

    @Override
    public void setTranslate(double positionX , double positionY) {
    }
}
