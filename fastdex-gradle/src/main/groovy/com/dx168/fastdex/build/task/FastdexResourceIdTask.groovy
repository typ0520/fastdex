package com.dx168.fastdex.build.task

import com.dx168.fastdex.build.util.Constants
import fastdex.common.utils.FileUtils
import com.dx168.fastdex.build.variant.FastdexVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import fastdex.build.lib.aapt.AaptResourceCollector
import fastdex.build.lib.aapt.AaptUtil
import fastdex.build.lib.aapt.PatchUtil
import fastdex.build.lib.aapt.RDotTxtEntry
import com.dx168.fastdex.build.util.FastdexUtils

/**
 * 保持补丁打包时R文件中相同的节点和第一次打包时的值保持一致
 *
 * 把第一次打包时生成的build/intermediates/symbols/${variant}/R.txt保存下来，
 * 补丁打包时使用R.txt作为输入生成public.xml和ids.xml并放进build/intermediates/res/merged/${variant}/values里面
 *
 * 详情请看老罗的文章和tinker项目的实现
 * http://blog.csdn.net/luoshengyang/article/details/8744683
 * https://github.com/Tencent/tinker/tree/master/tinker-build/tinker-patch-gradle-plugin
 *
 * Created by tong on 17/3/11.
 */
public class FastdexResourceIdTask extends DefaultTask {
    FastdexVariant fastdexVariant
    String resDir

    FastdexResourceIdTask() {
        group = 'fastdex'
    }

    @TaskAction
    def applyResourceId() {
        String resourceMappingFile = FastdexUtils.getResourceMappingFile(project,fastdexVariant.variantName)

        // Parse the public.xml and ids.xml
        if (!FileUtils.isLegalFile(new File(resourceMappingFile))) {
            project.logger.error("==fastdex apply resource mapping file ${resourceMappingFile} is illegal, just ignore")
            return
        }

        File idsXmlFile = FastdexUtils.getIdxXmlFile(project,fastdexVariant.variantName)
        File publicXmlFile = FastdexUtils.getPublicXmlFile(project,fastdexVariant.variantName)

        String idsXml = resDir + "/values/ids.xml";
        String publicXml = resDir + "/values/public.xml";
        File resDirIdsXmlFile = new File(idsXml)
        File resDirPublicXmlFile = new File(publicXml)

        if (FileUtils.isLegalFile(idsXmlFile) && FileUtils.isLegalFile(publicXmlFile)) {
            if (!FileUtils.isLegalFile(resDirIdsXmlFile) || idsXmlFile.lastModified() != resDirIdsXmlFile.lastModified()) {
                FileUtils.copyFileUsingStream(idsXmlFile,resDirIdsXmlFile)
                project.logger.error("==fastdex apply cached resource idx.xml ${idsXml}")
            }

            if (!FileUtils.isLegalFile(resDirPublicXmlFile) || publicXmlFile.lastModified() != resDirPublicXmlFile.lastModified()) {
                FileUtils.copyFileUsingStream(publicXmlFile,resDirPublicXmlFile)
                project.logger.error("==fastdex apply cached resource public.xml ${publicXml}")
            }
            return
        }

        FileUtils.deleteFile(idsXml);
        FileUtils.deleteFile(publicXml);
        List<String> resourceDirectoryList = new ArrayList<String>()
        resourceDirectoryList.add(resDir)

        project.logger.error("==fastdex we build ${project.getName()} apk with apply resource mapping file ${resourceMappingFile}")
        Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = PatchUtil.readRTxt(resourceMappingFile)

        AaptResourceCollector aaptResourceCollector = AaptUtil.collectResource(resourceDirectoryList, rTypeResourceMap)
        PatchUtil.generatePublicResourceXml(aaptResourceCollector, idsXml, publicXml)
        File publicFile = new File(publicXml)

        if (publicFile.exists()) {
            FileUtils.copyFileUsingStream(publicFile, publicXmlFile)
            project.logger.error("==fastdex gen resource public.xml in ${Constants.RESOURCE_PUBLIC_XML}")
        }
        File idxFile = new File(idsXml)
        if (idxFile.exists()) {
            FileUtils.copyFileUsingStream(idxFile, idsXmlFile)
            project.logger.error("==fastdex gen resource idx.xml in ${Constants.RESOURCE_IDX_XML}")
        }
    }
}

