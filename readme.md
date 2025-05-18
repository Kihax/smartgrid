
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

## Avancement du projet

Pour le projet, nous devons finaliser la partie Front-End, notamment la route POST http:///sensor/:id, qui permet la mise à jour des capteurs.

En ce qui concerne le Back-End, tout reste à faire, même si nous avons déjà commencé à écrire la route permettant de récupérer et insérer les données des éoliennes.