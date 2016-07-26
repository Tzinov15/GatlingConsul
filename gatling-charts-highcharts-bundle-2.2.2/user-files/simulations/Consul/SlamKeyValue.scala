import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random
import scala.concurrent.duration._

// Any class that extends the Simulation class will be provided as a potential Scenario to
class SlamKeyValue extends Simulation {

	object Search {
		val feeder = Iterator.continually(Map("randomString" -> (Random.alphanumeric.take(200).mkString + "___KEYVALUE")))
		// This will generate 100 random strings of length 200 (see above) and then make 100 PUT requests. These 100 PUT requests will create 100 Key-Value pairs in Consul
		val search = repeat(100) {
					feed(feeder)
					.exec(http("KEY-VALUE-PUT")
					.put("/v1/kv/${randomString}")
					.body(StringBody("this is some random data with the only goal of taking up space on the disk. It will be encoded in base 64 by consul anyways"))
					.check(status.is(200))
				)
		}
}

	val httpProtocol = http
		// Set the base path to correspond to where Consul is listening
		.baseURL("http://localhost:8500")
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0")

	val registerService = scenario("SlamKeyValue").exec(Search.search)

	// Inject 100 users per second where each user will be making 100 PUT requsts for consul KeyValue pairs. This will result in 100,000 PUT requests to Consul by the end of the test
	setUp( registerService.inject(
		rampUsers(1000) over(10 seconds))
	).protocols(httpProtocol)
}
