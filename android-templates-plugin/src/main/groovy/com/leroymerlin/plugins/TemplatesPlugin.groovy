package com.leroymerlin.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

/**
 * Created by florian on 04/07/2017.
 */
class TemplatesPlugin implements Plugin<Project> {

    public static final String GROUP = 'template'

    @Override
    void apply(Project project) {

        project.extensions.create("templates", TemplatesPluginExtension.class, project)

        project.task("generateTemplates", group: GROUP, dependsOn: project.tasks.withType(TemplateCreatorTask))

        def zipDir = new File(project.buildDir, "templates/zip")
        zipDir.mkdirs()
        def generatedDir = new File(project.buildDir, "templates/generated")
        generatedDir.mkdirs()

        def zipAllTask = project.task("zipTemplates", group: GROUP, type: Zip, dependsOn: ["generateTemplates"]) {
            from generatedDir
            include '**/*'
            archiveName "templates.zip"
            destinationDir zipDir
        }


        project.afterEvaluate {




            if (project.tasks.findByName('build')) {
                project.tasks.getByName("build").dependsOn += zipAllTask
            }

//            project.tasks.withType(TemplateCreatorTask).all {
//                taskCreator ->
//                    def zipTask = project.task("zip${taskCreator.templateName}Template", group: GROUP, type: Zip, dependsOn: taskCreator){
//                        from taskCreator.getOutputDir()
//                        include '*'
//                        archiveName "${taskCreator.templateName}.zip"
//                        destinationDir(new File(project.buildDir,"/templates/zip/"))
//                    }
//
//                    zipAllTask.dependsOn += zipTask
//            }
        }

//        project.gradle.addListener(new DependencyResolutionListener() {
//            @Override
//            void beforeResolve(ResolvableDependencies resolvableDependencies) {
//
//            }
//
//            @Override
//            void afterResolve(ResolvableDependencies resolvableDependencies) {
//               // resolvableDependencies.resolutionResult.
//            }
//        })

        //project.apply plugin: "java"
//        repositories {
//            maven {
//                url "http://nexus:8081/nexus/content/groups/public"
//            }
//        }
//        configurations {
//            drivers
//        }
//        dependencies {
//            drivers "com.company:chromedriver-mac32:2.8@bin"
//        }

    }


    File findAndroidFolder() {

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
        if (System.getenv("ANDROID_STUDIO_HOME") != null) {
            androidStudioHome = new File(System.getenv("ANDROID_STUDIO_HOME"))
        } else if (System.getProperty('jb.vmOptionsFile') != null) {
            androidStudioHome = new File(System.getProperty('jb.vmOptionsFile')).getParentFile().getParentFile();
        }

        if (androidStudioHome?.exists()) {
            println "yesssss !!!!! "
        } else {
            println "noooooo !!!!! "

        }


    }
}
