package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

public class GetPersonByIDHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GetPersonByIDHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {
        // Récupère l'id depuis l'url
        String idParam = context.pathParam("id");
        int personId;

        // Vérifie si l'id est un entier
        try {
            personId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end("{\"error\": \"Person not found\"}");
            return;
        }

        // Récupère la personne depuis la base de données
        Person person = db.find(Person.class, personId);
        if (person == null) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(404)
                    .end("{\"error\": \"Person not found\"}");
            return;
        }

        // Récupère les capteurs associés à la personne
        List<Integer> sensorIds = ((List<?>) db.createNativeQuery("SELECT sensor_id FROM person_sensor WHERE person_id = ?")
                                                .setParameter(1, personId)
                                                .getResultList())
                                                .stream()
                                                .map(id -> ((Number) id).intValue())
                                                .collect(Collectors.toList());

        // Crée un objet JSON avec les informations de la personne
        JsonObject json = new JsonObject()
            .put("id", person.getId())
            .put("first_name", person.getFirstName())
            .put("last_name", person.getLastName())
            .put("grid", person.getGrid() != null ? person.getGrid().getId() : null)
            .put("owned_sensors", sensorIds);

        // Envoie la réponse au client
        context.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(json.encode());
    }
}