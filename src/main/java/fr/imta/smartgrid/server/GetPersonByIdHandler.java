package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

public class GetPersonByIdHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GetPersonByIdHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String idParam = ctx.pathParam("id");
        int personId;

        try {
            personId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("{\"error\": \"Invalid ID\"}");
            return;
        }

        Person person = db.find(Person.class, personId);
        if (person == null) {
            ctx.response().setStatusCode(404).end("{\"error\": \"Person not found\"}");
            return;
        }

        // Native SQL to get sensor IDs from association table
        List<Integer> sensorIds = ((List<?>) db.createNativeQuery(
            "SELECT sensor_id FROM person_sensor WHERE person_id = ?"
        )
        .setParameter(1, personId)
        .getResultList())
        .stream()
        .map(id -> ((Number) id).intValue())
        .collect(Collectors.toList());

        JsonObject json = new JsonObject()
            .put("id", person.getId())
            .put("first_name", person.getFirstName())
            .put("last_name", person.getLastName())
            .put("grid", person.getGrid() != null ? person.getGrid().getId() : null)
            .put("owned_sensors", sensorIds);

        ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(json.encode());
    }
}