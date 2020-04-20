 # Spring Boot: How to design efficient REST API?
 
Resource collections are oftentimes enormous, and when some data has to be retrieved from them, it would be simply not very efficient to always get the full list and browse it for specific items. Therefore we should design an optimized Search API.

A few of the most important features for consuming an API are:
- **Filtering:** 
to narrow down the query results by specific parameters, eg. creation date, or country
```
GET /api/cars?country=USA
GET /api/cars?createDate=2019–11–11
```

- **Sorting:** 
basically allows sorting the results ascending or descending by a chosen parameter or parameters, eg. by date
```
GET /api/cars?sort=createDate,asc
GET /api/cars?sort=createDate,desc
```

- **Paging:**  
uses “size” to narrow down the number of results shown to a specific number, and “offset” to specify which part of the results range to be shown 
— this is important in cases where the number of total results is greater than the one presented, this works like a pagination you may encounter on many websites
Usually, these features are used by adding a so-called query parameter to the endpoint that is being called. 
```
GET /api/cars?limit=100
GET /api/cars?offset=2
```

All together:
```
GET /api/cars?country=USA&sort=createDate,desc&limit=100&offset=2
```
This query should result in the list of 100 cars from the USA, sorted descending by the creation date, and the presented records are on the second page, which means are from a 101–200 record number range.

For more details please see this **[medium post](https://medium.com/quick-code/spring-boot-how-to-design-efficient-search-rest-api-c3a678b693a0)** .

## License
Spring Boot Efficient Search Api Copyright © 2020 by Abderraouf Makhlouf <makhlouf.raouf@gmail.com>
