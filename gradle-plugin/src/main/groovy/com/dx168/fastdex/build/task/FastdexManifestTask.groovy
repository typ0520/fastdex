package com.dx168.fastdex.build.task

import com.dx168.fastdex.build.variant.FastdexVariant
import groovy.xml.Namespace
import groovy.xml.QName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.dx168.fastdex.build.util.FileUtils
import com.dx168.fastdex.build.util.FastdexUtils

/**
 * 替换项目的Application为com.dx168.fastdex.runtime.FastdexApplication
 * 并且在Manifest文件里中添加下面的节点
 * <meta-data android:name="FASTDEX_ORIGIN_APPLICATION_CLASSNAME" android:value="${项目真正的Application}"/>
 *
 * Created by tong on 17/3/11.
 */
public class FastdexManifestTask extends DefaultTask {
    static final String MANIFEST_XML = "AndroidManifest.xml"
    static final String FASTDEX_ORIGIN_APPLICATION_CLASSNAME = "FASTDEX_ORIGIN_APPLICATION_CLASSNAME"

    FastdexVariant fastdexVariant

    FastdexManifestTask() {
        group = 'fastdex'
    }

    @TaskAction
    def updateManifest() {
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

        def xml = new XmlParser().parse(new InputStreamReader(new FileInputStream(fastdexVariant.manifestPath), "utf-8"))

        def application = xml.application[0]
        if (application) {
            QName nameAttr = new QName("http://schemas.android.com/apk/res/android", 'name', 'android');
            def applicationName = application.attribute(nameAttr)
            if (applicationName == null || applicationName.isEmpty()) {
                applicationName = "android.app.Application"
            }
            application.attributes().put(nameAttr, "com.dx168.fastdex.runtime.FastdexApplication")

            def metaDataTags = application['meta-data']

            // remove any old FASTDEX_ORIGIN_APPLICATION_CLASSNAME elements
            def originApplicationName = metaDataTags.findAll {
                it.attributes()[ns.name].equals(FASTDEX_ORIGIN_APPLICATION_CLASSNAME)
            }.each {
                it.parent().remove(it)
            }

            // Add the new FASTDEX_ORIGIN_APPLICATION_CLASSNAME element
            application.appendNode('meta-data', [(ns.name): FASTDEX_ORIGIN_APPLICATION_CLASSNAME, (ns.value): applicationName])

            // Write the manifest file
            def printer = new XmlNodePrinter(new PrintWriter(fastdexVariant.manifestPath, "utf-8"))
            printer.preserveWhitespace = true
            printer.print(xml)
        }
        File manifestFile = new File(fastdexVariant.manifestPath)
        if (manifestFile.exists()) {
            File buildDir = FastdexUtils.getBuildDir(project,fastdexVariant.variantName)
            FileUtils.copyFileUsingStream(manifestFile, new File(buildDir,MANIFEST_XML))
            project.logger.error("fastdex gen AndroidManifest.xml in ${MANIFEST_XML}")
        }
    }
}

