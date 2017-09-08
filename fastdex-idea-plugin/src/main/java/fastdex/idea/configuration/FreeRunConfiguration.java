package fastdex.idea.configuration;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import fastdex.idea.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fastdex.idea.utils.FastdexUtil;

/**
 * Fastdex run configuration implementation
 *
 * @author act262@gmail.com
 */
class FreeRunConfiguration extends RunConfigurationBase {

    FreeRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        // setting editor ui
        return new FreeSettingEditor();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (!FastdexUtil.hadInitFastdex(getProject())) {
            throw new RuntimeConfigurationException("Not yet initialize fastdex code", "Warning");
        }
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new FreeRunState(executionEnvironment);
    }


    /**
     * RunState
     */
    private class FreeRunState extends CommandLineState {

        FreeRunState(ExecutionEnvironment environment) {
            super(environment);
        }

        @NotNull
        @Override
        protected ProcessHandler startProcess() throws ExecutionException {
            if (!FastdexUtil.hadInitFastdex(getProject())) {
                throw new CantRunException("Not yet initialized fastdex code");
            }
            GeneralCommandLine commandLine = new GeneralCommandLine();
            ExecutionEnvironment environment = getEnvironment();
            commandLine.setWorkDirectory(environment.getProject().getBasePath());

            if (SystemInfo.isWindows) {
                commandLine.setExePath("gradlew.bat");
                commandLine.addParameters(Utils.getBuildVariantName(getProject()));
            }
            else {
                commandLine.setExePath("sh");
                commandLine.addParameters("gradlew",Utils.getBuildVariantName(getProject()));
            }

            return new OSProcessHandler(commandLine);
        }

        @Nullable
        @Override
        protected ConsoleView createConsole(@NotNull Executor executor) throws ExecutionException {
            ConsoleView console = super.createConsole(executor);
            // before run new task,clean log
            if (console != null) {
                console.clear();
            }
            return console;
        }
    }
}
