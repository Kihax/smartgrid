package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Measurement;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import jakarta.persistence.EntityManager;

public class MeasurementIDHandler implements Handler<RoutingContext> {
    EntityManager db;

    public MeasurementIDHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        int id = Integer.parseInt(event.pathParam("id"));
        Measurement m = db.find(Measurement.class, id);

        if (m == null) {
            event.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "application/json")
                .end("{\"error\": \"No measurement with given id is found\"}");
            return;
        }

        JsonObject obj = new JsonObject()
            .put("id", m.getId())
            .put("sensor", m.getSensor() != null ? m.getSensor().getId() : null)
            .put("name", m.getName())
            .put("unit", m.getUnit());

        event.response()
            .putHeader("Content-Type", "application/json")
            .end(obj.encode());
    }
}
