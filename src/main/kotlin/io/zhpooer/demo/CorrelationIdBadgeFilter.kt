package io.zhpooer.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.sleuth.Tracer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.*

@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdBadgeFilter(
  private val tracer: Tracer,
  @Value("\${application.correlation-id-key}")
  val correlationIdKey: String
) : WebFilter, Logging {
  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    if (tracer.getBaggage(correlationIdKey)?.get().isNullOrBlank()) {
      val requestId = exchange.request.headers["x-request-id"]?.firstOrNull() ?: UUID.randomUUID().toString()
      tracer.createBaggage(correlationIdKey, requestId)
      logger().debug("Generate correlationId automatically!")
    }
    tracer.getBaggage(correlationIdKey)?.get()?.let {
      exchange.response.headers.add("X-Correlation-Id", it)
    }
    return chain.filter(exchange)
  }
}
