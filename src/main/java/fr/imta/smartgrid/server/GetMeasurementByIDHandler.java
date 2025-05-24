package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Measurement;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import jakarta.persistence.EntityManager;

public class GetMeasurementByIDHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetMeasurementByIDHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        try {
            // Récupération de l'id de la mesure depuis la requête et recherche de la mesure dans la base de données
            int id = Integer.parseInt(context.pathParam("id"));
            Measurement m = db.find(Measurement.class, id);

            if (m == null) {
                context.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \"No measurement with given id is found\"}");
                return;
            }

            // Création de l'objet JSON contenant les informations de la mesure
            JsonObject obj = new JsonObject()
                .put("id", m.getId())
                .put("sensor", m.getSensor() != null ? m.getSensor().getId() : null)
                .put("name", m.getName())
                .put("unit", m.getUnit());

            context.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(obj.encode());
            return;
        } catch(NumberFormatException e) {
            context.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \"No measurement with given id is found\"}");
        }

        context.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "application/json")
                .end("{\"error\": \"No measurement with given id is found\"}");
    }
}
