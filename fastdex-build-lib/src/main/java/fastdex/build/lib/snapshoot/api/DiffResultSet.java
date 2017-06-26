package fastdex.build.lib.snapshoot.api;

import fastdex.common.utils.SerializeUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tong on 17/3/30.
 */
public class DiffResultSet<T extends DiffInfo> implements STSerializable {
    public Set<T> changedDiffInfos = new HashSet<T>();
    public Set<T> nochangedDiffInfos = new HashSet<T>();

    public DiffResultSet() {
    }

    public DiffResultSet(DiffResultSet resultSet) {
        changedDiffInfos.addAll(resultSet.changedDiffInfos);
        nochangedDiffInfos.addAll(resultSet.nochangedDiffInfos);
    }

    /**
     * 添加对比信息
     * @param diffInfo
     * @return
     */
    public boolean add(T diffInfo) {
        if (diffInfo == null) {
            return false;
        }
        if (diffInfo.status == Status.NOCHANGED) {
            if (nochangedDiffInfos == null) {
                nochangedDiffInfos = new HashSet<T>();
            }
            return nochangedDiffInfos.add(diffInfo);
        }
        if (changedDiffInfos == null) {
            changedDiffInfos = new HashSet<T>();
        }
        return changedDiffInfos.add(diffInfo);
    }

    /**
     * 合并结果集
     * @param resultSet
     */
    public void merge(DiffResultSet<T> resultSet) {
        if (changedDiffInfos == null) {
            changedDiffInfos = new HashSet<T>();
        }
        changedDiffInfos.addAll(resultSet.changedDiffInfos);
    }

    /**
     * 获取所有发生变化的结果集
     * @return
     */
    public Set<T> getAllChangedDiffInfos() {
        HashSet set =  new HashSet<>();
        set.addAll(changedDiffInfos);
        return set;
    }

    /**
     * 获取所有发生变化的结果集
     * @return
     */
    public Set<T> getAllNochangedDiffInfos() {
        HashSet set =  new HashSet<>();
        set.addAll(nochangedDiffInfos);
        return set;
    }

    public Set<T> getDiffInfos(Status ...statuses) {
        Set<T> result = new HashSet<T>();
        if (statuses == null || statuses.length == 0) {
            result.addAll(changedDiffInfos);
            result.addAll(nochangedDiffInfos);
            return result;
        }

        for (T diffInfo : changedDiffInfos) {
            bb : for (Status status : statuses) {
                if (diffInfo.status == status) {
                    result.add(diffInfo);
                    break bb;
                }
            }
        }

        boolean containNochangedStatus = false;
        for (Status status : statuses) {
            if (status == Status.NOCHANGED) {
                containNochangedStatus = true;
                break;
            }
        }
        if (containNochangedStatus) {
            result.addAll(nochangedDiffInfos);
        }
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiffResultSet<?> resultSet = (DiffResultSet<?>) o;

        return changedDiffInfos != null ? changedDiffInfos.equals(resultSet.changedDiffInfos) : resultSet.changedDiffInfos == null;

    }

    @Override
    public int hashCode() {
        return changedDiffInfos != null ? changedDiffInfos.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DiffResultSet{" +
                "changedJavaFileDiffInfos=" + changedDiffInfos +
                '}';
    }

    @Override
    public void serializeTo(OutputStream outputStream) throws IOException {
        SerializeUtils.serializeTo(outputStream,this);
    }

    public static DiffResultSet load(InputStream inputStream, Class type) throws Exception {
        DiffResultSet resultSet = (DiffResultSet) SerializeUtils.load(inputStream,type);
        if (resultSet != null) {
            Constructor constructor = type.getConstructor(resultSet.getClass());
            resultSet = (DiffResultSet) constructor.newInstance(resultSet);
        }
        return resultSet;
    }

    public static DiffResultSet load(File file, Class type) throws Exception {
        return load(new FileInputStream(file),type);
    }
}
