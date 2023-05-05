package io.zhpooer.demo

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

class HelloWorldController(
  webClientBuilder: WebClient.Builder,
  @Value("\${application.backend.url}")
  val backendUrl: String
) : Logging {
  val logger = logger()
  private val webClient = webClientBuilder
    .clone()
    .baseUrl(backendUrl)
    .build()

  private suspend fun callbackend(request: ServerRequest): ServerResponse {
    val name = request.queryParam("name").orElseGet { "world" }
    logger.info("Call the backend")
    val response = webClient.get()
      .uri { it.path("/hello").queryParam("name", name).build() }
      .retrieve()
      .awaitBodyOrNull<String>() ?: ""
    return ServerResponse.ok().bodyValue(response).awaitSingle();
  }

  @Bean("HelloWorldControllerRouters")
  private fun routers() = coRouter {
    accept(MediaType.APPLICATION_JSON).nest {
      GET("/api/v1/hello", this@HelloWorldController::callbackend)
    }
  }
}
