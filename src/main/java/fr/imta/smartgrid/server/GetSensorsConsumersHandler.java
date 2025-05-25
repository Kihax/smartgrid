package fr.imta.smartgrid.server;

import java.awt.image.RescaleOp;
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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GetSensorsConsumersHandler implements Handler<RoutingContext> {
    private EntityManager db;

    public GetSensorsConsumersHandler(EntityManager entityManager) {
        this.db = entityManager;
    }

    public JsonObject sensor(Integer sensorId) {
        // Création d'un objet JSON pour stocker les informations du capteur
        JsonObject res = new JsonObject();
            
        // Exécute une requête SQL pour récupérer le capteur correspondant à l'identifiant
        Sensor s = (Sensor) db
                .createNativeQuery("SELECT * FROM sensor WHERE id = ?", Sensor.class)
                .setParameter(1, sensorId)
                .getSingleResult();
            
        res.put("id", s.getId());
        res.put("name", s.getName());
        res.put("description", s.getDescription());
        res.put("kind", s.getDtype());


        try {
            // Récupération de la source d'énergie si il s'agit d'un producteur
            String powerSource = (String) db.createNativeQuery("SELECT power_source FROM producer WHERE id = ?")
            .setParameter(1, sensorId)
            .getSingleResult();

            res.put("power_source", powerSource); // Ajout de la source d'énergie au JSON
        } catch (Exception e) {}

        try {
            // Récupération de la puissance maximale si il s'agit d'un consommateur
            double maxPower = (double) db.createNativeQuery("SELECT max_power FROM consumer WHERE id = ?")
                                    .setParameter(1, sensorId)
                                    .getSingleResult();

            res.put("max_power", maxPower);
        } catch (Exception e) {}

        try {
            // Récupération de la puissance maximale si il s'agit d'un panneau solaire
            double efficiency = (double) db.createNativeQuery("SELECT efficiency FROM solar_panel WHERE id = ?")
                                         .setParameter(1, sensorId)
                                         .getSingleResult();
            res.put("efficiency", efficiency);
        } catch (Exception e) {}

        try {
            // Récupération de la hauteur et de la longeur des pales si il s'agit d'une éolienne
            Object[] windTurbineData = (Object[]) db.createNativeQuery("SELECT height, bladelength FROM wind_turbine WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();
            if (windTurbineData != null && windTurbineData.length == 2) {
                res.put("height", windTurbineData[0]);
                res.put("bladelength", windTurbineData[1]);
            }
        } catch (Exception e) {}

        try {
            // Récupération de la tension, de l'ampérage maximum et du type de connecteur si il s'agit d'une borne de recharge
            Object[] evChargerData = (Object[]) db.createNativeQuery("SELECT voltage, maxamp, connector_type FROM ev_charger WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();
                
            if (evChargerData != null && evChargerData.length == 3) {
                res.put("voltage", evChargerData[0]);
                res.put("maxamp", evChargerData[1]);
                res.put("connector_type", evChargerData[2]);
            }
        } catch (Exception e) {}

        if (s.getGrid() != null) {
            res.put("grid", s.getGrid().getId()); // Ajout de la grille au JSON
        }

        // Récupération des mesures associées au capteur
        res.put("available_measurements", s.getMeasurements().stream().map(Measurement::getId).toList());
        // Récupération des utilisateurs associés au capteur
        res.put("owners", s.getOwners().stream().map(Person::getId).toList());

        // Envoi de la réponse au format JSON
        return res;
    }

    @Override
    public void handle(RoutingContext context) {

         // Query for all consumers
        List<Consumer> consumers = db.createQuery("SELECT c FROM Consumer c", Consumer.class).getResultList();
        
        // Create JSON response
        JsonArray response = new JsonArray();
        
        for (Consumer consumer : consumers) {
            JsonObject consumerJson = new JsonObject()
                    .put("id", consumer.getId())
                    .put("name", consumer.getName())
                    .put("description", consumer.getDescription())
                    .put("max_power", consumer.getMaxPower());
            
            // Add grid ID if present
            if (consumer.getGrid() != null) {
                consumerJson.put("grid", consumer.getGrid().getId());
            }
            
            // Add kind based on class
            String kind = consumer instanceof EVCharger ? "EVCharger" : "Consumer";
            consumerJson.put("kind", kind);
            
            // Add available measurements
            JsonArray measurements = new JsonArray();
            consumer.getMeasurements().forEach(measurement -> measurements.add(measurement.getId()));
            consumerJson.put("available_measurements", measurements);
            
            // Add owners
            JsonArray owners = new JsonArray();
            consumer.getOwners().forEach(owner -> owners.add(owner.getId()));
            consumerJson.put("owners", owners);
            
            // Add specific EVCharger fields
            if (consumer instanceof EVCharger) {
                EVCharger evCharger = (EVCharger) consumer;
                consumerJson.put("voltage", evCharger.getVoltage());
                consumerJson.put("maxAmp", evCharger.getMaxAmp());
                consumerJson.put("type", evCharger.getType());
            }
            
            response.add(consumerJson);
        }
        
        // Return response
        context.response()
                .putHeader("content-type", "application/json")
                .end(response.encode());
    }
    
}
