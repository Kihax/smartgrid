package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Consumer;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GetGridConsumptionHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetGridConsumptionHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        try {
            int gridId = Integer.parseInt(context.pathParam("id")); // Récupération de l'id de la grille et transformation en int
            
            Grid g = (Grid) db
                    .createNativeQuery("SELECT * FROM grid WHERE id = ?", Grid.class)
                    .setParameter(1, gridId)
                    .getSingleResult();  

                // Récupération de la liste des capteurs de la grille
                List<Sensor> sensors = g.getSensors();

                final float[] totalEnergy = {0}; // Initialisation de la variable pour stocker l'énergie totale

                // Parcours de la liste des capteurs
                sensors.forEach(s -> { 
                    
                try {
                    Consumer result = db.find(Consumer.class, s.getId()); // Récupération du capteur dans la base de données

                    if(result != null) { // Vérification si le capteur est un consommateur
                        s.getMeasurements().forEach(m -> { // Parcours de la liste des mesures du capteur
                            if(m.getName().equals("power")) { // Vérification si la mesure est de type "power"
                                m.getDatapoints().forEach(d -> { // Parcours de la liste des points de données de la mesure
                                    totalEnergy[0] += d.getValue() * 60.0; // Calcul de l'énergie totale en Joules car les valeurs sont récupérer toutes les minutes
                                });
                            }
                        }); 
                    }
                } catch (Exception e) {
                    System.out.println("No producer found for sensor: " + s.getName());
                }
            });

            // Envoi de l'énergie consommée totale
            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(String.valueOf(totalEnergy[0]));
        } catch (NumberFormatException e) {
            // Renvoie une erreur 404 si la grille n'est pas trouvée
            context.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end("{\"error\": \"Grid not found\"}");
        }

    }
    
}
