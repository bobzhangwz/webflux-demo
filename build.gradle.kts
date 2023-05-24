import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.streams.toList

val excludesFiles = listOf("io.zhpooer.demo.*").map { it.replace(".", "/") }

plugins {
	id("org.springframework.boot") version "2.7.10"
	id("io.spring.dependency-management") version "1.1.0"

	id("com.gorylenko.gradle-git-properties") version "2.2.4"
  id("co.uzzu.dotenv.gradle") version "1.1.0"
  id("com.diffplug.spotless") version "5.17.0"
  id("de.undercouch.download") version "5.4.0"

	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"

  jacoco
  application
}

val appClass = "io.zhpooer.demo.DemoApplicationKt"
application { mainClass.set(appClass) }

group = "io.zhpooer"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

extra["springCloudVersion"] = "2021.0.7"

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.7")
    mavenBom("org.springframework.cloud:spring-cloud-sleuth-otel-dependencies:1.1.3")
  }
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone/") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	runtimeOnly("io.micrometer:micrometer-core")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	implementation("org.springframework.cloud:spring-cloud-starter-sleuth") {
		exclude("org.springframework.cloud", "spring-cloud-sleuth-brave")
	}
	implementation("org.springframework.cloud:spring-cloud-sleuth-instrumentation")
	implementation("org.springframework.cloud:spring-cloud-sleuth-otel-autoconfigure")
  implementation("io.opentelemetry:opentelemetry-exporter-otlp")
//	runtimeOnly("io.opentelemetry:opentelemetry-exporter-jaeger")
	runtimeOnly("io.grpc:grpc-netty-shaded:1.41.0")
	runtimeOnly("io.grpc:grpc-protobuf:1.41.0")
	runtimeOnly("io.grpc:grpc-stub:1.41.0")

  implementation(platform("software.amazon.awssdk:bom:2.20.56"))
  implementation("software.amazon.awssdk:s3")

	runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
  runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
  runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.3")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testImplementation("io.kotest:kotest-runner-junit5:4.6.0")
  testImplementation("io.kotest:kotest-assertions-core:4.6.0")
  testImplementation("io.kotest:kotest-runner-junit5:4.6.0")
  testImplementation("io.mockk:mockk:1.11.0")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.test {
  testLogging {
    events("passed", "skipped", "failed") //, "standardOut", "standardError"

    showExceptions = true
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showCauses = true
    showStackTraces = true

    showStandardStreams = false
  }
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
  finalizedBy(tasks.jacocoTestCoverageVerification)
}

// Setup integration test
// Refer to: https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests
sourceSets {
  create("itest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    resources {
      srcDirs(sourceSets.main.get().resources)
    }
  }
}

val itestImplementation by configurations.getting {
  extendsFrom(configurations.testImplementation.get())
}
val itestRuntimeOnly by configurations.getting {
  extendsFrom(configurations.testRuntimeOnly.get())
}

val integrationTest = task<Test>("integrationTest") {
  description = "Runs integration tests."
  group = "verification"
  environment = env.allVariables

  testClassesDirs = sourceSets["itest"].output.classesDirs
  classpath = sourceSets["itest"].runtimeClasspath
  testLogging {
    events("passed", "skipped", "failed", "standardOut", "standardError")
  }
}

dependencies {
  itestImplementation("io.rest-assured:rest-assured:4.4.0")
  itestImplementation("io.rest-assured:json-path:4.4.0")
  itestImplementation("io.rest-assured:json-schema-validator:4.4.0")
  itestImplementation("io.rest-assured:kotlin-extensions:4.4.0")
}

// Setup spotless
spotless {
  kotlin {
    ktlint("0.40.0").userData(mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "2",
      "disabled_rules" to "no-wildcard-imports"
    ))
  }
}

// Setup jacoco
jacoco {
  toolVersion = "0.8.7"
}

tasks.withType<JacocoCoverageVerification> {
  dependsOn(tasks.test)
  violationRules {
    rule {
      limit {
        minimum = BigDecimal(0.95)
      }
    }
  }

  afterEvaluate {
    classDirectories.setFrom(files(classDirectories.files.map {
      fileTree(it).apply { exclude(excludesFiles) }
    }))
  }
  finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<JacocoReport> {
  dependsOn(tasks.test)
  sourceSets(sourceSets["main"])
  sourceDirectories.setFrom(files(sourceSets["main"].allSource.srcDirs))
  classDirectories.setFrom(files(sourceSets["main"].output))
  executionData.setFrom(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

  afterEvaluate {
    classDirectories.setFrom(files(classDirectories.files.map {
      fileTree(it).apply { exclude(excludesFiles) }
    }))
  }
}

tasks.getByName("bootJar") {
  doLast {
    download.run {
      src("https://github.com/aws-observability/aws-otel-java-instrumentation/releases/download/v1.26.0/aws-opentelemetry-agent.jar")
      dest("${project.buildDir.toString()}/otel/aws-opentelemetry-agent.jar")
    }
  }
}
