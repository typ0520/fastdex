package fastdex.build.util

import com.android.build.api.transform.Transform
import fastdex.build.variant.FastdexVariant
import fastdex.common.utils.FileUtils
import org.apache.tools.ant.taskdefs.condition.Os

/**
 * dex操作
 * Created by tong on 17/11/4.
 */
public class DexOperation {
    /**
     * 生成补丁dex
     * @param fastdexVariant
     * @param base
     * @param patchJar
     * @param patchDex
     */
    public static final void generatePatchDex(FastdexVariant fastdexVariant, Transform base,File patchJar,File patchDex) {
        FileUtils.deleteFile(patchDex)
        FileUtils.ensumeDir(patchDex.parentFile)

        long start = System.currentTimeMillis()
        List<String> cmdArgs = new ArrayList<>()

        //TODO 补丁的方法数也有可能超过65535个，最好加上使dx生成多个dex的参数，但是一般补丁不会那么大所以暂时不处理
        if (Os.isFamily(Os.FAMILY_WINDOWS) || fastdexVariant.project.projectDir.absolutePath.contains(" ")) {
            //调用dx命令
            cmdArgs.add(FastdexUtils.getDxCmdPath(fastdexVariant.project))
            cmdArgs.add("--dex")
            cmdArgs.add("--output=${patchDex}")
            cmdArgs.add(patchJar.absolutePath)
        }
        else {
            File dxJarFile = new File(FastdexUtils.getBuildDir(fastdexVariant.project),"fastdex-dx.jar")
            File dxCommandFile = new File(FastdexUtils.getBuildDir(fastdexVariant.project),"fastdex-dx")

            if (!FileUtils.isLegalFile(dxJarFile)) {
                FileUtils.copyResourceUsingStream("fastdex-dx.jar",dxJarFile)
            }

            if (!FileUtils.isLegalFile(dxCommandFile)) {
                FileUtils.copyResourceUsingStream("fastdex-dx",dxCommandFile)
            }

            cmdArgs.add("sh");
            cmdArgs.add(dxCommandFile.absolutePath);
            cmdArgs.add("--dex");
            cmdArgs.add("--no-optimize");
            cmdArgs.add("--force-jumbo");
            cmdArgs.add("--output=${patchDex}");
            cmdArgs.add(patchJar.absolutePath);
        }

        //调用dx命令
        FastdexUtils.runCommand(fastdexVariant.project, cmdArgs)
        long end = System.currentTimeMillis();
        fastdexVariant.project.logger.error("==fastdex patch transform generate dex success. use: ${end - start}ms")
    }

    /**
     * 合并dex
     * @param fastdexVariant
     * @param outputDex     输出的dex路径
     * @param patchDex      补丁dex路径
     * @param cachedDex
     */
    public static void mergeDex(FastdexVariant fastdexVariant,File outputDex,File patchDex,File cachedDex) {
        long start = System.currentTimeMillis()
        def project = fastdexVariant.project
        File dexMergeJar = new File(FastdexUtils.getBuildDir(project),Constants.DEX_MERGE_JAR_FILENAME)
        if (!FileUtils.isLegalFile(dexMergeJar)) {
            FileUtils.copyResourceUsingStream(Constants.DEX_MERGE_JAR_FILENAME,dexMergeJar)
        }

        List<String> cmdArgs = new ArrayList<>()
        cmdArgs.add(FastdexUtils.getJavaCmdPath());
        cmdArgs.add("-jar");
        cmdArgs.add(dexMergeJar.absolutePath);
        cmdArgs.add(outputDex.absolutePath);
        cmdArgs.add(patchDex.absolutePath);
        cmdArgs.add(cachedDex.absolutePath);

        FastdexUtils.runCommand(fastdexVariant.project, cmdArgs)

        long end = System.currentTimeMillis();
        project.logger.error("==fastdex merge dex success. use: ${end - start}ms")
    }
}
