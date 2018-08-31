# Recent Order Service

A challenge for processing many concurrent requests.

## Requirements

* Save and retrieve must be constant time and memory
* No processes outside of a request
* Must report the last 30s of orders based on an input timestamp (default to "now")
* Must report to millisecond precision. E.g., if "2018-09-25T07:30:04.871Z" is the timestamp of an order, queries for: ["2018-08-23T07:30:04.871Z", "2018-08-23T07:30:34.870Z"] should report the order. Queries including "2018-08-23T07:30:34.871Z" and beyond should not report the order
* Must process 2,000,000 total requests across 10 concurrent clients fast enough to return statistics including them all
* Must provide an HTTP API with 3 endpoints - `POST /orders`, `GET /reports/orders-last-30s`, `DELETE /orders`
* Must run with Maven on a single JVM limited by the following flags: `-Xms2g -Xmx2g`

## To Run `MaxOrdersResourceTest`

Need to have two terminal sessions positioned in the root directory of this repository.

In one terminal bundle and start the application:

```
MAVEN_OPTS="-Xmx500m -Xms500m" mvn -DskipTests clean package && \
java -jar target/recent-order-service-1.0.jar server environment.yml
```

In another terminal execute the test:

```
MAVEN_OPTS="-Xmx500m -Xms500m" mvn test -Dtest=MaxOrdersResourceTest
```
