package io.zhpooer.demo

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.context.support.beans
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

val allBeans = beans {
  bean({ handler: HelloWorldHandler ->
    coRouter {
      GET("ruok") {
        ok().bodyValue("All good.").awaitSingle()
      }

      accept(MediaType.APPLICATION_JSON).nest {
        GET("/hello_world", handler::helloworld)
      }
    }
  })
  bean<LoggingFilter>()
  bean<CorrelationIdBadgeFilter>()
  bean<HelloWorldHandler>()
  bean<HttpBinController>()
}

class HelloWorldHandler : Logging {
  suspend fun helloworld(request: ServerRequest): ServerResponse {
    logger().info("hello world")
    return ServerResponse.ok().buildAndAwait()
  }
}
