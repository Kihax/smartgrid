package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Définition du handler qui traite les requêtes PUT pour ajouter une personne dans la base de donnée
public class PutPersonHandler implements Handler<RoutingContext> {
    EntityManager db;

    // Constructeur
    public PutPersonHandler(EntityManager db) {
        this.db = db;
    }

    // Méthode principale
    @Override
    public void handle(RoutingContext ctx) {
        
        // Récupère le corps JSON de la requête et renvoie une erreur 500 si le format n'est pas correct
        JsonObject body;
        try {
            body = ctx.body().asJsonObject(); 
        } catch (Exception e) {
            ctx.response()
                .setStatusCode(500) 
                .end("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        // Créer une nouvelle personne 
        Person person = new Person();

        try {
            db.getTransaction().begin();

            // Ajout dans la base de donnée si tous les éléments sont présents
            if (body.containsKey("first_name") && body.containsKey("last_name") && body.containsKey("grid")) {

                // Récupère le prénom de la personne
                String personFirstName = body.getString("first_name");
                person.setFirstName(personFirstName);

                // Récupère le nom de famille de la personne
                String personLastName = body.getString("last_name");
                person.setLastName(personLastName);

                // Récupère la grille associée
                Integer gridId = body.getInteger("grid");
                Grid grid = db.find(Grid.class, gridId);
                if (grid != null) {
                    person.setGrid(grid);   
                }

                db.persist(person);
                db.flush();

            } else {

                // Renvoie une erreur 500 dans le cas où il manquerait un champ
                ctx.response().setStatusCode(500).end("{\"error\": \"Missing required fields\"}");
                return;
            }

            // Ajout des capteurs (optionnel)
            if (body.containsKey("owned_sensors")) {
                JsonArray sensorArray = body.getJsonArray("owned_sensors");

                // Récupère l'id de la personne ajoutée
                Integer personId = person.getId();

                // Ajoute les nouvelles associations
                for (int i = 0; i < sensorArray.size(); i++) {
                    Integer sensorId = sensorArray.getInteger(i);
                    db.createNativeQuery("INSERT INTO person_sensor (person_id, sensor_id) VALUES (?, ?)")
                        .setParameter(1, personId)
                        .setParameter(2, sensorId)
                        .executeUpdate();
                }
            }

            db.getTransaction().commit();

            // Retourne l'id de la personne ajoutée
            JsonObject json = new JsonObject()
                .put("id", person.getId());

            // Envoie la réponse au client
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(json.encode());

        } catch (Exception e) {
            
            // Retourne 500 en cas d'erreur pendant la mise à jour
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            ctx.response().setStatusCode(500).end("{\"error\": \"Database update failed\"}");
        }
    }
}
