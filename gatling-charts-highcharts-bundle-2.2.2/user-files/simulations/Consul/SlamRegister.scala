import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random
import scala.concurrent.duration._

class SlamRegister extends Simulation {


	object Search {
		val feeder = Iterator.continually(Map("randomString" -> (Random.alphanumeric.take(10).mkString + "@foo.com")))
		// Generate a random service ID and register a new service with a dead-man switch TTL of 30 seconds
		val search =
				feed(feeder)
				.exec(http("REGISTER")
					.put("/v1/agent/service/register")
		      .body(StringBody("""{"id" : "${randomString}", "Name": "name=${randomString}", "Check" : { "TTL" : "30s" } }""")).asJSON
					.check(status.is(200) )
				)
}

	val httpProtocol = http
		.baseURL("http://localhost:8500")
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0")

	val registerService = scenario("SlamRegister").exec(Search.search)

	// Inject users at once in order to simulate a bunch of services trying to register consul at the same time.
	setUp( registerService.inject(
		rampUsers(500) over(3 second))
	).protocols(httpProtocol)
}
