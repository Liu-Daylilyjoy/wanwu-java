# Callback Tourism POI Reproduction

Date: 2026-07-05

## Go Source Baseline

Original Go files inspected:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\callback\router.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\model\request\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\model\response\tourism_poi.go`
- `D:\work\week3\wanwu\internal\bff-service\service\tourism_poi.go`
- `D:\work\week3\wanwu\internal\bff-service\service\tourism_poi_test.go`
- `D:\work\week3\wanwu\configs\microservice\mcp-service\configs\tool\tourismpoisearch\openapi.json`

Go exposes `POST /callback/v1/tourism/poi/search`.

The service is deterministic and local: it searches a built-in Dunhuang POI list, filters by category and keyword, filters by radius, sorts by `rating desc, distance asc`, caps `limit` at 20, and returns:

- `query`
- `results`

## Java Reproduction

Java controller:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`

Implemented behavior:

- `location`, `latitude`, `longitude`, `category`, `keyword`, `radiusMeters`, and `limit` are accepted.
- Coordinates take priority over location text.
- Invalid coordinate ranges return frontend error code `1001`.
- Category aliases follow the Go service for English categories: `all`, `attraction`, `hotel`, and `restaurant`.
- Radius defaults to `30000` and caps at `100000`.
- Limit defaults to `10` and caps at `20`.
- Results use Go response fields: `rank`, `id`, `name`, `category`, `categoryLabel`, `rating`, `distanceMeters`, `latitude`, `longitude`, `address`, `description`, `recommendedFor`, `openHours`, `priceLevel`, and `tags`.

The Java dataset uses readable English POI names for Docker-development output while preserving the Go structure and deterministic ranking behavior.

## Verification

Targeted red-green test:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -DfailIfNoTests=false -Dtest=WanwuCallbackApiControllerTest#tourismPoiCallbackReturnsGoCompatibleRanking test
```

