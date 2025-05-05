package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

import java.util.List;

public class PersonsHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public PersonsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext ctx) {
        try {
            List<Integer> personIds = db
                .createQuery("SELECT p.id FROM Person p", Integer.class)
                .getResultList();

            String json = personIds.toString(); // [1, 2]

            ctx.response()
               .putHeader("Content-Type", "application/json")
               .end(json);

        } catch (Exception e) {
            ctx.response()
               .setStatusCode(500)
               .putHeader("Content-Type", "application/json")
               .end("{\"error\": \"Internal server error\"}");
        }
    }
}