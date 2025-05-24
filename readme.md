
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

Nous avons d'abord fait les tests avec le navigateur Google Chrome pour les requêtes GET. 
Toutefois, les requêtes de type POST, PUT, DELETE ne fonctionnaient pas avec les liens proposés sur le site https://hub.imt-atlantique.fr/ueinfo-fise1a/.

Un des membres du groupe connaissant les requêtes `curl`, nous avons utilisé cette méthode :  
`curl -X POST localhost:8080/source`. 

Cependant, cette méthode s’est révélée fastidieuse. Nous avons alors utilisé **HTTPIE**, un outil plus ergonomique permettant de tester tous les types de requêtes HTTP, de passer des paramètres et d’ajouter des données dans le body.

### Difficultés et questionnements

Concernant l'URL POST `http://localhost:8080/sensor/:id`, nous nous sommes interrogés sur la possibilité de modifier le champ `power_source`. 

Changer cette propriété impliquerait également la modification de champs dépendants comme `blade_length`, ce qui est contraire aux consignes. 

Actuellement, la modification de `power_source` est possible, mais sans ajustement automatique des autres paramètres liés.

Concernant certains codes, les résultats obtenus ne correspondent pas exactement à ceux affichés sur le site, notamment pour les codes calculant la consommation ou la production totale d’un capteur, ainsi que ceux affichant les mesures disponibles.

### Utilisation de l'IA

Nous avons utilisé une IA (comme ChatGPT) pour comprendre comment parser le body des requêtes dans les endpoints POST, par exemple pour `/sensor/:id` ou `/person/:id`.

Elle nous a permis de structurer correctement les données, solution qui a été abordée en cours deux séances plus tard.

Nous avons également utilisé l’IA pour rédiger des commandes `curl`, lorsque nous ne nous rappelions plus de la syntaxe exacte.