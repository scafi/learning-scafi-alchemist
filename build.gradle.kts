

plugins {
    application
    alias(libs.plugins.multiJvmTesting) // Pre-configures the Java toolchains
    alias(libs.plugins.taskTree) // Helps debugging dependencies among gradle tasks
    scala
}

repositories {
    mavenCentral()
}

dependencies {
    // Check the catalog at gradle/libs.versions.gradle
    implementation(libs.bundles.alchemist)
}

multiJvm {
    jvmVersionForCompilation.set(17)
}

val batch: String by project
val maxTime: String by project
val variables: String by project

val alchemistGroup = "Run Alchemist"
/*
 * This task is used to run all experiments in sequence
 */
val runAll by tasks.register<DefaultTask>("runAll") {
    group = alchemistGroup
    description = "Launches all simulations"
}
/*
 * Scan the folder with the simulation files, and create a task for each one of them.
 */
File(rootProject.rootDir.path + "/src/main/yaml").listFiles()
    .filter { it.extension == "yml" } // pick all yml files in src/main/yaml
    .sortedBy { it.nameWithoutExtension } // sort them, we like reproducibility
    .forEach { it ->
        // one simulation file -> one gradle task
        val task by tasks.register<JavaExec>("run${it.nameWithoutExtension.capitalize()}") {
            group = alchemistGroup // This is for better organization when running ./gradlew tasks
            description = "Launches simulation ${it.nameWithoutExtension}" // Just documentation
            mainClass.set("it.unibo.alchemist.Alchemist") // The class to launch
            classpath = sourceSets["main"].runtimeClasspath // The classpath to use
            // In case our simulation produces data, we write it in the following folder:
            val exportsDir = File("${projectDir.path}/build/exports/${it.nameWithoutExtension}")
            doFirst {
                // this is not executed upfront, but only when the task is actually launched
                // If the export folder doesn not exist, create it and its parents if needed
                if (!exportsDir.exists()) {
                    exportsDir.mkdirs()
                }
            }
            // Uses the latest version of java
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(multiJvm.latestJava))
                }
            )
            // These are the program arguments
            args("run", it.absolutePath)
            if (System.getenv("CI") == "true" || batch == "true") {
                // If it is running in a Continuous Integration environment, use the "headless" mode of the simulator
                // Namely, force the simulator not to use graphical output.
                args(

                    "--override",
                    "terminate: { type: AfterTime, parameters: [$maxTime] }",
                    "--override",
                    """
                    launcher: {
                        parameters: {
                            batch: [ ],
                            showProgress: true,
                            autoStart: true,
                            parallelism: 1,
                        }
                    }
                """.trimIndent())
            } else {
                // A graphics environment should be available, so load the effects for the UI from the "effects" folder
                // Effects are expected to be named after the simulation file
                args(
                    "--override",
                    "monitors: { type: SwingGUI, parameters: { graphics: effects/${it.nameWithoutExtension}.json } }",
                    "--override",
                    "launcher: { parameters: { batch: [], autoStart: false } }",
                    "--override",
                    "terminate: { type: AfterTime, parameters: [$maxTime] }"
                )
            }
            // This tells gradle that this task may modify the content of the export directory
            outputs.dir(exportsDir)
        }
        // task.dependsOn(classpathJar) // Uncomment to switch to jar-based classpath resolution
        runAll.dependsOn(task)
    }

tasks.withType<Tar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.WARN
}
tasks.withType<Zip>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.WARN
}