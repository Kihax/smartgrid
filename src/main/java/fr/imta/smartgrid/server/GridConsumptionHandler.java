package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Consumer;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridConsumptionHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GridConsumptionHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        try {
            int gridId = Integer.parseInt(event.pathParam("id"));
            
            Grid g;
            
            try {
                // We find the grid with the id associated

                g = (Grid) db
                    .createNativeQuery("SELECT * FROM grid WHERE id = ?", Grid.class)
                    .setParameter(1, gridId)
                    .getSingleResult();  

                List<Sensor> sensors = g.getSensors();

                final float[] totalEnergy = {0};

                sensors.forEach(s -> {
                    
                    try {
                        System.out.println("Sensor id: " + s.getId());
                        System.out.println("Sensor name: " + s.getName());

                        Consumer result = db.find(Consumer.class, s.getId());

                        if(result == null) { // Check if sensor is a producer or a charger
                            System.out.println("No producer found for sensor: " + s.getName());
                        }else {
                            s.getMeasurements().forEach(m -> {
                                if(m.getName().equals("power")) { // Only print power measurements
                                    m.getDatapoints().forEach(d -> {
                                        totalEnergy[0] += d.getValue() * 60.0; // en Joules
                                        System.out.println("Sensor: " + s.getName() + ", Measurement: " + m.getName() + ", DataPoint: " + d.getValue());
                                    });
                                }
                            }); 
                        }
                    } catch (Exception e) {
                        System.out.println("No producer found for sensor: " + s.getName());
                    }
                    

                    
                });

                
                event.end(totalEnergy[0] + " Joules");
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
