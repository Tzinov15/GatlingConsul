
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class LocalExpress extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:3000")
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:47.0) Gecko/20100101 Firefox/47.0")

	val headers_1 = Map("Accept" -> "*/*")



	val scn = scenario("LocalExpress")
		.exec(http("HomePage")
			.get("/")
			.check(status.is(200))
		)

	setUp(scn.inject(rampUsersPerSec(1) to (500) during(20 seconds))).protocols(httpProtocol)
}
