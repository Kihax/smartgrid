package fr.imta.smartgrid.server;

import java.util.ArrayList;
import java.util.List;

import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
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

        // Exécute une requête SQL pour récupérer tous les identifiants des consommateurs
        List<Integer> producerIds = db.createNativeQuery("SELECT id FROM consumer").getResultList();

        // Récupère les identifiants des consommateurs et les ajoute à une liste
        List<JsonObject> producers = new ArrayList<>();
        for (Integer producerId : producerIds) {
            JsonObject res = sensor(producerId);
            producers.add(res);
        }

        // Envoi de la réponse au format JSON
        context.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(producers.toString());
    }
    
}