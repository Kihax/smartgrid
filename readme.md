
## Auteurs

Killian Kerlau, Lounis Hamitouche, Mélina Wang

## Pre-requis

- un JRE pour faire tourner gradle
- Docker : guide d'installation [ici](https://docs.docker.com/engine/install/)

## Mise en place

- Placez vous dans ce répertoire et exécutez la commande `docker compose up -d` pour lancer le serveur postgresql
- Lancez le projet avec `./gradlew.bat run` (utilisez `gradlew` sur macOS / linux)

Le backend est accessible sur le port `8080`, le frontend est accessible [ici](http://localhost:8082).
Une interface web pour administrer la base de données est accessible [ici](http://localhost:8081), sélectionnez `PostgreSQL` comme système, `db` comme serveur et `test` comme utilisateur/mot de passe/base de données. 

## Compte rendu

Toutes les routes ont été mises en place.

### Test

Nous avons d'abord, fait les tests avec le navigateur google chrome pour les requêtes GET. Toutefois, les requêtes de type POST, PUT, DELETE ne fonctionnait pas avec les liens proposés sur le site https://hub.imt-atlantique.fr/ueinfo-fise1a/ , or comme un des membres du groupe connaissait les requetes de type curl, nous sommes passés par cette méthode : dans le cmd : `curl -X POST localhost:8080/source`. Or, nous nous sommes rendu compte que cette méthode prennait du temps et nous sommes passé par un outil pour les tests suivant : **HTTPIE** qui permet de faire n'importe quel type de requête HTTP, passer des paramètres et passer des informations dans le body. 

### Difficultés et questionnement 

Pour l'url POST http://localhost:8080/sensor/:id nous ne savions pas si il était possible de changer la `power_source` car cela implique de changer les paramètres comme `blade_length` or il est demandé de ne pas changer ce genre de paramètres. Donc il est actuellement possible de changer `power_source` mais nous ne faisons pas toutes les modifications que cela nécessiterait après.

### Utilisation de l'IA

Pour ce projet, nous avons un peu utilisé l'IA, notamment lorsqu'il s'agissait de parser le body pour l'utiliser dans POST http://localhost:8080/sensor/:id et POST http://localhost:8080/person/:id , etc ... Nous ne savions pas comment faire et l'IA nous a apporté une réponse, solution qui nous a ensuite été présentée deux séances plus tard.


Pour les tests avec curl, nous savions que cette commande existait, toutefois nous n'avions plus en tête la synthaxe des commandes donc nous avons utilisés chat gpt pour qu'il nous écrive les requêtes.