package com.leroymerlin.plugins

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.WriteProperties
import org.gradle.api.tasks.bundling.Zip

import java.util.zip.ZipFile

/**
 * Created by florian on 04/07/2017.
 */
class TemplatesPlugin implements Plugin<Project> {

    public static final String GROUP = 'template'

    Project project
    TemplatesPluginExtension templatesExtension;

    @Override
    void apply(Project project) {
        this.project = project;
        this.templatesExtension = project.extensions.create("templates", TemplatesPluginExtension.class, project)

        def zipDir = new File(project.buildDir, "templates/zip")
        zipDir.mkdirs()
        def generatedDir = new File(project.buildDir, "templates/generated")
        generatedDir.mkdirs()

        def createTemplatesPropertiesTask = project.task("createTemplatesProperties", group: GROUP, type: WriteProperties) {
            outputFile = new File(generatedDir, "templates.properties")
        }

        project.task("generateTemplates", group: GROUP, dependsOn: project.tasks.withType(TemplateCreatorTask))



        def zipAllTask = project.task("zipTemplates", group: GROUP, type: Zip, dependsOn: ["generateTemplates"]) {
            from generatedDir
            from createTemplatesPropertiesTask.outputFile
            include '**/*'
            archiveName "templates.zip"
            destinationDir zipDir
        }


        def uninstallTemplatesTask = project.task("uninstallTemplates").doLast {
            if (!project.tasks.withType(TemplateCreatorTask).isEmpty()){
                uninstallTemplates(templatesExtension.templatesName)
            }
            project.configurations.template.resolve().each {
                uninstallTemplates(getTemplatesProperties(it).getProperty("name"))
            }
        }
        project.task("installTemplates", dependsOn: [uninstallTemplatesTask, zipAllTask]).doLast {
            if (!project.tasks.withType(TemplateCreatorTask).isEmpty()) {
                installTemplates(templatesExtension.templatesName, zipAllTask.outputs.files.singleFile, project.version.toString())
            }
            project.configurations.template.resolve().each {
                def prop = getTemplatesProperties(it);
                installTemplates(prop.getProperty("name"), it, prop.getProperty("version"))
            }
        }

        project.configurations {
            template
        }

        project.configurations.template.dependencies.all {
            dependency ->
                //we force zip as an extension
                dependency.artifacts.each { art ->
                    art.extension = "zip"
                }
        }

        project.afterEvaluate {
            if (project.tasks.findByName('build')) {
                project.tasks.getByName("build").dependsOn += zipAllTask
            }
            createTemplatesPropertiesTask.properties = [version: project.version, name: templatesExtension.templatesName]
        }
    }


    Properties getTemplatesProperties(File file) {
        def zip = new ZipFile(file)
        Properties properties = new Properties()
        def stream = zip.getInputStream(zip.getEntry("templates.properties"))
        properties.load(stream)
        stream.close()
        return properties;
    }

    def uninstallTemplates(String name) {
        def outputDir = new File(findAndroidStudioFolder(), "$name")
        outputDir.deleteDir()
    }

    def installTemplates(String name, File file, String version) {
        def outputDir = new File(findAndroidStudioFolder(), "$name")
        def zip = new ZipFile(file)
        zip.entries().each {
            if (!it.isDirectory()) {
                def fOut = new File(outputDir, it.name)
                //create output dir if not exists
                fOut.parentFile.mkdirs()
                def fos = new FileOutputStream(fOut)
                def buf = new byte[it.size]
                def len = zip.getInputStream(it).read(buf)
                fos.write(buf, 0, len)
                fos.close()
            }
        }
        project.logger.warn("$name templates have been installed. You have to restart Android Studio to use them.")
    }

    static File findAndroidStudioFolder() throws FileNotFoundException {

//        def templateDir = new File('/Applications/Android Studio.app/Contents/plugins/android/lib/templates/pandroid');
//
//
//        task clearTemplates(type: Delete) {
//            delete templateDir
//        }
//
//        task installTemplates(type: Copy, dependsOn: 'clearTemplates') {
//            from file('templates')
//            into templateDir
//        }


        File androidStudioHome = null;
        if (System.getProperty("ANDROID_STUDIO_HOME") != null) {
            androidStudioHome = new File(System.getenv("ANDROID_STUDIO_HOME"))
        } else if (System.getenv("ANDROID_STUDIO_HOME") != null) {
            androidStudioHome = new File(System.getenv("ANDROID_STUDIO_HOME"))
        } else if (System.getProperty('jb.vmOptionsFile') != null) {
            androidStudioHome = new File(System.getProperty('jb.vmOptionsFile')).getParentFile().getParentFile();
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            androidStudioHome = new File("/Applications/Android Studio.app")
        }

        if (androidStudioHome)
            androidStudioHome = new File(androidStudioHome, "Contents/plugins/android/lib/templates")

        if (androidStudioHome?.exists()) {
            return androidStudioHome
        } else {
            throw new FileNotFoundException("Can't find Android Studio directory. Please add the path to ANDROID_STUDIO_HOME environment variable")
        }


    }
}
