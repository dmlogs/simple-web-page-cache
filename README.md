# Simple Web Page Cache

A simple web service for caching web pages powered by Jersey/Grizzly.

### Build Executable Jar

```
mvn package
```

### Run Application

#### Execute via Maven
```
mvn exec:java [-Dexec.args="[args]"]
```

#### Execute via Java Command Line

```
# Package shaded Jar with Maven
mvn package

# Execute with java
java -jar target/simple-web-page-cache-1.0-SNAPSHOT.jar [args]
```

#### Optional Command Line Arguments

Command line arguments must be formatted 'key=value'.

- **maxCacheSize**: The maximum number of items to store in the cache. (Default = 0; no limit)
- **ttl**: Time to live in milliseconds. Cache records expire after TTL. This is controlled by a cleanup process which executes on interval. (Default = 0; infinite)
- **updateCacheInterval**: The frequency at which the cleanup process will run in milliseconds. The cleanup process only executes if TTL is set to value other than 0. (Default = 30000; 30 seconds.)
- **port**: The port to reserve for the Grizzly HttpServer.  (Default = 8080)
- **authenticationEnabled**: Enabled authentication. The api will expect Authorization header. (Default = false)
- **user**: String to assert the proper user is authenticating. (Default = "user")
- **password**: String to assert the proper password is provided. (Default = "password")

Example:

```
 java -jar simple-web-page-cache-1.0-SNAPSHOT.jar maxCacheSize=100
```

## Web Service

WADL Definition is provided after start up via /application.wadl

- #### /cache
  - Method: GET
  - Parameters: None.
  - Response Media-Type: text/html

  Returns web page containing cache statistics.

- #### /cache
  - Method: POST
  - Parameters:
      - url: query paramater, the url to fetch and cache. [example: http://www.google.com]
  - Response Media-Type: text/plain

  Given an HTTP url, the service makes a GET request and stores the body and headers to the Cache.
  Returns ID of content stored to cache.
  ID is a random unique identifier.

- #### /cache/{id}
  - Method: GET
  - Parameters:
      - ID: path parameter, the unique identifier of an item stored in the cache.
  - Response Media-Type: text/html

  Given an ID returns the content that is associated with the ID as an appropriate response with the original body and headers.

## Authentication

If authentication is enabled the service will filter requests to assert any request not to the "application.wadl" path is authorized.

Authentication expects a basic authorization header containing "[user]:[password]" Base64 encoded.

Example:
```
# Where 'dXNlcjpwYXNzd29yZA==' is "user:password" Base64 encoded.
Authorization: Basic dXNlcjpwYXNzd29yZA==
```


## Caching and Persistant Storage

Only ephemeral storage is implemented.

Persistant storage could be handled in several ways, and would vary depending on the goal.

For instance items could be stored both in ephemeral and persistant storage, but only expire from the ephemeral cache and on a cache miss be retrieved from persistant storage.

A simple method to implement persistant storage could be to write out each cache item to a formatted file with the file name as the unique identifier. Methods for adding and expiring items in the cache would then be disk I/O operations.

A configuration could be added to switch between ephemeral or persistant storage, or alternatively both in the case that persistant storage is used to retrieve items on a cache miss from ephemeral.

Another enhancement to consider for either ephemeral or persistant storage is compression of the cached data.

## Manual Web Service Examples
```
curl http://localhost:8080/cache
curl http://localhost:8080/cache?url=http://www.google.com -X POST
curl http://localhost:8080/cache/f3b46fb8-d5cd-450e-a4eb-0886f739c0a7
```
