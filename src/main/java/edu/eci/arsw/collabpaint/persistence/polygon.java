/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint.persistence;

import edu.eci.arsw.collabpaint.model.Point;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author camila
 */

@Service
public class polygon implements persistence{
    
    @Autowired
    SimpMessagingTemplate msgt;
    
    ConcurrentHashMap<String, ArrayList<Point>> poligono = new ConcurrentHashMap<>();
    
    @Override
    public void handlePointEvent(Point pt, String numdibujo) {
        if(poligono.containsKey(numdibujo)){
            poligono.get(numdibujo).add(pt);
            if(poligono.get(numdibujo).size()>=3){
                msgt.convertAndSend("/topic/newpolygon."+numdibujo, poligono.get(numdibujo));
            }
        }else{
            ArrayList puntos = new ArrayList<>();
            puntos.add(pt);
            poligono.put(numdibujo, puntos);                        
        }
    }
    
}
