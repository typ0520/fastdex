package fastdex.idea.models;

import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import fastdex.idea.utils.LogUtil;
import fastdex.idea.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by pengwei on 2016/10/31.
 */
public class FastdexStatus {
    Collection<VirtualFile> gradleBuildFiles;
    boolean existClasspath = false;
    private VirtualFile classpathFile;
    boolean existPlugin = false;
    private VirtualFile pluginFile;
    private Module selectedModule;
    private GradleBuildFile moduleBuildFile;
    private List<Module> moduleList;

    public FastdexStatus setClasspathFile(VirtualFile classpathFile) {
        this.classpathFile = classpathFile;
        if (classpathFile != null) {
            existClasspath = true;
        }
        return this;
    }

    public FastdexStatus setPluginFile(VirtualFile pluginFile) {
        this.pluginFile = pluginFile;
        if (pluginFile != null) {
            existPlugin = true;
        }
        return this;
    }

    public boolean isExistClasspath() {
        return existClasspath;
    }

    public VirtualFile getClasspathFile() {
        return classpathFile;
    }

    public boolean isExistPlugin() {
        return existPlugin;
    }

    public VirtualFile getPluginFile() {
        return pluginFile;
    }

    /**
     * 是否初始化Fastdex
     * 满足一下两个条件
     * 1. 存在classpath 'com.github.typ0520:fastdex-gradle:*'
     * 2. 存在apply plugin: 'fastdex.app'
     *
     * @return
     */
    public boolean hasInitFastdex() {
        LogUtil.d("existClasspath=%s,existPlugin=%s", existClasspath, existPlugin);
        return existClasspath && existPlugin;
    }

    public Collection<VirtualFile> getGradleBuildFiles() {
        return gradleBuildFiles;
    }

    public FastdexStatus setGradleBuildFiles(Collection<VirtualFile> gradleBuildFiles) {
        this.gradleBuildFiles = gradleBuildFiles;
        return this;
    }

    public void setSelectedModule(Module selectedModule) {
        this.selectedModule = selectedModule;
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public void setModuleBuildFile(GradleBuildFile moduleBuildFile) {
        this.moduleBuildFile = moduleBuildFile;
    }

    public GradleBuildFile getModuleBuildFile() {
        return moduleBuildFile;
    }

    public boolean hasOneAppplicationModule() {
        return getModuleList() != null && getModuleList().size() == 1;
    }

    public ArrayList<String> getFastdexRunShell(Project project) {
        ArrayList<String> shell = new ArrayList<String>();

        File projectDir = new File(project.getBasePath());
        String projectPath = project.getBasePath();
        if (!projectPath.endsWith(File.separator)) {
            projectPath += File.separator;
        }

        boolean hasGradlewWrapper = false;

        if (SystemInfo.isWindows && new File(projectDir,"gradlew.bat").exists()) {
            hasGradlewWrapper = true;
        }
        if (!SystemInfo.isWindows && new File(projectDir,"gradlew").exists()) {
            hasGradlewWrapper = true;
        }
        if (hasGradlewWrapper) {
            if (SystemInfo.isWindows) {
                if (hasOneAppplicationModule()) {
                    shell.add("gradlew.bat");
                }
                else {
                    shell.add(projectPath + "gradlew.bat");
                }
            }
            else {
                shell.add("sh");

                if (hasOneAppplicationModule()) {
                    shell.add("gradlew");
                }
                else {
                    shell.add(projectPath + "gradlew");
                }
            }
        }
        else {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(new String[]{"gradle", "--version"});
                if (process.waitFor() == 0) {
                    shell.add("gradle");
                }
            } catch (Exception e) {
                return null;
            }
        }

        String variantName = Utils.getBuildVariantName(selectedModule);
        shell.add("fastdex" + String.valueOf(variantName.charAt(0)).toUpperCase() + variantName.substring(1));
        shell.add("--i");
        return shell;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
    }

    public List<Module> getModuleList() {
        return moduleList;
    }
}
