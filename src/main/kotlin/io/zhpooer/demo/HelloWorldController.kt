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
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider
import software.amazon.awssdk.services.s3.S3Client

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
    return ServerResponse.ok().bodyValue(response).awaitSingle()
  }

  private suspend fun testS3(request: ServerRequest): ServerResponse {
    val s3Client = S3Client.builder()
      .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
      .build();

    s3Client.listBuckets().buckets().forEach { logger.info("Bucket: ${it.name()}") }

    return ServerResponse.ok().build().awaitSingle();
  }

  @Bean("HelloWorldControllerRouters")
  private fun routers() = coRouter {
    accept(MediaType.APPLICATION_JSON).nest {
      GET("/api/v1/hello", this@HelloWorldController::callbackend)
      GET("/api/v1/s3", this@HelloWorldController::testS3)
    }
  }
}
