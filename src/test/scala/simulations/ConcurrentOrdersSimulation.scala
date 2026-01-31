package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ConcurrentOrdersSimulation extends Simulation {
  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  private val scn = scenario("Concurrent Order Placement")
    .exec(http("Place order")
      .post("/api/orders")
      .body(StringBody("{\"items\":[{\"productId\":1,\"quantity\":2}]}"))
      .asJson
      .check(status.in(200, 201, 400)))

  setUp(
    scn.inject(atOnceUsers(30))
  ).protocols(httpProtocol)
}
