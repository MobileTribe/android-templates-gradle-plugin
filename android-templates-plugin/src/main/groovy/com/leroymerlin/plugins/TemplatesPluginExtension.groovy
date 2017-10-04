package com.leroymerlin.plugins

import org.gradle.api.Project

/**
 * Created by florian on 04/07/2017.
 */
class TemplatesPluginExtension {

    Project project
    String templatesName

    String getTemplatesName() {
        if(templatesName){
            return templatesName
        }
        return project.hasProperty('projectName') ? project.projectName : project.name
    }

    TemplatesPluginExtension(Project project) {
        this.project = project
    }

    def template(String name, Closure closure) {
        project.task(type: TemplateCreatorTask, dependsOn: ["createTemplatesProperties"], group: TemplatesPlugin.GROUP, "generate${name.capitalize()}Template", closure).templateName = name
    }

}
