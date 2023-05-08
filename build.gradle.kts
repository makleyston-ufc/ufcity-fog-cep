plugins {
    id("java")
}

group = "com.ufcity.cep"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.1")
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(fileTree("lib"){
        include("*.jar")
    })
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.ufcity.cep.Main"
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}
