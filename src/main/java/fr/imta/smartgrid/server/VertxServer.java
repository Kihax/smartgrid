package fr.imta.smartgrid.server;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.LOGGING_LEVEL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import org.eclipse.persistence.config.TargetServer;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

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

        // Route concernant les grilles
        router.get("/grids").handler(new GetGridsHandler(this.db)); // Retourne un JSON contenant la liste des grilles
        router.get("/grid/:id").handler(new GetGridByIDHandler(this.db)); // Retourne un JSON contenant les informations de la grille
        router.get("/grid/:id/production").handler(new GetGridProductionHandler(this.db)); // Retourne un JSON contenant les informations de production de la grille
        router.get("/grid/:id/consumption").handler(new GetGridConsumptionHandler(this.db)); // Retourne un JSON contenant les informations de consommation de la grille

        // Route pour la gestion des utilisateurs
        router.get("/persons").handler(new GetPersonsHandler(this.db)); // Renvoie la liste des utilisateurs
        router.get("/person/:id").handler(new GetPersonByIDHandler(this.db)); // Renvoie les informations d'un utilisateur
        router.post("/person/:id") // Modifie un utilisateur dans la base de données 
                .handler(BodyHandler.create()) // nécessaire pour parser le corps de la requête et l'utiliser dans le handler PostPersonIDHandler
                .handler(new PostPersonIDHandler(this.db));
        router.put("/person")  // Ajoute un utilisateur dans la base de données
                .handler(BodyHandler.create()) // nécessaire pour parser le corps de la requête et l'utiliser dans le handler
                .handler(new PutPersonHandler(this.db)); 
        router.delete("/person/:id").handler(new DeletePersonHandler(this.db)); // Supprime un utilisateur de la base de données

        // Route pour la gestion des capteurs
        router.get("/sensor/:id").handler(new GetSensorByIDHandler(this.db)); // Renvoie les informations d'un capteur
        router.get("/sensors/:kind").handler(new GetSensorsByKindHandler(this.db)); // Renvoie la liste des capteurs d'un type donné
        router.get("/producers").handler(new GetSensorsProducerHandler(this.db)); // Renvoie la liste des producteurs
        router.get("/consumers").handler(new GetSensorsConsumersHandler(this.db)); // Renvoie la liste des consommateurs
        router.post("/sensor/:id") // Modifie un capteur dans la base de données
                .handler(BodyHandler.create())  // nécessaire pour parser le corps de la requête et l'utiliser dans le handler
                .handler(new PostSensorIDHandler(this.db));

        // Route pour afficher les mesures
        router.get("/measurement/:id").handler(new GetMeasurementByIDHandler(this.db)); // Renvoie les informations d'une mesure
        router.get("/measurement/:id/values").handler(new GetMeasurementByIDValuesHandler(this.db)); // Renvoie les valeurs d'une mesure

        // Partie backend pour la persistance des données
        router.get("/ingress/windturbine")
                .handler(BodyHandler.create()) // nécessaire pour parser le corps de la requête et l'utiliser dans le handler
                .handler(new WindTurineIngressHandler(this.db));
        
        // démarre le serveur HTTP sur le port 8080
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        new VertxServer().start();
    }
}
