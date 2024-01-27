import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.2.1"
    id("io.micronaut.aot") version "4.2.1"
}

version = "0.1"
group = "io.hashimati"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micrometer:context-propagation")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.views:micronaut-views-fieldset")
    implementation("io.micronaut.views:micronaut-views-thymeleaf")
    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut:micronaut-http-client")
}


application {
    mainClass.set("io.hashimati.Application")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}


graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("io.hashimati.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading.set(false)
        convertYamlToJava.set(false)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)
    }
}

tasks.register<NpmTask>("npmTask")

abstract class NpmTask : DefaultTask() {
    @get:Input
    abstract val workingDir : Property<String>
    init {
        // set working dir to project root
        workingDir.convention(project.projectDir.absolutePath+"/src/main/webapp".replace("/", File.separator))
    }

    fun printProcessOutput(process: Process) {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String? = null
        while ({ line = reader.readLine(); line }() != null) {
            println(line)
        }
    }

    @TaskAction
    fun buildNpm() {
        //  println "Greeting: ${greeting.get()}"
        println("Working Dir: ${workingDir.get()}")
        //get the operating system
        val os :String= System.getProperty("os.name").lowercase()
        println("OS: ${os}")

        //create the assets folder if it does not exist
        val staticFolder:File =  File(project.projectDir.absolutePath + "/src/main/resources/static".replace("/", File.separator));
        if(!staticFolder.exists()) {
            staticFolder.mkdirs();
        }

        //create the views folder if it does not exist
        val views =  File(project.projectDir.absolutePath + "/src/main/resources/views".replace("/", File.separator));
        if(!views.exists()) {
            views.mkdirs();
        }

        if(os.contains("win")) {

            val npmInstallBuilder =  ProcessBuilder("npm.cmd", "install").directory(File(workingDir.get()));
            val npmInstall=  npmInstallBuilder.start()

            printProcessOutput(npmInstall)
            if(npmInstall.isAlive() == true)
            {
                npmInstall.waitFor();

            }
            val builder =  ProcessBuilder("npm.cmd", "run", "build").directory(File(workingDir.get()));
            val npmBuild =builder.start();

            printProcessOutput(npmBuild)
            //check if builder is not finished
            if(npmBuild.isAlive() == true) {
                //wait for the process to finish
                npmBuild.waitFor();
            }
            val moveIndexBuilder =  ProcessBuilder("cmd.exe", "/c", "move", project.projectDir.absolutePath + "/src/main/webapp/dist/index.html".replace("/", File.separator), project.projectDir.absolutePath + "/src/main/resources/views".replace("/", File.separator));
            printProcessOutput(moveIndexBuilder.start());
            val moveAssetsBuilder =  ProcessBuilder("cmd.exe", "/c", "move", project.projectDir.absolutePath + "/src/main/webapp/dist/assets".replace("/", File.separator), project.projectDir.absolutePath + "/src/main/resources/static".replace("/", File.separator));
            printProcessOutput(moveAssetsBuilder.start())
            val moveFaviconBuilder =  ProcessBuilder("cmd.exe", "/c", "move", project.projectDir.absolutePath + "/src/main/webapp/dist/favicon.ico".replace("/", File.separator), project.projectDir.absolutePath + "/src/main/resources/static".replace("/", File.separator));
            printProcessOutput(moveFaviconBuilder.start());
        }
        else {

            val npmInstallBuilder =  ProcessBuilder("npm", "install").directory( File(workingDir.get()));
            val npmInstall=  npmInstallBuilder.start()
            printProcessOutput(npmInstall)
            if(npmInstall.isAlive() == true)
            {
                npmInstall.waitFor();

            }
            val builder =  ProcessBuilder("npm", "run", "build").directory( File(workingDir.get()));
            val npmBuild =builder.start();
            printProcessOutput(npmBuild)
            //check if builder is not finished
            if(npmBuild.isAlive() == true) {
                //wait for the process to finish
                npmBuild.waitFor();
            }

            val moveIndexBuilder =  ProcessBuilder("mv", project.projectDir.absolutePath + "/src/main/webapp/dist/index.html".replace("/", File.separator), project.projectDir.absolutePath + "/src/main/resources/views".replace("/", File.separator));
            printProcessOutput(moveIndexBuilder.start());

            val moveAssetsBuilder =  ProcessBuilder("mv", project.projectDir.absolutePath + "/src/main/webapp/dist/assets".replace("/", File.separator), project.projectDir.absolutePath + "/src/main/resources/static".replace("/", File.separator));
            printProcessOutput(moveAssetsBuilder.start());

            val moveFaviconBuilder =  ProcessBuilder("mv", project.projectDir.absolutePath + "/src/main/webapp/dist/favicon.ico".replace("/", File.separator), project.projectDir.absolutePath + "/src/main/resources/static".replace("/", File.separator));
            printProcessOutput(moveFaviconBuilder.start());
        }
    }
}

