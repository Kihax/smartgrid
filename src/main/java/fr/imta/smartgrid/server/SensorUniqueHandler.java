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

            System.out.println("------------------- Production -------------------");

            try {
                String powerSource = (String) db.createNativeQuery("SELECT power_source FROM producer WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();

                res.put("power_source", powerSource);
            } catch (Exception e) {
                System.out.println("No power source found");
            }

            try {
                int maxPower = (int) db.createNativeQuery("SELECT max_power FROM consumer WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();

                res.put("max_power", maxPower);
            } catch (Exception e) {
                System.out.println("No consumer found");
            }

            try {
                double efficiency = (double) db.createNativeQuery("SELECT efficiency FROM solar_panel WHERE id = ?")
                                         .setParameter(1, sensorId)
                                         .getSingleResult();
                res.put("efficiency", efficiency);
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération du panneau solaire : " + e.getMessage());
            }

            try {
                Object[] windTurbineData = (Object[]) db.createNativeQuery("SELECT height, bladelength FROM wind_turbine WHERE id = ?")
                    .setParameter(1, sensorId)
                    .getSingleResult();
                if (windTurbineData != null && windTurbineData.length == 2) {
                    res.put("height", windTurbineData[0]);
                    res.put("bladelength", windTurbineData[1]);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération de l'éolienne : " + e.getMessage());
            }

            try {
                Object[] evChargerData = (Object[]) db.createNativeQuery("SELECT voltage, maxamp, connector_type FROM ev_charger WHERE id = ?")
                    .setParameter(1, sensorId)
                    .getSingleResult();
                
                if (evChargerData != null && evChargerData.length == 3) {
                    res.put("voltage", evChargerData[0]);
                    res.put("maxamp", evChargerData[1]);
                    res.put("connector_type", evChargerData[2]);
                }

            } catch (Exception e) {

            }

            if (s.getGrid() != null) {
                res.put("grid", s.getGrid().getId());
            }


            res.put("available_measurements", s.getMeasurements().stream().map(Measurement::getId).toList());
            res.put("owners", s.getOwners().stream().map(Person::getId).toList());

            event.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(res.toString());
        } catch (Exception e) {
            event.end("500 Internal Server Error: " + e.getMessage());
            return;
        }

    }
    
}
