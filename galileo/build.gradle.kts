/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/platform-microservices.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("application")
}

group = "app.whichlicense.service"
version = "0.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("app.whichlicense.service.galileo.GalileoService")
}

repositories {
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    mavenCentral()
}

dependencies {
    implementation("io.helidon.nima.http2:helidon-nima-http2-webserver:4.0.0-ALPHA6")
    testImplementation("io.helidon.nima.testing.junit5:helidon-nima-testing-junit5-webserver:4.0.0-ALPHA6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.whichlicense.testing:naming:0.1.1-SNAPSHOT")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
