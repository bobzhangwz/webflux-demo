package io.zhpooer.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class LoggingFilter(
  @Value("\${management.endpoints.web.base-path}")
  val managementPath: String
) : WebFilter, Logging {
  private val log = logger()

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val request = exchange.request
    val requestInfo = "${request.method} ${request.uri}"
    val isManageEndpoint = request.uri.path.contains(managementPath)

    return chain.filter(exchange).doFirst {
      if(!isManageEndpoint) log.info(requestInfo)
    }.doOnError {
      log.error(requestInfo, it)
    }.doFinally {
      if(!isManageEndpoint) log.info("${exchange.response.statusCode} $requestInfo")
    }
  }
}
