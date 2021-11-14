package io.zhpooer.demo

import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class LoggingFilter : WebFilter, Logging {
  private val log = logger()

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val request = exchange.request
    val requestInfo = "${request.method} ${request.uri}"

    return chain.filter(exchange).doFirst {
      log.info(requestInfo)
    }.doOnError {
      log.error(requestInfo, it)
    }.doFinally {
      log.info("${exchange.response.statusCode} $requestInfo")
    }
  }
}
