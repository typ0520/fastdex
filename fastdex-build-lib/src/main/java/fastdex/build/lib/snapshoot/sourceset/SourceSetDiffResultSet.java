package fastdex.build.lib.snapshoot.sourceset;

import fastdex.build.lib.snapshoot.api.DiffResultSet;
import fastdex.build.lib.snapshoot.api.Status;
import fastdex.build.lib.snapshoot.string.StringDiffInfo;
import com.google.gson.annotations.Expose;

import org.apache.tools.ant.taskdefs.condition.Os;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tong on 17/3/31.
 */
public class SourceSetDiffResultSet extends DiffResultSet<StringDiffInfo> {
    public Set<JavaFileDiffInfo> changedJavaFileDiffInfos = new HashSet<JavaFileDiffInfo>();

    @Expose
    public Set<String> addOrModifiedClasses = new HashSet<>();

    @Expose
    public Set<PathInfo> addOrModifiedPathInfos = new HashSet<>();

    @Expose
    public Map<String,List<String>> addOrModifiedClassesMap = new HashMap<>();

    public SourceSetDiffResultSet() {

    }

    public SourceSetDiffResultSet(SourceSetDiffResultSet resultSet) {
        super(resultSet);
        //from gson
        this.changedJavaFileDiffInfos.addAll(resultSet.changedJavaFileDiffInfos);
        this.addOrModifiedClasses.addAll(resultSet.addOrModifiedClasses);
        this.addOrModifiedPathInfos.addAll(resultSet.addOrModifiedPathInfos);
    }

    public boolean isJavaFileChanged() {
        return !addOrModifiedClasses.isEmpty();
    }

//    public void addJavaFileDiffInfo(JavaFileDiffInfo diffInfo) {
//        if (diffInfo.status != Status.NOCHANGED) {
//            this.changedJavaFileDiffInfos.add(diffInfo);
//        }
//    }

    public void mergeJavaDirectoryResultSet(String path,JavaDirectoryDiffResultSet javaDirectoryResultSet) {
        List<String> addOrModifiedClassRelativePathList = addOrModifiedClassesMap.get(javaDirectoryResultSet.projectPath);
        if (addOrModifiedClassRelativePathList == null) {
            addOrModifiedClassRelativePathList = new ArrayList<>();
            addOrModifiedClassesMap.put(javaDirectoryResultSet.projectPath,addOrModifiedClassRelativePathList);
        }

        for (JavaFileDiffInfo javaFileDiffInfo : javaDirectoryResultSet.changedDiffInfos) {
            switch (javaFileDiffInfo.status) {
                case ADDED:
                case MODIFIED:
                    addOrModifiedPathInfos.add(new PathInfo(new File(path,javaFileDiffInfo.uniqueKey),javaFileDiffInfo.uniqueKey));
                    String classRelativePath = javaFileDiffInfo.uniqueKey.substring(0, javaFileDiffInfo.uniqueKey.length() - ".java".length());

//                    String entryName = classRelativePath;
//                    if (entryName.contains("\\")) {
//                        entryName = entryName.replace("\\", "/");
//                    }
//                    entryName = entryName + ".class";
//                    addOrModifiedClassRelativePathList.add(entryName);
                    addOrModifiedClassRelativePathList.add(classRelativePath + ".class");
                    addOrModifiedClassRelativePathList.add(classRelativePath + "$*.class");

                    classRelativePath = classRelativePath.replaceAll(Os.isFamily(Os.FAMILY_WINDOWS) ? "\\\\" : File.separator,"\\.");
                    addOrModifiedClasses.add(classRelativePath);

//                    addOrModifiedClasses.add(classRelativePath + ".class");
//                    addOrModifiedClasses.add(classRelativePath + "\\$\\S{0,}.class");
                    break;
            }
        }
        this.changedJavaFileDiffInfos.addAll(javaDirectoryResultSet.changedDiffInfos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SourceSetDiffResultSet resultSet = (SourceSetDiffResultSet) o;

        return changedJavaFileDiffInfos != null ? changedJavaFileDiffInfos.equals(resultSet.changedJavaFileDiffInfos) : resultSet.changedJavaFileDiffInfos == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (changedJavaFileDiffInfos != null ? changedJavaFileDiffInfos.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SourceSetDiffResultSet{" +
                "changedJavaFileDiffInfos=" + changedJavaFileDiffInfos +
                ", addOrModifiedClasses=" + addOrModifiedClasses +
                ", addOrModifiedPathInfos=" + addOrModifiedPathInfos +
                '}';
    }
}
