package fastdex.idea.utils;

import com.android.tools.idea.gradle.dsl.model.GradleBuildModel;
import com.android.tools.idea.gradle.dsl.model.dependencies.ArtifactDependencyModel;
import com.android.tools.idea.gradle.dsl.model.dependencies.ArtifactDependencySpec;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.run.AndroidRunConfiguration;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import fastdex.idea.actions.UpdateAction;
import fastdex.idea.models.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fastdex Utility
 *
 * @author act262@gmail.com
 */
public class FastdexUtil {
    /**
     * if had init fastdex return true
     */
    public static boolean hadInitFastdex(Project project) {
        return true;
    }

    /**
     * 获取fastdex安装状态
     *
     * @param project
     * @param currentSelectedAndroidRunConfiguration
     *@param selectModulesList @return
     */
    public static FastdexStatus getFastdexStatus(@NotNull Project project, AndroidRunConfiguration currentSelectedAndroidRunConfiguration, List<Pair<Module, GradleBuildFile>> selectModulesList) {
        FastdexStatus status = new FastdexStatus();
        Collection<VirtualFile> gradleFiles = GradleUtil.getAllGradleFile(project);
        status.setGradleBuildFiles(gradleFiles);

        if (selectModulesList != null && selectModulesList.size() == 1) {
            status.setSelectedModule(selectModulesList.get(0).getFirst());
            status.setModuleBuildFile(selectModulesList.get(0).getSecond());
        }
        else {
            status.setSelectedModule(currentSelectedAndroidRunConfiguration.getConfigurationModule().getModule());
            status.setModuleBuildFile(GradleBuildFile.get(status.getSelectedModule()));
        }

        for (VirtualFile file : gradleFiles) {
            if (!status.isExistClasspath()) {
                GradleBuildModel model = GradleBuildModel.parseBuildFile(file, project);
                if (model != null) {
                    List<ArtifactDependencyModel> classPaths = model.buildscript().dependencies().artifacts();
                    for (ArtifactDependencyModel classpath : classPaths) {
                        ArtifactDependencyModelWrapper wrapper = new ArtifactDependencyModelWrapper(classpath);
                        if (wrapper.group().equals(Constant.FASTDEX_CLASSPATH_GROUP)
                                && wrapper.name().equals(Constant.FASTDEX_CLASSPATH_ARTIFACT)) {
                            status.setFastdexVersion(wrapper.version());
                            status.setClasspathFile(file);
                            break;
                        }
                    }
                }
            }
            // 正则二次判断是否存在Fastdex classpath
            if (!status.isExistClasspath() && regularExistFastdexClassPath(file)) {
                status.setClasspathFile(file);
            }
            if (!status.isExistPlugin()) {
                GradleBuildFile gradleBuildFile = new GradleBuildFile(file, project);

                if (isExpectedGradleBuildFile(gradleBuildFile,currentSelectedAndroidRunConfiguration,selectModulesList)) {
                    List<String> plugins = gradleBuildFile.getPlugins();
                    if (plugins.contains(Constant.FASTDEX_PLUGIN_ID)) {
                        status.setPluginFile(file);
                    }
                }
            }
            if (status.isExistClasspath() && status.isExistPlugin()) {
                break;
            }
        }
        return status;
    }

