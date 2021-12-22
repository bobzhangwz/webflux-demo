package io.zhpooer.demo

import kotlinx.coroutines.reactor.mono
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import reactor.core.publisher.Mono

class HttpBinHealthIndicator : ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return mono {
      Health.Builder().down().build()
    }
  }
}
