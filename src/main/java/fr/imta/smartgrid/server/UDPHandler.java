package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.SolarPanel;

import io.vertx.core.Handler;
import io.vertx.core.datagram.DatagramPacket;
import jakarta.persistence.EntityManager;
import fr.imta.smartgrid.model.Measurement;


public class UDPHandler implements Handler<DatagramPacket> {

    private final EntityManager db;

    public UDPHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(DatagramPacket packet) {
        String payload = packet.data().toString();
        // Affiche les données reçues
        System.out.println("Incoming UDP data: " + payload);
        // Coupe les données reçues en fonction du caractère ":"
        String[] values = payload.split(":");

        // Récupère les valeurs
        int solar_panel_id = Integer.parseInt(values[0]);
        float temperature = Float.parseFloat(values[1]);
        float power = Float.parseFloat(values[2]);
        Long timestamp = Long.parseLong(values[3]);


        // Récupère la mesure associée à l'ID du capteur donné dans le corps de la requête
        int idMeasureTemperature = (int) db.createNativeQuery("SELECT id FROM measurement WHERE sensor = ? and name = ?")
                .setParameter(1, solar_panel_id)
                .setParameter(2, "temperature")
                .getResultList().get(0);

        Measurement measureTemperature = db.find(Measurement.class, idMeasureTemperature);

        // Récupère la mesure associée à l'ID du capteur donné dans le corps de la requête
        int idMeasurePower = (int) db.createNativeQuery("SELECT id FROM measurement WHERE sensor = ? and name = ?")
                .setParameter(1, solar_panel_id)
                .setParameter(2, "power")
                .getResultList()
                .get(0);

        Measurement measurePower = db.find(Measurement.class, idMeasurePower);

        // Récupère la mesure associée à l'ID du capteur donné dans le corps de la requête
        int idMeasureEnergy = (int) db.createNativeQuery("SELECT id FROM measurement WHERE sensor = ? and name = ?")
                .setParameter(1, solar_panel_id)
                .setParameter(2, "total_energy_produced")
                .getResultList()
                .get(0);

        Measurement measureEnergy = db.find(Measurement.class, idMeasureEnergy);

        db.getTransaction().begin();
        // Créer une nouvelle instance de DataPoint pour la température
        DataPoint dataPointTemperature = new DataPoint();
        dataPointTemperature.setMeasurement(measureTemperature);
        dataPointTemperature.setTimestamp(timestamp);
        dataPointTemperature.setValue(temperature);

        db.persist(dataPointTemperature);
        // Créer une nouvelle instance de DataPoint pour la puissance
        DataPoint dataPointPower = new DataPoint();
        dataPointPower.setMeasurement(measurePower);
        dataPointPower.setTimestamp(timestamp);
        dataPointPower.setValue(power);

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

        // Créer une nouvelle instance de DataPoint pour l'énergie
        DataPoint dataPointEnergy = new DataPoint();
        double energy = (lastEnergy*3600.0 + power * 60.0)/3600.0;
        dataPointEnergy.setMeasurement(measureEnergy);
        dataPointEnergy.setTimestamp(timestamp);
        dataPointEnergy.setValue(energy);
        // Persist the DataPoint object
        db.persist(dataPointEnergy);
        db.getTransaction().commit();

        // Affiche les données reçues
        // Affiche quels points sont sauvegardés
        //System.out.println("Data point for solar panel " + solar_panel_id + " saved: Temperature = " + temperature + ", Power = " + power + ", Timestamp = " + timestamp);
        //System.out.println("Id saved : " + dataPointTemperature.getId() + ", " + dataPointPower.getId() + ", " + dataPointEnergy.getId());
    }
}
