package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CheckoutSimulation extends Simulation {
  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  private val scn = scenario("Checkout Flow")
    .exec(http("Add to cart")
      .post("/api/cart/add")
      .body(StringBody("{\"productId\":1,\"quantity\":1}"))
      .asJson
      .check(status.in(200, 201)))
    .pause(1)
    .exec(http("Checkout")
      .post("/api/checkout")
      .body(StringBody("{\"paymentMethod\":\"COD\"}"))
      .asJson
      .check(status.in(200, 201, 400)))

  setUp(
    scn.inject(rampUsers(50) during (30.seconds))
  ).protocols(httpProtocol)
}
