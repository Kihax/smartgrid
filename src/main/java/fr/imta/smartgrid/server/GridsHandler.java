package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridsHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GridsHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        @SuppressWarnings("unchecked")
        List<Integer> listIdList = (List<Integer>) db.createNativeQuery("SELECT id FROM grid").getResultList();
        
        event.end(listIdList.toString());


        System.out.println(listIdList);

    }
    
}
