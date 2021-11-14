package io.zhpooer.demo

import org.springframework.cloud.sleuth.Tracer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdBadgeFilter(private val tracer: Tracer) : WebFilter, Logging {
  private val correlationIdKey = "x-requestid"

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    return chain.filter(exchange).doFirst {
      if (tracer.getBaggage(correlationIdKey)?.get().isNullOrBlank()) {

        val requestId = exchange.request.headers["x-request-id"]?.firstOrNull() ?: UUID.randomUUID().toString()
        tracer.createBaggage(correlationIdKey, requestId)
        logger().debug("Generate correlationId automatically!")
      }
    }
  }
}
