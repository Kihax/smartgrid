package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class PostWindTurineIngressHandler implements Handler<RoutingContext> {
    EntityManager db;

    // Constructeur
    public PostWindTurineIngressHandler(EntityManager db) {
        this.db = db;
    }

    // Méthode principale
    @Override
    public void handle(RoutingContext context) {
        
        // Récupère le corps JSON de la requête et renvoie une erreur 500 si le format n'est pas correct
        JsonObject body;
        try {
            body = context.body().asJsonObject(); 
        } catch (Exception e) {
            context.response()
                .setStatusCode(500) 
                .end("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        // Récupère la turbine associé à l'ID donné dans le corps de la requête
        WindTurbine windTurbine;

        try {
            // Récupère l'objet WindTurbine correspondant à l'ID donné dans le corps de la requête
            windTurbine = db.find(WindTurbine.class, body.getInteger("windturbine"));

            // Vérifie si l'objet WindTurbine existe
            if (windTurbine == null) {
                context.response()
                    .setStatusCode(404) 
                    .end("{\"error\": \"No find turbine found\"}");
                return;
            }
        } catch (Exception e) {
            context.response()
                .setStatusCode(404) 
                .end("{\"error\": \"No find turbine found\"}");
            return;
        }

        // Vérifie si le corps de la requête contient les champs requis
        int id;
        String timestamp;
        JsonObject data;
        int speed;
        int power;
        try {
            id = body.getInteger("windturbine");
            timestamp = body.getString("timestamp");
            data = body.getJsonObject("data");
            speed = data.getInteger("speed");
            power = data.getInteger("power");

            // vérifie si les champs sont valides
            if (timestamp == null || speed < 0 || power < 0) {
                context.response()
                    .setStatusCode(500)
                    .putHeader("content-type", "application/json") 
                    .end("{\"error\": \"Invalid data\"}");
                return;
            }
        } catch (Exception e) {
            context.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end("{\"error\": \"Missing required fields\"}");
            return;
        }

        // Nous cherchons l'id des mesures associées à l'id du capteur donné dans le corps de la requête et aux différentes mesures

        int idMeasureSpeed = (int) db.createNativeQuery("SELECT id FROM measurement WHERE sensor = ? and name = ?")
                .setParameter(1, id)
                .setParameter(2, "speed")
                .getResultList().get(0);

        Measurement measureSpeed = db.find(Measurement.class, idMeasureSpeed);


        int idMeasurePower = (int) db.createNativeQuery("SELECT id FROM measurement WHERE sensor = ? and name = ?")
                .setParameter(1, id)
                .setParameter(2, "power")
                .getResultList()
                .get(0);

        Measurement measurePower = db.find(Measurement.class, idMeasurePower);

        int idMeasureEnergy = (int) db.createNativeQuery("SELECT id FROM measurement WHERE sensor = ? and name = ?")
                .setParameter(1, id)
                .setParameter(2, "total_energy_produced")
                .getResultList()
                .get(0);

        Measurement measureEnergy = db.find(Measurement.class, idMeasureEnergy);

        db.getTransaction().begin();

        // Create a new DataPoint object for Speed
        DataPoint dataPointSpeed = new DataPoint();
        dataPointSpeed.setMeasurement(measureSpeed);
        dataPointSpeed.setTimestamp(Long.parseLong(timestamp));
        dataPointSpeed.setValue(speed);

        // Persist the DataPoint object
        db.persist(dataPointSpeed);

        DataPoint dataPointPower = new DataPoint();
        dataPointPower.setMeasurement(measurePower);
        dataPointPower.setTimestamp(Long.parseLong(timestamp));
        dataPointPower.setValue(power);
        
        // Persist the DataPoint object
        db.persist(dataPointPower);

        double lastEnergy = 0.;

        try {
            // Récupère la dernière valeur de la mesure d'énergie
            lastEnergy = (double) db.createNativeQuery("SELECT value FROM datapoint WHERE measurement = ? ORDER BY timestamp DESC LIMIT 1")
                    .setParameter(1, idMeasureEnergy)
                    .getSingleResult();
        } catch (Exception e) {
            // Si aucune valeur n'est trouvée, on initialise lastEnergy à 0
            lastEnergy = 0.;
        }

        // Crée un nouvel objet DataPoint pour l'énergie
        DataPoint dataPointEnergy = new DataPoint();
        double energy = (lastEnergy*3600.0 + power * 60.0)/3600.0;
        dataPointEnergy.setMeasurement(measureEnergy);
        dataPointEnergy.setTimestamp(Long.parseLong(timestamp));
        dataPointEnergy.setValue(energy);

        // Persist the DataPoint object
        db.persist(dataPointEnergy);

        // Commit the transaction
        db.getTransaction().commit();
        
        // Envoie une réponse JSON avec le code 200 et les données de la turbine
        context.response()
            .setStatusCode(200) 
            .putHeader("content-type", "application/json")
            .end("{\"status\": \"success\"}");
    }
}
