 # Spring Boot: How to design an efficient REST API?
 [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Raouf25_Spring-Boot-efficient-search-API&metric=alert_status)](https://sonarcloud.io/dashboard?id=Raouf25_Spring-Boot-efficient-search-API)
 
Resource collections are often enormous, and when some data has to be retrieved from them, it would be only not very efficient to always get the full list and browse it for specific items. Therefore we should design an optimized Search API.

- **Filtering:**
  - Narrow down query results by parameters (e.g., country, creation date).
    ```bash
    GET /api/cars?country=Japan
    GET /api/cars?createDate=2019-11-11
    ```

- **Sorting:**
  - Sort results ascending or descending by chosen parameters (e.g., by date).
    ```bash
    GET /api/cars?sort=createDate,asc
    GET /api/cars?sort=createDate,desc
    ```

- **Paging:**
  - Use "limit" to restrict results and "offset" to specify which part of the result range to show.
    ```bash
    GET /api/cars?limit=100
    GET /api/cars?offset=2
    ```

Combine features:
```bash
GET /api/cars?country=Japan&sort=createDate,desc&limit=100&offset=2
```
Results in the list of 100 cars from Japan, sorted by creation date in descending order, starting from the second page (records 101-200).

### How to Run the Project

##### Clone source code from git
```bash
$ git clone https://github.com/Raouf25/Spring-Boot-efficient-search-API.git
```

##### Build Docker image
```bash
$ docker build -t="spring-boot-efficient-search-api" --force-rm=true .
```
This command runs Maven build to create a JAR package and builds the Docker image.

*Note: Initial command may take time to download the base image from [DockerHub](https://hub.docker.com/)*

##### Run Docker Container
```bash
$ docker run -p 8080:8080 -it --rm spring-boot-efficient-search-api
```

##### Test Application
```bash
$ curl localhost:8080/api/cars/1
```
Response:
```json
{
   "id":1,
   "manufacturer":"Acura",
   "model":"Integra",
   "type":"Small",
   "country":"Japan",
   "createDate":"1931-02-01"
}
```

##### Stop Docker Container
```bash
docker stop $(docker container ls | grep "spring-boot-efficient-search-api:*" | awk '{ print $1 }')
```

## Live Demo
This project is deployed on https://spring-boot-efficient-search-api.fly.dev/api/cars

Try: [Demo Link](https://spring-boot-efficient-search-api.fly.dev/api/cars?country=Japan&sort=createDate,desc&limit=100&offset=2)

## Docker Image Repository
Docker Image: [raouf25/spring-boot-efficient-search-api](https://hub.docker.com/r/raouf25/spring-boot-efficient-search-api)

Refer to the [Docker Commands](https://hub.docker.com/r/raouf25/spring-boot-efficient-search-api) section for pulling, running, and querying the API using cURL and jq.

## License
For more details please see this **[medium post](https://medium.com/quick-code/spring-boot-how-to-design-efficient-search-rest-api-c3a678b693a0)** .

Spring Boot Efficient Search Api Copyright © 2020 by Abderraouf Makhlouf <makhlouf.raouf@gmail.com>
