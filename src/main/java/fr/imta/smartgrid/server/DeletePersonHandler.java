package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.stream.Collectors;

public class DeletePersonHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public DeletePersonHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {
        String idParam = context.pathParam("id");
        if (idParam == null) {
            context.response()
                   .setStatusCode(400)
                   .end("Missing ID");
            return;
        }

        try {
            int personId = Integer.parseInt(idParam);

            EntityTransaction tx = db.getTransaction();
            tx.begin();

            Person person = db.find(Person.class, personId);
            if (person == null) {
                tx.rollback();
                context.response()
                       .setStatusCode(404)
                       .end("Person not found");
                return;
            }

            db.remove(person);
            tx.commit();

            context.response()
                   .setStatusCode(200)
                   .end("Person successfully deleted");
        } catch (NumberFormatException e) {
            context.response()
                   .setStatusCode(500)
                   .end("Error during deletion: " + e.getMessage());
        }
    }
}