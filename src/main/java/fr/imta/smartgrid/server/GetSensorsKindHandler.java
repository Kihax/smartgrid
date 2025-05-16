package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonArray;

import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

public class GetSensorsKindHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GetSensorsKindHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        String kind = event.pathParam("kind");

        List<?> resultList = db
                .createNativeQuery("SELECT id FROM sensor WHERE dtype = ?")
                .setParameter(1, kind)
                .getResultList();
        
        List<Integer> ids = new ArrayList<>();
        for (Object result : resultList) {
            if (result instanceof Number) {
                ids.add(((Number) result).intValue());
            }  
        }

        JsonArray json = new JsonArray(ids);
        event.response()
            .putHeader("Content-Type", "application/json")              .end(json.encode());
    }
}
