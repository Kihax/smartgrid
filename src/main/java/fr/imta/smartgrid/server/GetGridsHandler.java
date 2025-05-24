package fr.imta.smartgrid.server;

import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GetGridsHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetGridsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {
        @SuppressWarnings("unchecked")
        // Récupération de la liste des identifiants de la table grid
        List<Integer> listIdList = (List<Integer>) db.createNativeQuery("SELECT id FROM grid").getResultList();
        
        // Envoi de la liste des identifiants au client
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(listIdList.toString());
    }
    
}
