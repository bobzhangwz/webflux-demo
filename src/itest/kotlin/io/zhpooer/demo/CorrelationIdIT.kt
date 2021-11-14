package io.zhpooer.demo

import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CorrelationIdIT(@LocalServerPort val serverPort: Int) {

  @Test
  fun `it should pass correlation id to downstream system`() {
    Given {
      port(serverPort)
      header("x-requestid", "8")
      log().all()
    } When {
      get("/api/v1/ping")
    } Then {
      log().all()
      statusCode(200)
      body("headers.X-Requestid", `is`("8"))
    }
  }

  @Test
  fun `it should generate correlation id to downstream system`() {
    Given {
      port(serverPort)
      log().all()
    } When {
      get("/api/v1/ping")
    } Then {
      log().all()
      statusCode(200)
      body("headers.X-Requestid", not(emptyOrNullString()))
    }
  }
}
