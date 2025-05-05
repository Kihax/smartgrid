package fr.imta.smartgrid.server;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridProductionHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GridProductionHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        try {
            int gridId = Integer.parseInt(event.pathParam("id"));
            
            Grid g;
            
            try {
                g = (Grid) db
                    .createNativeQuery("SELECT * FROM grid WHERE id = ?", Grid.class)
                    .setParameter(1, gridId)
                    .getSingleResult();

                List<Person> persons = g.getPersons();
                List<Sensor> sensors = g.getSensors();

                sensors.forEach(sensor -> {
                    System.out.println(sensor.getId());
                    System.out.println(sensor.getName());
                    sensor.getMeasurements().forEach(measurement -> {
                        measurement.getDatapoints().forEach(dataPoint -> {
                            System.out.println(dataPoint.getValue() + " " + measurement.getUnit());
                        });
                    });
                });
        } catch (NumberFormatException e) {
            event.end("404 Not Found: "); 
            return; 
        }

        


    }
    
}
