package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonArray;

import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

public class GetSensorsByKindHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GetSensorsByKindHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {
        // Récupère le type de capteur depuis l'URL
        String kind = context.pathParam("kind");

        // Exécute une requête SQL pour récupérer les identifiants des capteurs de ce type
        List<?> resultList = db
                .createNativeQuery("SELECT id FROM sensor WHERE dtype = ?")
                .setParameter(1, kind)
                .getResultList();
        
        // Récupère les identifiants des capteurs et les ajoute à une liste
        List<Integer> ids = new ArrayList<>();
        for (Object result : resultList) {
            if (result instanceof Number) {
                ids.add(((Number) result).intValue());
            }  
        }

        // Renvoie la liste des identifiants au format JSON
        JsonArray json = new JsonArray(ids);
        context.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(json.encode());
    }
}
