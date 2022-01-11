import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.adtran:scala-multiversion-plugin:1.+") // for scala cross building
    }
}

plugins {
    java
    scala
    id("com.github.johnrengelman.shadow") version "4.0.3"
    idea
    kotlin("jvm") version "1.3.50"
    id("com.adtran.scala-multiversion-plugin") version "1.0.36" // for scala cross building
}

repositories {
    mavenCentral()
}

dependencies {
    fun alchemist(module: String? = null) = "it.unibo.alchemist:alchemist${if (module == null) "" else "-$module"}:11.3.0"

    implementation(alchemist())
    implementation(alchemist("incarnation-scafi"))
    implementation(alchemist("swingui"))
    implementation ("org.danilopianini:thread-inheritable-resource-loader:0.3.5")

    implementation("org.scala-lang:scala-library:%scala-version%")
    implementation("it.unibo.scafi:scafi-core_2.13:0.3.3")
}

tasks.withType<ScalaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

sourceSets.getByName("main") {
    resources {
        srcDirs("src/main/protelis")
    }
}

// Needed to avoid error:
// > shadow.org.apache.tools.zip.Zip64RequiredException: archive contains more than 65535 entries.
tasks.withType<ShadowJar> {
    isZip64 = true
    classifier = null
    version = null
    // baseName = "anotherBaseName"
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to "Alchemist",
                "Implementation-Version" to rootProject.version,
                "Main-Class" to "it.unibo.alchemist.Alchemist",
                "Automatic-Module-Name" to "it.unibo.alchemist"
        ))
    }
    archiveBaseName.set("${rootProject.name}-redist")
    isZip64 = true
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        // remove all signature files
        exclude("META-INF/")
        exclude("ant_tasks/")
        exclude("about_files/")
        exclude("help/about/")
        exclude("build")
        exclude("out")
        exclude("bin")
        exclude(".gradle")
        exclude("build.gradle.kts")
        exclude("gradle")
        exclude("gradlew")
        exclude("gradlew.bat")
    }
    with(tasks.jar.get() as CopySpec)
}

fun makeTest(
        file: String,
        name: String = file,
        sampling: Double = 1.0,
        time: Double = Double.POSITIVE_INFINITY,
        vars: Set<String> = setOf(),
        maxHeap: Long? = null,
        taskSize: Int = 1024,
        threads: Int? = null,
        debug: Boolean = false,
        effects: String? = null
) {
    val heap: Long = maxHeap ?: if (System.getProperty("os.name").toLowerCase().contains("linux")) {
        ByteArrayOutputStream()
                .use { output ->
                    exec {
                        executable = "bash"
                        args = listOf("-c", "cat /proc/meminfo | grep MemAvailable | grep -o '[0-9]*'")
                        standardOutput = output
                    }
                    output.toString().trim().toLong() / 1024
                }
                .also { println("Detected ${it}MB RAM available.") }  * 9 / 10
    } else {
        // Guess 10GB RAM of which 2 used by the OS
        10 * 1024L
    }

    val threadCount = threads ?: maxOf(1, minOf(Runtime.getRuntime().availableProcessors(), heap.toInt() / taskSize ))

    val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    tasks.register("$name-pre") {
        doLast {
            println("$name > Running task $name on $threadCount threads and with maxHeapSize $heap")
        }
    }

    task<JavaExec>("$name") {
        dependsOn("$name-pre")
        classpath = sourceSets["main"].runtimeClasspath
        classpath("src/main/protelis")
        main = "it.unibo.alchemist.Alchemist"
        maxHeapSize = "${heap}m"
        jvmArgs("-XX:+AggressiveHeap")
        jvmArgs("-XX:-UseGCOverheadLimit")
        //jvmArgs("-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap") // https://stackoverflow.com/questions/38967991/why-are-my-gradle-builds-dying-with-exit-code-137
        if (debug) {
            jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044")
        }
        File("data").mkdirs()
        args(
                "-y", "src/main/yaml/${file}.yml",
                "-t", "$time",
                "-e", "data/${today}-${name}",
                "-p", threadCount,
                "-i", "$sampling"
        )
        if (vars.isNotEmpty()) {
            args("-b", "-var", *vars.toTypedArray())
        }
        if(effects != null){
            args("-g", effects)
        }
    }
}

makeTest(name="hellogui", file = "hello_scafi", time = 100.0, taskSize = 2800, effects="src/main/resources/helloworld.aes")
makeTest(name="hello", file = "hello_scafi", time = 100.0, vars = setOf("random"), taskSize = 2800)

makeTest(name="aggregate_processes", file = "aggregate_processes", effects="src/main/resources/aggregate_processes.aes")

makeTest(name="scr", file = "selforg_coord_regions", effects="src/main/resources/selforg_coord_regions.aes")
makeTest(name="scrdata", file = "selforg_coord_regions", time = 1000.0, vars = setOf("random"))
