package io.zhpooer.demo

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

class HttpBinController(webClientBuilder: WebClient.Builder) {

  private val webClient = webClientBuilder
    .clone()
    .baseUrl("https://httpbin.org/")
    .build()

  private suspend fun ping(request: ServerRequest): ServerResponse {
    val respStr = webClient.get()
      .uri("/get")
      .retrieve()
      .awaitBodyOrNull<JsonNode>() ?: ""

    return ServerResponse.ok().bodyValue(respStr).awaitSingle()
  }

  private suspend fun mock(request: ServerRequest): ServerResponse {
    val status = request.pathVariable("status")
    val respStatus = webClient.get()
      .uri("/status/$status")
      .retrieve()
      .awaitBodilessEntity()
      .statusCodeValue

    return ServerResponse.status(respStatus).buildAndAwait()
  }

  @Bean
  private fun routers() = coRouter {
    accept(MediaType.APPLICATION_JSON).nest {
      GET("/api/v1/ping", this@HttpBinController::ping)
      GET("/api/v1/mock/{status}", this@HttpBinController::mock)
    }
  }
}
