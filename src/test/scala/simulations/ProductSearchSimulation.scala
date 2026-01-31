package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ProductSearchSimulation extends Simulation {
  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  private val scn = scenario("Product Search Load Test")
    .exec(http("List products")
      .get("/api/v2/products?page=0&size=20")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Search products")
      .get("/api/v2/products/search?query=cement")
      .check(status.in(200, 204)))

  setUp(
    scn.inject(rampUsers(100) during (30.seconds))
  ).protocols(httpProtocol)
}
