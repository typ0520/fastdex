package fastdex.build.lib.snapshoot.api;

/**
 * 目录对比，file.length或者file.lastModified不一样时判定文件发生变化
 * Created by tong on 17/3/29.
 */
public class DiffInfo<T extends Node> {
    public Status status;
    public String uniqueKey;
    public T now;//如果是删除此值为null
    public T old;

    public DiffInfo() {
    }

    public DiffInfo(Status status, String uniqueKey, T now, T old) {
        this.status = status;
        this.uniqueKey = uniqueKey;

        this.now = now;
        this.old = old;

        if (this.uniqueKey == null || this.uniqueKey.length() == 0) {
            throw new IllegalStateException("UniqueKey can not be null or epmty!!");
        }
    }

    @Override
    public String toString() {
        return "DiffInfo{" +
                "status=" + status +
                ", uniqueKey='" + uniqueKey + '\'' +
                ", now=" + now +
                ", old=" + old +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiffInfo that = (DiffInfo) o;

        if (status != that.status) return false;
        return uniqueKey != null ? uniqueKey.equals(that.uniqueKey) : that.uniqueKey == null;

    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (uniqueKey != null ? uniqueKey.hashCode() : 0);
        return result;
    }
}
