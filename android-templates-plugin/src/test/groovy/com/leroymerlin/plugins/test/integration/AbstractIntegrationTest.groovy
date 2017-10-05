package com.leroymerlin.plugins.test.integration

import com.leroymerlin.plugins.test.TestUtils
import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass

/**
 * Created by florian on 17/12/15.
 */
abstract class AbstractIntegrationTest {

    Project project
    File workingDirectory, projectTemplate

    abstract String getProjectName()

    @BeforeClass
    static void setUpPlugin() {
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(new File(""))
                .connect()
        try {
            connection.newBuild()
                    .forTasks(":android-template-plugin:install")
                    .run()
        } finally {
            connection.close()
        }
    }

    @Before
    void setUp() {
        projectTemplate = new File(TestUtils.getPluginBaseDir(), "src/test/resources/${getProjectName()}")
        workingDirectory = new File(TestUtils.getPluginBaseDir(), "build/tests/integration/${getProjectName()}")
        FileUtils.deleteDirectory(workingDirectory)
        FileUtils.copyDirectory(projectTemplate, workingDirectory)
        project = ProjectBuilder.builder().withProjectDir(workingDirectory).build()
    }

    @After
    void tearDown() {
        Thread.sleep(1000)
        project = null
        FileUtils.deleteDirectory(workingDirectory)
    }

    protected void applyExtraGradle(String string) {
        new File(workingDirectory, "extra.gradle") << string
    }

    protected String testTask(String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(workingDirectory)
                .connect()
        String versionPlugin = getPluginVersion()


        try {
            def outputStream = new ByteArrayOutputStream()
            OutputStream out = new TeeOutputStream(outputStream, System.out)
            connection.newBuild()
                    .forTasks(tasks)
                    .withArguments("--stacktrace", "--info", "-DPLUGIN_VERSION=$versionPlugin")
                    .setStandardOutput(out)
                    .run()

            return outputStream.toString("UTF-8")
        } finally {
            connection.close()
        }
    }

    protected String getPluginVersion() {
        Properties props = new Properties()
        props.load(new FileInputStream(new File(TestUtils.getPluginBaseDir(), "../version.properties")))
        def versionPlugin = props.getProperty('version')
        versionPlugin
    }


    protected String getTestVersion(){
        Properties props = new Properties()
        props.load(new FileInputStream(project.file("version.properties")))
        return props.getProperty('version')
    }
}