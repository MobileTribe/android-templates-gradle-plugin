package com.leroymerlin.plugins

import org.gradle.api.Project

/**
 * Created by florian on 04/07/2017.
 */
class TemplatesPluginExtension {

    Project project



    private String mTemplatesName
    String setTemplatesName(String templateName){
        this.templateName = mTemplatesName
    }
    String getTemplatesName() {
        if(mTemplatesName){
            return mTemplatesName
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
