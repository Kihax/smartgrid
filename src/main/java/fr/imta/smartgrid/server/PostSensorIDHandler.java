package fr.imta.smartgrid.server;

import java.util.ArrayList;
import java.util.List;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;


import jakarta.persistence.EntityManager;

// Définition du handler qui traite les requêtes POST pour mettre à jour un capteur par rapport à son ID
public class PostSensorIDHandler implements Handler<RoutingContext> {
    EntityManager db;

    // Constructeur
    public PostSensorIDHandler(EntityManager db) {
        this.db = db;
    }

    // Méthode principale
    @Override
    public void handle(RoutingContext context) {
        
        // Récupère le corps JSON de la requête
        JsonObject body;
        body = context.body().asJsonObject();

        // Récupère l'identifiant du capteur depuis l'URL
        String idParam = context.pathParam("id");
        int sensorId;
        Sensor sensor;

        try {
            // Convertis l'identifiant en entier
           sensorId = Integer.parseInt(idParam);

            // Recherche le capteur dans la base de donnée
            sensor = db.find(Sensor.class, sensorId);

            // Renvoie une erreur 404 si l'ID n'est pas dans la base de donnée
            if (sensor == null) {
                context.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Sensor not found with ID: " + sensorId).encode());
                return;
            }
        } catch (NumberFormatException e) {
            // Renvoie une erreur 404 si le capteur n'est pas trouvé
            context.response()
               .setStatusCode(404)
               .putHeader("Content-Type", "application/json")
               .end(new JsonObject().put("error", "Invalid sensor ID").encode());
            return;
        }

        try {
            db.getTransaction().begin();

            // Met à jour le nom du capteur
            if (body.containsKey("name")) {
                sensor.setName(body.getString("name"));
            }

            // Met à jour la description
            if (body.containsKey("description")) {
                sensor.setDescription(body.getString("description"));
            }

            // Met à jour la liste des propriétaires
            if (body.containsKey("owners")) {
                JsonArray jsonOwners = body.getJsonArray("owners");
                List<Person> owners = new ArrayList<>();
                for (int i = 0; i < jsonOwners.size(); i++) {
                    Integer ownerId = jsonOwners.getInteger(i);
                    Person owner = db.find(Person.class, ownerId);
                    if (owner != null) {
                        owners.add(owner);
                    }
                }
                sensor.setOwners(owners);
            }

            // Dans le cas d'un EVCharger (on ignore les champs qui ne correspondent pas à ce type de capteur)
            if (sensor instanceof EVCharger charger) {

                // Met à jour la puissance maximale
                if (body.containsKey("max_power")) {
                    charger.setMaxPower(body.getDouble("max_power"));
                }

                // Met à jour le type
                if (body.containsKey("type")) {
                    charger.setType(body.getString("type"));
                }

                // Met à jour voltage
                if (body.containsKey("voltage")) {
                        charger.setVoltage(body.getInteger("voltage"));
                }

                // Met à jour maxAmp
                if (body.containsKey("maxAmp")) {
                        charger.setMaxAmp(body.getInteger("maxAmp"));
                }
        
            }

            // Dans le cas d'un panneau solaire
            if (sensor instanceof SolarPanel panel) {

                // Met à jour la source d'alimentation
                if (body.containsKey("power_source")) {
                    panel.setPowerSource(body.getString("power_source"));
                }

                // Met à jour l'efficacité du capteur
                if (body.containsKey("efficiency")) {
                    panel.setEfficiency(body.getFloat("efficiency"));
                }
            }

            // Si le capteur est une éolienne
            if (sensor instanceof WindTurbine turbine) {

                // Met à jour la source d'alimentation
                if (body.containsKey("power_source")) {
                    turbine.setPowerSource(body.getString("power_source"));
                }

                // Met à jour la hauteur de l'éolienne
                if (body.containsKey("height")) {
                    turbine.setHeight(body.getDouble("height"));
                }

                // Met à jour la longueur des lames
                if (body.containsKey("blade_length")) {
                    turbine.setBladeLength(body.getDouble("blade_length"));
                }
            }

            // Mise à jour et enregistrement dans la base de données
            db.merge(sensor); 
            db.getTransaction().commit(); 

            // Retourne 200 en cas de succès
            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end("{\"status\": \"success\"}");

        } catch (Exception e) {
            
            // Retourne 500 en cas d'erreur pendant la mise à jour
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            context.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json")
                    .end("{\"error\": \"Database update failed\", \"details\": \"" + e.getMessage() + "\"}");
        }
    }
}
