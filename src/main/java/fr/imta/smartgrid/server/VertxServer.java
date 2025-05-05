package fr.imta.smartgrid.server;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.config.TargetServer;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

public class VertxServer {
    private Vertx vertx;
    private EntityManager db; // database object

    public VertxServer() {
        this.vertx = Vertx.vertx();

        // setup database connexion
        Map<String, String> properties = new HashMap<>();

        properties.put(LOGGING_LEVEL, "FINE");
        properties.put(CONNECTION_POOL_MIN, "1");

        properties.put(TARGET_SERVER, TargetServer.None);

        var emf = Persistence.createEntityManagerFactory("smart-grid", properties);
        db = emf.createEntityManager();
    }

    public void start() {
        Router router = Router.router(vertx);

        router.get("/hello").handler(new ExampleHandler(this.db));

        // differents handlers for grids
        router.get("/grids").handler(new GridsHandler(this.db));
        router.get("/grid/:id").handler(new GridUniqueHandler(this.db));
        router.get("/grid/:id/production").handler(new GridProductionHandler(this.db));

        // differents handlers for persons
        router.get("/persons").handler(new PersonsHandler(this.db));
        router.get("/persons/:id").handler(new GetPersonByIdHandler(this.db));
        
        // start the server
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        new VertxServer().start();
    }
}
