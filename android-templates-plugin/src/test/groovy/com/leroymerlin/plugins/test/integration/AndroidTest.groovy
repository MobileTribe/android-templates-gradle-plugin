package com.leroymerlin.plugins.test.integration

import org.junit.Assert
import org.junit.Test

/**
 * Created by florian on 17/12/15.
 */
class AndroidTest extends AbstractIntegrationTest {

    @Override
    String getProjectName() {
        return "android"
    }

    @Test
    void testGenerationTemplates() {
        addTemplateFragment();
        testTask('generateTemplates')
        def archivePath = new File(workingDirectory, "build/templates/generated")
        Assert.assertTrue("Output folder doesn't exist", archivePath.exists())
        Assert.assertTrue("Output folder is empty", archivePath.isDirectory() && archivePath.listFiles().size() > 0)
    }

    @Test
    void testZipTemplates() {
        addTemplateFragment();
        testTask('zipTemplates')
        def archivePath = new File(workingDirectory, "build/templates/zip")
        Assert.assertTrue("Output folder doesn't exist", archivePath.exists())
        Assert.assertTrue("Output folder is empty", archivePath.isDirectory() && archivePath.listFiles().size() > 0)
    }


    @Test
    void testDelivery() {
        addTemplateFragment();
        testTask('install')
    }

    private addTemplateFragment() {
        applyExtraGradle('''
        def templatesFolder = file('src/main/')
        def templatesJava = new File(templatesFolder, 'java/com/leroymerlin/templates')
        def fragmentClassName = "BasePandroid"
        templates {
            template('PandroidFragmentTest'){
                description = "Creates a new basic PandroidFragment with a presenter."
                javaFiles = fileTree(templatesJava).include('fragment/*.java')
                resourceFiles = files(new File(templatesFolder.path ,"res/layout/fragment_base.xml"))
                parameters {
                    fragmentClass {
                        replace = "${fragmentClassName}Fragment"
                        label = "Fragment Name"
                        constraints = "class|unique|nonempty"
                        suggest = 'fragment_${classToResource(fragmentClass)}\'
                        help = "The name of the fragment class to create"
                    }

                    layoutName {
                        replace = 'fragment_base\'
                        label = "Layout Name"
                        constraints = "layout|unique|nonempty"
                        suggest = 'fragment_${classToResource(fragmentClass)}\'
                        help = "The name of the layout to create for the fragment"
                    }

                    packageName {
                        replace = 'com.leroymerlin.templates\'
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

''')
    }

}
