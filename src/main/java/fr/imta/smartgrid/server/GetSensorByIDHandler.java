package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GetSensorByIDHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GetSensorByIDHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext context) {

        // Récupère l'indentifiant du capteur à partir de l'URL
        try {
            int sensorId = Integer.parseInt(context.pathParam("id"));
            
            // Exécute une requête SQL pour récupérer le capteur correspondant à l'identifiant
            Sensor s = (Sensor) db
                    .createNativeQuery("SELECT * FROM sensor WHERE id = ?", Sensor.class)
                    .setParameter(1, sensorId)
                    .getSingleResult();

            // Création d'un objet JSON pour stocker les informations du capteur
            JsonObject res = new JsonObject();

            res.put("id", s.getId());
            res.put("name", s.getName());
            res.put("description", s.getDescription());
            res.put("kind", s.getDtype());
            
            if (s.getGrid() != null) {
                res.put("grid", s.getGrid().getId()); // Ajout de la grille au JSON
            }

            // Récupération des mesures associées au capteur
            res.put("available_measurements", s.getMeasurements().stream().map(Measurement::getId).toList());
            // Récupération des utilisateurs associés au capteur
            res.put("owners", s.getOwners().stream().map(Person::getId).toList());


            try {
                // Récupération de la source d'énergie si il s'agit d'un producteur
                String powerSource = (String) db.createNativeQuery("SELECT power_source FROM producer WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();

                res.put("power_source", powerSource); // Ajout de la source d'énergie au JSON
            } catch (Exception e) {}

            try {
                // Récupération de la puissance maximale si il s'agit d'un consommateur
                double maxPower = (double) db.createNativeQuery("SELECT max_power FROM consumer WHERE id = ?")
                .setParameter(1, sensorId)
                .getSingleResult();

                res.put("max_power", maxPower);
            } catch (Exception e) {}

            try {
                // Récupération de la puissance maximale si il s'agit d'un panneau solaire
                double efficiency = (double) db.createNativeQuery("SELECT efficiency FROM solar_panel WHERE id = ?")
                                         .setParameter(1, sensorId)
                                         .getSingleResult();
                res.put("efficiency", efficiency);
            } catch (Exception e) {}

            try {
                // Récupération de la hauteur et de la longeur des pales si il s'agit d'une éolienne
                Object[] windTurbineData = (Object[]) db.createNativeQuery("SELECT height, bladelength FROM wind_turbine WHERE id = ?")
                    .setParameter(1, sensorId)
                    .getSingleResult();
                if (windTurbineData != null && windTurbineData.length == 2) {
                    res.put("height", windTurbineData[0]);
                    res.put("blade_length", windTurbineData[1]);
                }
            } catch (Exception e) {}

            try {
                // Récupération de la tension, de l'ampérage maximum et du type de connecteur si il s'agit d'une borne de recharge
                Object[] evChargerData = (Object[]) db.createNativeQuery("SELECT voltage, maxamp, connector_type FROM ev_charger WHERE id = ?")
                    .setParameter(1, sensorId)
                    .getSingleResult();
                
                if (evChargerData != null && evChargerData.length == 3) {
                    res.put("type", evChargerData[2]);
                    res.put("maxAmp", evChargerData[1]);
                    res.put("voltage", evChargerData[0]);
                }

            } catch (Exception e) {}

            // Envoi de la réponse au format JSON
            context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(200)
                .end(res.toString());
        } catch (Exception e) {
            context.response().setStatusCode(404).end("{\"error\": \"Sensor not found\"}");
        }

    }
    
}
