package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Producer;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GetGridProductionHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetGridProductionHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        try {
            int gridId = Integer.parseInt(context.pathParam("id")); // Récupération de l'id de la grille et transformation en int
            
            // Récupération de la grille dans la base de données
            Grid g = (Grid) db
                    .createNativeQuery("SELECT * FROM grid WHERE id = ?", Grid.class)
                    .setParameter(1, gridId)
                    .getSingleResult();  

            // Récupération de la liste des capteurs de la grille
            List<Sensor> sensors = g.getSensors();

            // Initialisation de la variable pour stocker l'énergie totale
            final float[] totalEnergy = {0};

            // Parcours de la liste des capteurs
            sensors.forEach(s -> {
                    
                try {

                    Producer result = db.find(Producer.class, s.getId());

                    if(result != null) { // Vérification si le capteur est un producteur
                        s.getMeasurements().forEach(m -> {
                            if(m.getName().equals("power")) { // Only print power measurements
                                m.getDatapoints().forEach(d -> {
                                    totalEnergy[0] += d.getValue() * 60.0; // en Joules
                                    System.out.println("Sensor: " + s.getName() + ", Measurement: " + m.getName() + ", DataPoint: " + d.getValue());
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
