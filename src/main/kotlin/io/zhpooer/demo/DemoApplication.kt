package io.zhpooer.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
  runApplication<DemoApplication>(*args)
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
  override fun initialize(applicationContext: GenericApplicationContext) {
    listOf(
      allBeans
    ).forEach { it.initialize(applicationContext) }
  }
}
