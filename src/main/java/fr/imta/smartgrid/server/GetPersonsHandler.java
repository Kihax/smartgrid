package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

import java.util.List;

public class GetPersonsHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public GetPersonsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        // Récupération de la liste des identifiants de la table Person
        List<Integer> personIds = db
                .createQuery("SELECT p.id FROM Person p", Integer.class)
                .getResultList();

        // Envoi de la liste des identifiants au client
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(personIds.toString());
    }
}