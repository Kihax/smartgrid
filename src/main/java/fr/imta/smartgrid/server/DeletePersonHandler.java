package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class DeletePersonHandler implements Handler<RoutingContext> {
    private final EntityManager db;

    public DeletePersonHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        // Récupère l'indentifiant de la personne à supprimer depuis l'URL
        String idParam = context.pathParam("id");
        Person person;
        // Récupère la personne à partir de l'identifiant
        try {
            int personId = Integer.parseInt(idParam);
            person = db.find(Person.class, personId);
            
            if (person == null) {
                // Renvoie une erreur 404 si l'utilisateur n'existe pas
                context.response().setStatusCode(404).end("{\"error\": \"Person not found\"}");
                return;
            }
        } catch (NumberFormatException e) {
            // Renvoie une erreur 404 si l'utilisateur n'existe pas
            context.response().setStatusCode(404).end("{\"error\": \"Person not found\"}");
            return;
        }

        try {
            // Démarre une transaction pour supprimer la personne
            EntityTransaction tx = db.getTransaction();
            tx.begin();

            // Supprime la personne de la base de données
            db.remove(person);
            // Commit la transaction
            tx.commit();

            // Renvoie une réponse 200 si la suppression a réussi
            context.response()
                   .setStatusCode(200)
                   .end("Person successfully deleted");
        } catch (NumberFormatException e) {
            // Renvoie une erreur 500 si la suppression échoue
            context.response()
                   .setStatusCode(500)
                   .end("Error during deletion: " + e.getMessage());
        }
    }
}