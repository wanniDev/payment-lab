import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


group = "org.collaborator"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

val asciidoctorExtensions: Configuration by configurations.creating

dependencies {

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
	testImplementation("org.springframework.security:spring-security-test")

	/** aop */
	implementation("org.springframework.boot:spring-boot-starter-aop")

	/** rest docs */
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("org.springframework.restdocs:spring-restdocs-asciidoctor")
	asciidoctorExtensions("org.springframework.restdocs:spring-restdocs-asciidoctor")

	/** jwt */
	implementation("com.auth0:java-jwt:4.2.1")

	/** queryDSL */
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	implementation("com.querydsl:querydsl-core")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
	kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
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
		dependsOn(asciidoctor)

		from(asciidoctor.get().outputDir) {
			into("BOOT-INF/classes/static")
		}
	}
}

