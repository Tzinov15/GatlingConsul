import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random
import scala.concurrent.duration._

class SlamHeartBeating extends Simulation {

	object Search {
		val feeder = Iterator.continually(Map("randomString" -> (Random.alphanumeric.take(10).mkString + "@foo.com")))
		val search = feed(feeder)
				.exec(http("REGISTER")
					.put("/v1/agent/service/register")
		      .body(StringBody("""{"id" : "${randomString}", "Name": "name=${randomString}", "Check" : { "TTL" : "30s" } }""")).asJSON
					.check(status.is(200) )
				)
	    .repeat(500) {
		    exec(http("HEARTBEAT")
		    .get("/v1/agent/check/pass/service:${randomString}")
		    .check(status.is(200))
			).pause(50 milliseconds)
		}
}

	val httpProtocol = http
		.baseURL("http://localhost:8500")
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0")

	val registerService = scenario("SlamHeartBeating").exec(Search.search)

	setUp( registerService.inject(
		rampUsers(50) over (10 seconds))
	).protocols(httpProtocol)
}
