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
        project.templates {
            template('PandroidFragmentTest'){
                description = "Creates a new basic PandroidFragment with a presenter."
                javaFiles = project.fileTree(templatesJava).include('fragment/*.java')
                resFiles = project.files(new File(templatesFolder.path ,"res/layout/fragment_base.xml"))
                parameters {
                    fragmentClass {
                        replace = "${fragmentClassName}Fragment"
                        label = "Fragment Name"
                        constraints = "class|unique|nonempty"
                        suggest = 'fragment_${classToResource(fragmentClass)}'
                        help = "The name of the fragment class to create"
                    }

                    layoutName {
                        replace = 'fragment_base'
                        label = "Layout Name"
                        constraints = "layout|unique|nonempty"
                        suggest = 'fragment_${classToResource(fragmentClass)}'
                        help = "The name of the layout to create for the fragment"
                    }

                    packageName {
                        replace = 'com.leroymerlin.pandroid.templates'
                        label = "Package Name"
                        constraints = "package"
                        help = "The name of the layout to create for the fragment"
                    }

                    openerClass {
                        value = '${fragmentClass}Opener';
                        replace = "${fragmentClassName}FragmentOpener";
                    }
                    presenterClass {
                        value = '${fragmentClass}Presenter';
                        replace = "${fragmentClassName}FragmentPresenter";
                    }
                }
            }
        }
    }




}
