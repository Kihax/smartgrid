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
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Producer;
import fr.imta.smartgrid.model.Consumer;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import fr.imta.smartgrid.model.EVCharger;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class SensorUniqueHandler implements Handler<RoutingContext> {
    EntityManager db;

    public SensorUniqueHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        try {
            int sensorId = Integer.parseInt(event.pathParam("id"));
            
            Sensor s;
            
            try {
                s = (Sensor) db
                    .createNativeQuery("SELECT * FROM sensor WHERE id = ?", Sensor.class)
                    .setParameter(1, sensorId)
                    .getSingleResult();

                
            } catch (Exception e) {
                event.end("404 Not Found: ");
                return;
            }

             JsonObject res = new JsonObject();

            res.put("id", s.getId());
            res.put("name", s.getName());
            res.put("description", s.getDescription());
            res.put("kind", s.getDtype());

            try {
                Producer prod = db.find(Producer.class, s.getId());
                if (prod != null) {
                    System.out.println("SolarPanel trouvé avec id = " + s.getId());
                    res.put("power_source", prod.getPowerSource());
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération du producteur : " + e.getMessage());
            }

            try {
                System.out.println("Panneau solaire----------------------------------");
                SolarPanel sol = db.find(SolarPanel.class, s.getId());
                if (sol != null) {
                    System.out.println(sol.getEfficiency());
                    res.put("efficiency", sol.getEfficiency());
                } else {
                    System.out.println("Aucun panneau solaire trouvé avec l'ID " + s.getId());
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération du panneau solaire : " + e.getMessage());
            }

            try {
                WindTurbine wind = db.find(WindTurbine.class, s.getId());
                if (wind != null) {
                    res.put("height", wind.getHeight());
                    res.put("blade_length", wind.getBladeLength());
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération de l'éolienne : " + e.getMessage());
            }

            try {
                Consumer cons = db.find(Consumer.class, s.getId());
                if (cons != null) {
                    res.put("max_power", cons.getMaxPower() != null ? cons.getMaxPower() : 0);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération du consommateur : " + e.getMessage());
            }

            try {
                EVCharger ev = db.find(EVCharger.class, s.getId());
                if (ev != null) {
                    res.put("type", ev.getType());
                    res.put("maxAmp", ev.getMaxAmp());
                    res.put("voltage", ev.getVoltage());
                }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la récupération du chargeur EV : " + e.getMessage());
                }

                if (s.getGrid() != null) {
                    res.put("grid", s.getGrid().getId());
                }

                res.put("available_measurements", s.getMeasurements().stream().map(Measurement::getId).toList());
                res.put("owners", s.getOwners().stream().map(Person::getId).toList());

                event.end(res.toString());
                
            } catch (NumberFormatException e) {
                event.end("404 Not Found: "); 
                return; 
            }

        


    }
    
}
