package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Measurement;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import jakarta.persistence.EntityManager;

public class GetMeasurementByIDValuesHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetMeasurementByIDValuesHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        // Récupération de l'identifiant de la mesure depuis l'URL
        int id;
        Measurement m;

        try {
            id = Integer.parseInt(context.pathParam("id"));
            m = db.find(Measurement.class, id);

            if (m == null) {
                context.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end("{\"error\": \"No measurement with given id is found\"}");
                return;
            }
        } catch (NumberFormatException e) {
            context.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "application/json")
                .end("{\"error\": \"Invalid measurement ID\"}");
            return;
        }

        // Récupération des paramètres de la requête
        final int[] from = {0};
        final int[] to = {2147483646};

        if(!context.queryParam("from").isEmpty()) {
            try{
                from[0] = Integer.parseInt(context.queryParam("from").get(0));
            } catch (NumberFormatException e) {

            }
        }
        if(!context.queryParam("to").isEmpty()) {
            try{
                to[0] = Integer.parseInt(context.queryParam("to").get(0));
            } catch (NumberFormatException e) {
                to[0] = 2147483646;
            }
        }

       

        List<JsonObject> mesures = new java.util.ArrayList<>();

        // Parcours des points de mesure
        m.getDatapoints().forEach(dp -> {
            // Vérification si le point de mesure est dans l'intervalle
            if(dp.getTimestamp() < from[0] || dp.getTimestamp() > to[0]) {
                return;
            }
            // Ajout du point de mesure à la liste
            JsonObject obj = new JsonObject()
                .put("timestamp", dp.getTimestamp())
                .put("value", dp.getValue());
            mesures.add(obj);
        });

        // Création de la réponse JSON
        JsonObject obj = new JsonObject()
            .put("sensor_id", m.getSensor() != null ? m.getSensor().getId() : null)
            .put("measurement_id", m.getId())
            .put("values", mesures);

        // Envoi de la réponse au format JSON
        context.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(obj.encode());
    }
}
