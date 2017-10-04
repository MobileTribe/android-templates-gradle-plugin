package com.leroymerlin.plugins.test

import com.leroymerlin.plugins.TemplatesPlugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.http.util.Asserts
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class TemplatesPluginTest {

    Project project

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        def manager = project.pluginManager
        manager.apply(TemplatesPlugin.class)
    }

    @After
    void tearDown() {
        project = null
    }

    @Test
    void testTemplatesExtension() {
        Asserts.notNull(project.templates, "templates extension don't exist")
    }

    @Test
    void testTemplateTaskGeneration() {
        project.templates {
            template('test') {

            }
        }
        project.evaluate()
        Asserts.notNull(project.tasks.findByPath('generateTestTemplate'), "task generateTestTemplate not found")
    }


    @Test
    void testTemplateTaskParameters() {

        def templatesFolder = new File(TestUtils.pluginBaseDir, '/src/test/resources/android/src/main/')
        def templatesJava = new File(templatesFolder, 'java/com/leroymerlin/templates/fragment')
        def fragmentClassName = "BasePandroid"

        project.
        //tag::template[]
                templates {
                    //define the folder in which install the files
                    templatesName "myCustomTemplates"
                    //create a new template named PandroidFragmentTest
                    template('PandroidFragmentTest') {
                        //template description
                        description = "Creates a new basic PandroidFragment with a presenter."
                        //template category
                        category = "CustomActivities"
                        //template formfactor
                        formfactor = "Mobile"
                        //java files to use
                        javaFiles = project.fileTree(templatesJava).include('fragment/*.java')
                        //resources files to use (layout, values, manifest...)
                        resFiles = project.files(new File(templatesFolder.path, "res/layout/fragment_base.xml"))

                        //configure your template parameters
                        parameters {
//end::template[]
                            fragmentClass {
                                replace = "${fragmentClassName}Fragment"
                                label = "Fragment Name"
                                constraints = "class|unique|nonempty"
                                suggest = 'fragment_${classToResource(fragmentClass)}'
                                help = "The name of the fragment class to create"
                            }
//tag::template[]
                            //variable name
                            layoutName {
                                //value to remplace in source files
                                replace = 'fragment_base'
                                //user label to display. If not set the variable won't be visible
                                label = "Layout Name"
                                //template constraint. See existing template
                                constraints = "layout|unique|nonempty"
                                //suggest value
                                suggest = 'fragment_${classToResource(fragmentClass)}'
                                //help value to display
                                help = "The name of the layout to create for the fragment"
                            }

                            //another example
                            packageName {
                                replace = 'com.leroymerlin.templates'
                                label = "Package Name"
                                constraints = "package"
                                help = "The name of the layout to create for the fragment"
                            }
//end::template[]
                            openerClass {
                                value = '${fragmentClass}Opener';
                                replace = "${fragmentClassName}FragmentOpener";
                            }
                            presenterClass {
                                value = '${fragmentClass}Presenter';
                                replace = "${fragmentClassName}FragmentPresenter";
                            }
                            //tag::template[]
                        }
                    }
                }
//end::template[]
    }


}
