package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Définition du handler qui traite les requêtes POST pour mettre à jour une personne par rapport à son ID
public class PostPersonIDHandler implements Handler<RoutingContext> {
    EntityManager db;

    // Constructeur
    public PostPersonIDHandler(EntityManager db) {
        this.db = db;
    }

    // Méthode principale
    @Override
    public void handle(RoutingContext context) {
        
        // Récupère le corps JSON de la requête
        JsonObject body;
        body = context.body().asJsonObject();

        // Récupère l'identifiant de la personne depuis l'URL
        String idParam = context.pathParam("id");
        int personId;
    
        
        // Convertis l'identifiant en entier
        try {
            personId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            context.response()
               .setStatusCode(404)
               .putHeader("content-type", "application/json")
               .end("{\"error\": \"Person not found\"}");
            return;
        }

        // Recherche la personne dans la base de donnée
        Person person;
        try {
            person  = db.find(Person.class, personId);
        } catch (Exception e) {
            context.response()
               .setStatusCode(404)
               .putHeader("content-type", "application/json")
               .end("{\"error\": \"Person not found\"}");
            return;
        }
        
        try {
            db.getTransaction().begin();

            // Met à jour le prénom de la personne
            if (body.containsKey("first_name")) {

                String personFirstName = body.getString("first_name");
                person.setFirstName(personFirstName);

                // Mise à jour dans la base de donnée
                db.createNativeQuery("UPDATE person SET firstname = ? WHERE id = ?")
                        .setParameter(1, personFirstName)
                        .setParameter(2, personId)
                        .executeUpdate();
            }

            // Met à jour le nom de famille de la personne
            if (body.containsKey("last_name")) {

                String personLastName = body.getString("last_name");
                person.setLastName(personLastName);

                // Mise à jour dans la base de donnée
                db.createNativeQuery("UPDATE person SET lastname = ? WHERE id = ?")
                        .setParameter(1, personLastName)
                        .setParameter(2, personId)
                        .executeUpdate();
            }

            // Met à jour du grid associé à la personne
            if (body.containsKey("grid")) {

                Integer gridId = body.getInteger("grid");
                Grid grid = db.find(Grid.class, gridId);
                
                if (grid != null) {
                    person.setGrid(grid);
                }

                // Mise à jour dans la base de donnée
                db.createNativeQuery("UPDATE person SET grid = ? WHERE id = ?")
                        .setParameter(1, gridId)
                        .setParameter(2, personId)
                        .executeUpdate();
            }

            // Met à jour les capteurs
            if (body.containsKey("owned_sensors")) {
                JsonArray sensorArray = body.getJsonArray("owned_sensors");

                // Mise à jour dans la base de donnée

                // Supprimer les anciennes associations
                db.createNativeQuery("DELETE FROM person_sensor WHERE person_id = ?")
                    .setParameter(1, personId)
                    .executeUpdate();

                // Ajouter les nouvelles associations
                for (int i = 0; i < sensorArray.size(); i++) {
                    Integer sensorId = sensorArray.getInteger(i);
                    db.createNativeQuery("INSERT INTO person_sensor (person_id, sensor_id) VALUES (?, ?)")
                        .setParameter(1, personId)
                        .setParameter(2, sensorId)
                        .executeUpdate();
                }
            }

            db.merge(person);
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
