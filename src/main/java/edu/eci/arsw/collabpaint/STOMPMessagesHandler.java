/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.collabpaint;

import edu.eci.arsw.collabpaint.model.Point;
import edu.eci.arsw.collabpaint.util.JedisUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 *
 * @author camila
 */
@Controller
public class STOMPMessagesHandler {
    @Autowired
    SimpMessagingTemplate msgt;
    ConcurrentHashMap<String, ArrayList<Point>> poligono = new ConcurrentHashMap<>();

    @MessageMapping("/newpoint.{numdibujo}")    
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {
        
        Jedis jedis;
        jedis = JedisUtil.getPool().getResource();
        
        Transaction t = jedis.multi();
        List<Object> res=t.exec();
        
        while(!res.isEmpty()){
            t.watch("x" , "y");
            t.rpush("x", String.valueOf(pt.getX()));
            t.rpush("y", String.valueOf(pt.getY()));
            res=t.exec();
        }
        
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
        
        jedis.close();
    }
}
