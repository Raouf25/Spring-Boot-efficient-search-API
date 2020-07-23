 # Spring Boot: How to design efficient REST API?
 
Resource collections are oftentimes enormous, and when some data has to be retrieved from them, it would be simply not very efficient to always get the full list and browse it for specific items. Therefore we should design an optimized Search API.

A few of the most important features for consuming an API are:
- **Filtering:** 
to narrow down the query results by specific parameters, eg. creation date, or country
```
GET /cars?country=USA
GET /cars?creation_date=2019–11–11
```

- **Sorting:** 
basically allows sorting the results ascending or descending by a chosen parameter or parameters, eg. by date
```
GET /cars?sort=creation_date,asc
GET /cars?sort=creation_date,desc
```

- **Paging:**  
uses “size” to narrow down the number of results shown to a specific number, and “offset” to specify which part of the results range to be shown 
— this is important in cases where the number of total results is greater than the one presented, this works like a pagination you may encounter on many websites
Usually, these features are used by adding a so-called query parameter to the endpoint that is being called. 
```
GET /cars?limit=100
GET /cars?offset=2
```

All together:
```
GET /cars?country=USA&sort=creation_date:desc&limit=100&offset=2
```
This query should result in the list of 100 cars from the USA, sorted descending by the creation date, and the presented records are on the second page, which means are from a 101–200 record number range.

### How to run the project

##### Clone source code from git
```
$  git clone https://github.com/Raouf25/Spring-Boot-efficient-search-API.git 
```

##### Build Docker image
```
$  docker build -t="spring-boot-efficient-search-api" --force-rm=true .
```
This will first run maven build to create jar package and then build hello-world image using built jar package.

>Note:if you run this command for first time it will take some time in order to download base image from [DockerHub](https://hub.docker.com/)

##### Run Docker Container
```
$ docker run -p 8080:8080 -it --rm spring-boot-efficient-search-api
```

##### Test application

```
$  curl localhost:8080/api/cars/1
```

the respone should be:
```json
{
   "id":1,
   "manufacturer":"Acura",
   "model":"Integra",
   "type":"Small",
   "country":"Japon",
   "createDate":"1931-02-01"
}
```

#####  Stop Docker Container:
```
docker stop `docker container ls | grep "spring-boot-efficient-search-api:*" | awk '{ print $1 }'`
```

## License
For more details please see this **[medium post](https://medium.com/quick-code/spring-boot-how-to-design-efficient-search-rest-api-c3a678b693a0)** .

Spring Boot Efficient Search Api Copyright © 2020 by Abderraouf Makhlouf <makhlouf.raouf@gmail.com>
