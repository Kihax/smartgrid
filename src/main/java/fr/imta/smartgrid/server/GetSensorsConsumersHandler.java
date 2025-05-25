package fr.imta.smartgrid.server;

import java.util.ArrayList;
import java.util.List;

import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

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

        if (s.getGrid() != null) {
            res.put("grid", s.getGrid().getId()); // Ajout de la grille au JSON
        }

        // Récupération des mesures associées au capteur
        res.put("available_measurements", s.getMeasurements().stream().map(Measurement::getId).toList());
        // Récupération des utilisateurs associés au capteur
        res.put("owners", s.getOwners().stream().map(Person::getId).toList());

        try {
            // Récupération de la puissance maximale si il s'agit d'un consommateur
            double maxPower = (double) db.createNativeQuery("SELECT max_power FROM consumer WHERE id = ?")
                                    .setParameter(1, sensorId)
                                    .getSingleResult();

            res.put("max_power", maxPower);
        } catch (Exception e) {}

        try {
            // Récupération de la tension, de l'ampérage maximum et du type de connecteur si il s'agit d'une borne de recharge
            Object[] evChargerData = (Object[]) db.createNativeQuery("SELECT voltage, maxamp, connector_type FROM ev_charger WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();
                
            if (evChargerData != null && evChargerData.length == 3) {
                res.put("type", evChargerData[2]);
                res.put("maxAmp", evChargerData[1]);
                res.put("voltage", evChargerData[0]);
  
            }
        } catch (Exception e) {}

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
