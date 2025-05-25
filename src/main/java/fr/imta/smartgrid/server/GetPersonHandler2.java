package fr.imta.smartgrid.server;
import java.util.List;

import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

// Definition of a handler that returns a JSON description of a person given its id
public class GetPersonHandler2 implements Handler<RoutingContext> {

    // Entity manager to interact with the database
    EntityManager db;

    // Constructor of the class
    public GetPersonHandler2(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        // Retrieving the name of the called route
        String routeCalled = event.currentRoute().getName();

        // Logs to debug request information
        System.out.println("Route called: " + routeCalled);
        System.out.println("Request HTTP method: " + event.request().method());
        System.out.println("We received these query parameters: " + event.queryParams());
        System.out.println("We have these path params: " + event.pathParams());
        System.out.println("We received this body: " + event.body().asString());

        // Retrieving the Person ID from the path parameters
        int idPerson = Integer.parseInt(event.pathParam("id"));

        // Finding the corresponding Person
        Person p = (Person) db.find(Person.class, idPerson);

        // Creating a JSON object to store the response
        JsonObject result = new JsonObject();

        // Checking if the person exists or not
        if (p != null) {
            // Adding the person's parameters to the response
            result.put("id", idPerson);
            result.put("firstname", p.getFirstName());
            result.put("lastname", p.getLastName());
            result.put("grid", p.getGrid().getId());
            
            // SQL request to find list of the sensors owned by the person
            List<Integer> owned_sensors = db
                            .createNativeQuery("SELECT sensor_id FROM person_sensor WHERE person_id = ?")
                            .setParameter(1, idPerson)
                            .getResultList();
            // Adding the list of the owned sensors to the JSON object
            result.put("owned_sensors", owned_sensors);

        } else {
            // In case the person does not exist
            event.fail(404); // Not Found
            return;
        }

        // Returning the JSON object
        event.response()
                    .putHeader("content-type", "application/json")
                    .end(result.encode());
    }
    
}