    private static boolean isExpectedGradleBuildFile(GradleBuildFile gradleBuildFile, AndroidRunConfiguration currentSelectedAndroidRunConfiguration, List<Pair<Module, GradleBuildFile>> selectModulesList) {
        if (gradleBuildFile == null) {
            return false;
        }
        boolean onlyOneApplicationModule = selectModulesList != null && selectModulesList.size() == 1;
        if (onlyOneApplicationModule) {
            return true;
        }

        for (Pair<Module, GradleBuildFile> pair : selectModulesList) {
            if (pair.getFirst() == currentSelectedAndroidRunConfiguration.getConfigurationModule().getModule()) {
                GradleBuildFile file = GradleBuildFile.get(pair.getFirst());

                if (file != null && (file == gradleBuildFile || file.getFile().getPath().equals(gradleBuildFile.getFile().getPath()))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查是否需要载入Fastdex
     *
     * @param project
     * @return
     */
    public static FastdexStatus checkInstall(@NotNull final Project project) {
        if (GradleUtil.isSyncInProgress(project)) {
            NotificationUtils.errorMsgDialog("Waiting for sync project to complete");
            return null;
        }

        final RunnerAndConfigurationSettings selectedConfiguration = RunManager.getInstance(project).getSelectedConfiguration();
        AndroidRunConfiguration currentSelectedAndroidRunConfiguration = null;
        if (selectedConfiguration.getConfiguration() instanceof AndroidRunConfiguration) {
            currentSelectedAndroidRunConfiguration = (AndroidRunConfiguration) selectedConfiguration.getConfiguration();
        }

        List<Module> modules = GradleUtil.getGradleModulesWithAndroidProjects(project);
        List<Pair<Module, GradleBuildFile>> selectModulesList = new ArrayList<Pair<Module, GradleBuildFile>>();
        for (Module module : modules) {
            GradleBuildFile file = GradleBuildFile.get(module);
            if (file == null) {
                NotificationUtils.errorMsgDialog(module.getName() + " miss build.gradle");
            } else {
                if (!GradleUtil.isLibrary(file)) {
                    selectModulesList.add(Pair.create(module, file));
                }
            }
        }

        if (selectModulesList.isEmpty()) {
            NotificationUtils.errorMsgDialog("Can not found Application Module! Please Sync Project.");
            return null;
        }

        if (selectModulesList.size() > 1 && currentSelectedAndroidRunConfiguration == null) {
            NotificationUtils.errorMsgDialog("Please select the application first");
            return null;
        }

        /**
         *
         * 首选判断有几个android application moudle
         *   只有一个               ================
         *     判断有没有加载gradle配置              |
         *   有多个moudle，获取当前选中的是哪个        |
         *      选中的不是android moudle            |
         *        报错，提示请选择调试模块            |
         *      是android moudle   ================
         *
         *
         */

        final FastdexStatus status = getFastdexStatus(project,currentSelectedAndroidRunConfiguration,selectModulesList);
        if (status.getGradleBuildFiles().size() < 1) {
            NotificationUtils.errorMsgDialog("It's not an Android Gradle project Currently?");
            return null;
        }

        status.setModuleList(modules);

        if (status.hasInitFastdex()) {
            return status;
        }

        if (DialogUtil.createDialog("Detected that " + status.getSelectedModule().getName() + " did not fastdex, Whether apply fastdex plugin Automatically？",
                "Apply fastdex plugin Automatically", "Cancel")) {
            installFastdex(project, status, status.getModuleBuildFile().getPsiFile());
        }
        return null;
    }

    /**
     * 载入Fastdex
     *
     * @param project
     * @param status
     * @param psiFile
     */
    private static void installFastdex(final Project project, final FastdexStatus status, final PsiFile psiFile) {
        ApplicationManager.getApplication().executeOnPooledThread(new UpdateAction.GetServerVersion(new GetServerCallback() {
            @Override
            public void onSuccess(final GradleDependencyEntity entity) {
                LogUtil.d("获取版本号成功:" + entity);
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        installFastdex(project, status, psiFile, entity);
                    }
                });
            }

            @Override
            public void onFailure(String errMsg) {
                LogUtil.d("获取版本号失败:" + errMsg);
                NotificationUtils.errorNotification("Get Fastdex Version Failure: " + errMsg);
            }
        }));
    }

    private static boolean needReformatCode = false;

    private static void installFastdex(final Project project, final FastdexStatus status, final PsiFile psiFile,
                                       final GradleDependencyEntity dependencyEntity) {
        needReformatCode = false;
        CommandProcessor.getInstance().runUndoTransparentAction(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        if (!status.isExistClasspath()) {
                            Collection<VirtualFile> collection = status.getGradleBuildFiles();
                            if (dependencyEntity != null) {
                                for (VirtualFile file : collection) {
                                    GradleBuildModel model = GradleBuildModel.parseBuildFile(file, project);
                                    List<ArtifactDependencyModel> artifactDependencyModels = model.buildscript().dependencies().artifacts();
                                    for (ArtifactDependencyModel model1 : artifactDependencyModels) {
                                        ArtifactDependencyModelWrapper wrapper = new ArtifactDependencyModelWrapper(model1);
                                        if (wrapper.group().equals(Constant.ANDROID_GRADLE_TOOL_GROUP_NAME)) {
                                            ArtifactDependencySpec spec = new ArtifactDependencySpec(dependencyEntity.getArtifactId(),
                                                    dependencyEntity.getGroupId(), dependencyEntity.getNewestReleaseVersion());
                                            model.buildscript().dependencies().addArtifact("classpath", spec);
                                            model.applyChanges();
                                            needReformatCode = true;
                                            status.setClasspathFile(file);
                                            break;
                                        }
                                    }
                                    if (status.isExistClasspath()) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (!status.isExistPlugin()) {
                            if (psiFile != null && psiFile instanceof GroovyFile) {
                                GradleUtil.applyPlugin(project, (GroovyFile) psiFile, Constant.FASTDEX_PLUGIN_ID);
                            }
                        }
                    }
                });
            }
        });
        if (needReformatCode && status.getClasspathFile() != null) {
            DocumentUtil.reformatCode(project, status.getClasspathFile());
        }
    }

    public static final Pattern PATTERN_CLASSPATH = Pattern.compile("classpath\\s+'"
            + Constant.FASTDEX_CLASSPATH_GROUP + ":" + Constant.FASTDEX_CLASSPATH_ARTIFACT + ":[\\d|\\.]*'");

    /**
     * Fastdex classpath
     *
     * @param file
     * @return
     */
    public static boolean regularExistFastdexClassPath(VirtualFile file) {
        try {
            if (file.exists()) {
                String content = FileUtils.readFileToString(new File(file.getPath()));
                Matcher matcher = PATTERN_CLASSPATH.matcher(content);
                return matcher.find();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
