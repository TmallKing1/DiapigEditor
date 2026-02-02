plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
}

group = "top.pigest"
version = "1.0"

repositories {
    maven(url = "https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("top.pigest.dialogeditor")
    mainClass.set("top.pigest.dialogeditor.DialogEditor")
    applicationDefaultJvmArgs = listOf("--add-opens=java.base/java.lang.reflect=com.jfoenix",
        "--add-opens=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix",
        "--add-opens=javafx.controls/com.sun.javafx.scene.control=com.jfoenix",
        "--add-opens=javafx.base/com.sun.javafx.binding=com.jfoenix",
        "--add-opens=javafx.base/com.sun.javafx.event=com.jfoenix",
        "--add-opens=javafx.graphics/com.sun.javafx.stage=com.jfoenix",
        "--add-opens=javafx.graphics/com.sun.javafx.scene=com.jfoenix",
        "--add-opens=javafx.graphics/com.sun.javafx.util=com.jfoenix",
        "--add-opens=com.jfoenix/com.jfoenix.skins=top.pigest.dialogeditor")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    implementation("org.rationalityfrontline.workaround:jfoenix:21.0.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-fontawesome6-pack:12.4.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "Diapig Editor"
    }
    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            imageOptions.addAll(listOf("--icon", "src/main/resources/top/pigest/dialogeditor/icon.ico"))
        }
    }
}
