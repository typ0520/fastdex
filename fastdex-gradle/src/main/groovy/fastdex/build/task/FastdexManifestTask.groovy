package fastdex.build.task

import fastdex.build.util.GradleUtils
import fastdex.build.variant.FastdexVariant
import groovy.xml.Namespace
import groovy.xml.QName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 替换项目的Application为fastdex.runtime.FastdexApplication
 * 并且在Manifest文件里中添加下面的节点
 * <meta-data android:name="FASTDEX_ORIGIN_APPLICATION_CLASSNAME" android:value="${项目真正的Application}"/>
 *
 * Created by tong on 17/3/11.
 */
class FastdexManifestTask extends DefaultTask {
    static final String FASTDEX_ORIGIN_APPLICATION_CLASSNAME = "FASTDEX_ORIGIN_APPLICATION_CLASSNAME"
    static final String FASTDEX_BOOT_ACTIVITY_CLASSNAME = "FASTDEX_BOOT_ACTIVITY_CLASSNAME"
    static final String MIDDLEWARE_ACTIVITY = "fastdex.runtime.MiddlewareActivity"
    static final String FASTDEX_SERVICE = "fastdex.runtime.FastdexService"

    FastdexVariant fastdexVariant

    FastdexManifestTask() {
        group = 'fastdex'
    }

    @TaskAction
    def updateManifest() {
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

        def xml = GradleUtils.parseXml(fastdexVariant.manifestPath)

        def application = xml.application[0]
        if (application) {
            QName nameAttr = new QName("http://schemas.android.com/apk/res/android", 'name', 'android')
            def applicationName = application.attribute(nameAttr)
            if (applicationName == null || applicationName.isEmpty()) {
                applicationName = "android.app.Application"
            }
            application.attributes().put(nameAttr, "fastdex.runtime.FastdexApplication")
            def metaDataTags = application['meta-data']

            // remove any old FASTDEX_ORIGIN_APPLICATION_CLASSNAME elements
            metaDataTags.findAll {
                it.attributes()[ns.name].equals(FASTDEX_ORIGIN_APPLICATION_CLASSNAME)
            }.each {
                it.parent().remove(it)
            }
            // Add the new FASTDEX_ORIGIN_APPLICATION_CLASSNAME element
            application.appendNode('meta-data', [(ns.name): FASTDEX_ORIGIN_APPLICATION_CLASSNAME, (ns.value): applicationName])


            String bootActivityName = GradleUtils.getBootActivityByXmlNode(xml)
            // remove any old FASTDEX_BOOT_ACTIVITY_CLASSNAME elements
            metaDataTags.findAll {
                it.attributes()[ns.name].equals(FASTDEX_BOOT_ACTIVITY_CLASSNAME)
            }.each {
                it.parent().remove(it)
            }
            // Add the new FASTDEX_BOOT_ACTIVITY_CLASSNAME element
            application.appendNode('meta-data', [(ns.name): FASTDEX_BOOT_ACTIVITY_CLASSNAME, (ns.value): bootActivityName])

            application['activity'].findAll {
                it.attributes()[ns.name].equals(MIDDLEWARE_ACTIVITY)
            }.each {
                it.parent().remove(it)
            }

            application.appendNode('activity', [(ns.name): MIDDLEWARE_ACTIVITY])

            application['service'].findAll {
                it.attributes()[ns.name].equals(FASTDEX_SERVICE)
            }.each {
                it.parent().remove(it)
            }

            application.appendNode('service', [(ns.name): FASTDEX_SERVICE,(ns.process): ":fastdex"])

            // Write the manifest file
            def printer = new XmlNodePrinter(new PrintWriter(fastdexVariant.manifestPath, "utf-8"))
            printer.preserveWhitespace = true
            printer.print(xml)
        }
    }
}

