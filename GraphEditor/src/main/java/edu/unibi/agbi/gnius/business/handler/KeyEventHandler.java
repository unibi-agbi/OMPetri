/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.business.service.DataService;
import edu.unibi.agbi.gnius.business.service.SelectionService;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class KeyEventHandler
{
    @Autowired private SelectionService selectionService;
    @Autowired private DataService dataService;
    @Autowired private MouseEventHandler mouseEventHandler;
    
    private boolean isCloning;
    
    public void registerTo(Scene scene) {
        
        scene.setOnKeyPressed((KeyEvent event) -> {
            
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                
                mouseEventHandler.setCreatingNodes(false);
                
                selectionService.clear();
            }
            
            /**
             * Delete selected nodes.
             */
            else if (event.getCode().equals(KeyCode.DELETE)) {
                
                Platform.runLater(() -> {
                    dataService.removeSelected();
                });
            }
            
            /**
             * Copy, clone or paste selected nodes.
             */
            else if (event.isControlDown()) {
                
                if (event.getCode().equals(KeyCode.C)) {
                    
                    selectionService.copy();
                    isCloning = false;
                    
                } else if (event.getCode().equals(KeyCode.X)) {
                    
                    selectionService.copy();
                    isCloning = true;
                    
                } else if (event.getCode().equals(KeyCode.V)) {

                    selectionService.clear();
                    dataService.paste(isCloning);
                }
            }

            event.consume();
        });
    }
}