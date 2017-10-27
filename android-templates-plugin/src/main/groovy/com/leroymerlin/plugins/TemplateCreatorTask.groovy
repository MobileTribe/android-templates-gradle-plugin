package com.leroymerlin.plugins

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class TemplateCreatorTask extends DefaultTask {

    @Input
    String templateName;

    @Input
    String description = ""

    @Input
    String category = "Custom"

    @Input
    String formfactor = "Mobile"

    @InputFiles
    @Optional
    FileCollection javaFiles

    @InputFiles
    @Optional
    FileCollection resFiles

    @Input
    String minSdk = "17"

    final NamedDomainObjectContainer<ParameterData> parametersContainer = this.project.container(ParameterData)


    void parameters(Action<NamedDomainObjectContainer<ParameterData>> action) {
        action.execute(parametersContainer)
    }

    @OutputDirectory
    File getOutputDir() {
        if (!templateName) {
            throw new IllegalStateException("templateName should be defined in ${this.name}")
        }
        File outputDir = new File(project.buildDir, "/templates/generated/$templateName")
        return outputDir
    }

    Map<String, String> replaceMap;

    @TaskAction
    public void generateTemplate() {
        File outputDir = getOutputDir()
        if (outputDir.exists()) {
            outputDir.deleteDir()
        }
        outputDir.mkdirs()

        replaceMap = new LinkedHashMap()
        parametersContainer.each {
            if (it.replace) {
                replaceMap.put(it.name, it.replace)
            }
        }
        replaceMap = replaceMap.sort { a, b -> b.value <=> a.value }

        writeGlobals()
        writeRecipe()
        writeTemplate()

        javaFiles?.each {
            def file = new File(outputDir, "root/src/app_package/" + it.name + ".ftl")
            createFoldersAndCopyFile(it, file)
        }

        resFiles?.each {
            if (it.name.contains("AndroidManifest")) {
                createFoldersAndCopyFile(it, new File(outputDir, "root/AndroidManifest.xml.ftl"))
            } else {
                def resFolder = it.parentFile
                createFoldersAndCopyFile(it, new File(outputDir, "root/" + resFolder.parentFile.name + "/" + resFolder.name + "/" + it.name + ".ftl"))
            }
        }
    }


    String replaceString(String name) {
        replaceMap.each {
            name = escapePackage(name.replaceAll(it.value, '\\$\\{' + it.key + '\\}'))
        }
        return name
    }


    static String escapePackage(String name) {
        return name.replaceAll(/import com.leroymerlin.pandroid.demo.([^;]+);/) {
            all, className ->
                '''
<#if applicationPackage??>
import ${applicationPackage}.''' + className + ''';
</#if>
'''
        }
    }

    void createFoldersAndCopyFile(File from, File to) {
        to.parentFile.mkdirs()
        to << replaceString(from.text)
    }

    void writeRecipe() {
        def xml = {
            recipe() {
                String openFile;
                javaFiles?.each { file ->
                    def path = '${escapeXmlAttribute(srcOut)}/' + replaceString(file.name)
                    if (!openFile) {
                        openFile = path
                    }
                    instantiate(from: 'root/src/app_package/' + file.name + ".ftl", to: path)
                }
                resFiles?.each { file ->
                    if (file.name.contains("AndroidManifest")) {
                        def path = '${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml'
                        merge(from: 'root/' + file.name + ".ftl", to: path)
                    } else {
                        def path = '${escapeXmlAttribute(resOut)}/' + file.parentFile.name + "/" + replaceString(file.name)
                        merge(from: 'root/res/' + file.parentFile.name + '/' + file.name + ".ftl", to: path)

                    }
                }
                if (openFile) {
                    open(file: openFile)
                }
            }
        }
        writeFile('recipe.xml.ftl', xml)
    }


    void writeTemplate() {
        def xml = {
            template(format: '5',
                    revision: project.version,
                    name: templateName,
                    description: description,
                    minApi: minSdk,
                    minBuildApi: minSdk) {

                category(value: category)
                formfactor(value: formfactor)

                parametersContainer.findAll { it.label }.each {
                    parameter(id: it.name,
                            name: it.label,
                            type: 'string',
                            constraints: it.constraints,
                            suggest: it.suggest,
                            default: it.defaultValue,
                            help: it.help)
                }

                globals(file: "globals.xml.ftl")
                execute(file: "recipe.xml.ftl")
            }
        }
        writeFile('template.xml', xml)
    }

    void writeGlobals() {
        def xml = {
            globals() {
                parametersContainer.findAll { !it.label }.each {
                    item ->
                        global(id: item.name, value: item.value)
                }
            }
        }
        writeFile('globals.xml.ftl', xml)
    }


    void writeFile(String path, Closure closure) {
        def builder = new StreamingMarkupBuilder()
        builder.encoding = "UTF-8"
        new File(getOutputDir(), path) << XmlUtil.serialize(
                builder.bind(closure << { mkp.xmlDeclaration() })
        )
    }


}


class ParameterData {
    String name;
    String label;
    String replace;
    String constraints = "";
    String defaultValue = "";
    String suggest = "";
    String help = "";
    String value

    ParameterData(String name) {
        this.name = name;
    }
}