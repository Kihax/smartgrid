package fr.imta.smartgrid.server;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridUniqueHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GridUniqueHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        try {
            int gridId = Integer.parseInt(event.pathParam("id"));
            
            Grid g;
            
            try {
                g = (Grid) db
                    .createNativeQuery("SELECT * FROM grid WHERE id = ?", Grid.class)
                    .setParameter(1, gridId)
                    .getSingleResult();

                List<Person> persons = g.getPersons();
                List<Sensor> sensors = g.getSensors();

                List<Integer> personIds = new ArrayList<>();
                List<Integer> sensorIds = new ArrayList<>();
                for (Person p : persons) {
                    personIds.add(p.getId());
                }
                for (Sensor s : sensors) {
                    sensorIds.add(s.getId());
                }

                for (Person p : persons) {
                    p.setGrid(null);
                }
                for (Sensor s : sensors) {
                    s.setGrid(null);
                }
                

                JsonObject message = new JsonObject();
                message.put("id", g.getId());
                message.put("name", g.getName());
                message.put("description", g.getDescription());
                message.put("users", personIds);
                message.put("sensors", sensorIds);

                
                event.end(message.toString());
            } catch (Exception e) {
                event.end("404 Not Found: ");
                return;
            }
            
            event.end("Grid id: " + gridId);
        } catch (NumberFormatException e) {
            event.end("404 Not Found: "); 
            return; 
        }

        


    }
    
}
