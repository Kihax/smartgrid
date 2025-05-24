package fr.imta.smartgrid.server;

import java.util.ArrayList;
import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GetGridByIDHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetGridByIDHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        try {
            int gridId = Integer.parseInt(context.pathParam("id")); // Récupération de l'id de la grille et transformation en int
            
            Grid g; // Création d'un objet Grid
            
            try {
                g = (Grid) db
                    .createNativeQuery("SELECT * FROM grid WHERE id = ?", Grid.class)
                    .setParameter(1, gridId)
                    .getSingleResult(); // Récupération de la grille dans la base de données

                List<Person> persons = g.getPersons(); // Récupération de la liste des utilisateurs de la grille
                List<Sensor> sensors = g.getSensors(); // Récupération de la liste des capteurs de la grille

                // Initialisation des listes d'id
                List<Integer> personIds = new ArrayList<>();
                List<Integer> sensorIds = new ArrayList<>();

                for (Person p : persons) {
                    personIds.add(p.getId()); // Récupération de l'id de l'utilisateur
                }
                for (Sensor s : sensors) {
                    sensorIds.add(s.getId()); // Récupération de l'id du capteur
                }
                
                // Création de l'objet JSON
                JsonObject message = new JsonObject();
                message.put("id", g.getId());
                message.put("name", g.getName());
                message.put("description", g.getDescription());
                message.put("users", personIds);
                message.put("sensors", sensorIds);
                
                // Envoi de l'objet JSON en réponse
                context.response()
                    .putHeader("content-type", "application/json")
                    .end(message.toString());
                    
            } catch (Exception e) {
                context.response()
                    .setStatusCode(404)
                    .putHeader("content-type", "application/json")
                    .end("{\"error\": \"Grid not found\"}");
            }
            
            context.end("Grid id: " + gridId);
        } catch (NumberFormatException e) {
            context.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end("{\"error\": \"Grid not found\"}");
        }

        


    }
    
}
