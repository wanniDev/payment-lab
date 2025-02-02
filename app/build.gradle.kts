plugins {
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
}

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

val asciidoctorExtensions: Configuration by configurations.creating

dependencies {
	implementation(project(":account-api:account-presentation"))
	implementation(project(":account-api:account-application"))
	implementation(project(":account-api:account-domain"))
	implementation(project(":account-api:account-infrastructure"))

	implementation(project(":payment-api:payment-presentation"))
	implementation(project(":payment-api:payment-application"))
	implementation(project(":payment-api:payment-domain"))
	implementation(project(":payment-api:payment-infrastructure"))

	implementation(project(":common"))

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
	testImplementation("org.springframework.security:spring-security-test")

	/** aop */
	implementation("org.springframework.boot:spring-boot-starter-aop")

	/** rest docs */
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("org.springframework.restdocs:spring-restdocs-asciidoctor")
	asciidoctorExtensions("org.springframework.restdocs:spring-restdocs-asciidoctor")

	/** queryDSL */
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	implementation("com.querydsl:querydsl-core")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
	kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

	/** kafka **/
	implementation("org.springframework.kafka:spring-kafka")
	/** kafka test **/
	implementation("org.springframework.kafka:spring-kafka-test")
	implementation("org.testcontainers:kafka:1.19.3")

	/** resilience4j **/
	implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")

	/** monitoring **/
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	implementation("org.togglz:togglz-spring-boot-starter:3.1.2")
	implementation("org.togglz:togglz-kotlin:2.8.0")
	implementation("org.togglz:togglz-console:3.1.2")
	implementation("org.togglz:togglz-spring-security:3.1.2")
	implementation("com.github.heneke.thymeleaf:thymeleaf-extras-togglz:2.0.1.RELEASE")

	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
}

tasks {
	val snippetsDir = file("$buildDir/generated-snippets")

	clean {
		delete("src/main/resources/static")
	}

	test {
		useJUnitPlatform()
		systemProperty("org.springframework.restdocs.outputDir", snippetsDir)
		outputs.dir(snippetsDir)
	}

	build {
		dependsOn("copyDocument")
	}

	asciidoctor {
		dependsOn(test)

		attributes(
			mapOf("snippets" to snippetsDir)
		)
		inputs.dir(snippetsDir)

		doFirst {
			delete("src/main/resources/static")
		}
	}

	register<Copy>("copyDocument") {
		dependsOn(asciidoctor)

		destinationDir = file(".")
		from(asciidoctor.get().outputDir) {
			into("src/main/resources/static")
		}
	}

	bootJar {
		enabled = true
		dependsOn(asciidoctor)

		from(asciidoctor.get().outputDir) {
			into("BOOT-INF/classes/static")
		}
	}

	jar {
		enabled = false
	}
}


