package fastdex.build.lib.snapshoot.api;

import fastdex.common.utils.SerializeUtils;
import com.google.gson.annotations.Expose;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by tong on 17/3/29.
 */
public class Snapshoot<DIFF_INFO extends DiffInfo,NODE extends Node> implements STSerializable {
    public Collection<NODE> nodes;

    @Expose
    private DiffResultSet<DIFF_INFO> lastDiffResult;

    public Snapshoot() {
        createEmptyNodes();
    }

    public Snapshoot(Snapshoot<DIFF_INFO,NODE> snapshoot) {
        createEmptyNodes();
        nodes.addAll(snapshoot.getAllNodes());
    }

    protected void createEmptyNodes() {
        nodes = new HashSet<NODE>();
    }

    /**
     * 创建空的对比结果集
     * @return
     */
    protected DiffResultSet<DIFF_INFO> createEmptyResultSet() {
        return new DiffResultSet<DIFF_INFO>();
    }

    protected DiffInfo createEmptyDiffInfo() {
        return new DiffInfo();
    }

    /**
     * 添加内容
     * @param code
     */
    protected void addNode(NODE code) {
        nodes.add(code);
    }

    /**
     * 获取所有的内容
     * @return
     */
    protected Collection<NODE> getAllNodes() {
        return nodes;
    }

    public DiffResultSet<DIFF_INFO> getLastDiffResult() {
        return lastDiffResult;
    }

    /**
     * 通过索引获取内容
     * @param uniqueKey
     * @return
     */
    protected NODE getNodeByUniqueKey(String uniqueKey) {
        NODE node = null;
        for (NODE n : nodes) {
            if (uniqueKey.equals(n.getUniqueKey())) {
                node = n;
                break;
            }
        }
        return node;
    }

    /**
     * 创建一项内容的对比结果
     * @param status
     * @param now
     * @param old
     * @return
     */
    protected DIFF_INFO createDiffInfo(Status status, NODE now, NODE old) {
        DIFF_INFO diffInfo = (DIFF_INFO) createEmptyDiffInfo();
        diffInfo.status = status;
        diffInfo.now = now;
        diffInfo.old = old;

        switch (status) {
            case NOCHANGED:
            case ADDED:
            case MODIFIED:
                diffInfo.uniqueKey = now.getUniqueKey();
                break;
            case DELETEED:
                diffInfo.uniqueKey = old.getUniqueKey();
                break;
        }

        return diffInfo;
    }

    /**
     * 把一项内容的对比结果添加到结果集中
     * @param diffInfos
     * @param diffInfo
     */
    protected void addDiffInfo(DiffResultSet<DIFF_INFO> diffInfos, DIFF_INFO diffInfo) {
        diffInfos.add(diffInfo);
    }

    /**
     * 对比快照
     * @param otherSnapshoot
     * @return
     */
    public DiffResultSet<DIFF_INFO> diff(Snapshoot<DIFF_INFO,NODE> otherSnapshoot) {
        //获取删除项
        Set<NODE> deletedNodes = new HashSet<>();
        deletedNodes.addAll(otherSnapshoot.getAllNodes());
        deletedNodes.removeAll(new ArrayList<NODE>(getAllNodes()));

        //新增项
        Set<NODE> increasedNodes = new HashSet<>();
        increasedNodes.addAll(getAllNodes());
        //如果不用ArrayList套一层有时候会发生移除不掉的情况 why?
        increasedNodes.removeAll(new ArrayList<NODE>(otherSnapshoot.getAllNodes()));

        //需要检测是否变化的列表
        Set<NODE> needDiffNodes = new HashSet<>();
        needDiffNodes.addAll(getAllNodes());
        needDiffNodes.addAll(otherSnapshoot.getAllNodes());
        needDiffNodes.removeAll(new ArrayList<NODE>(deletedNodes));
        needDiffNodes.removeAll(new ArrayList<NODE>(increasedNodes));

        DiffResultSet<DIFF_INFO> diffInfos = createEmptyResultSet();
        scanDeletedAndIncreased(diffInfos,otherSnapshoot,deletedNodes,new HashSet<NODE>(increasedNodes));
        scanNeedDiffNodes(diffInfos,otherSnapshoot,needDiffNodes);

        this.lastDiffResult = diffInfos;
        return diffInfos;
    }

    /**
     * 扫描变化项和删除项
     * @param diffInfos
     * @param otherSnapshoot
     * @param deletedNodes
     * @param increasedNodes
     */
    protected void scanDeletedAndIncreased(DiffResultSet<DIFF_INFO> diffInfos, Snapshoot<DIFF_INFO,NODE> otherSnapshoot, Set<NODE> deletedNodes, Set<NODE> increasedNodes) {
        if (deletedNodes != null) {
            for (NODE node : deletedNodes) {
                addDiffInfo(diffInfos,createDiffInfo(Status.DELETEED,null,node));
            }
        }
        if (increasedNodes != null) {
            for (NODE node : increasedNodes) {
                addDiffInfo(diffInfos,createDiffInfo(Status.ADDED,node,null));
            }
        }
    }

    /**
     * 对比两个node
     * @param diffInfos
     * @param otherSnapshoot
     * @param now
     * @param old
     */
    protected void diffNode(DiffResultSet<DIFF_INFO> diffInfos, Snapshoot<DIFF_INFO, NODE> otherSnapshoot, NODE now, NODE old) {
        if (now.diffEquals(old)) {
            addDiffInfo(diffInfos,createDiffInfo(Status.NOCHANGED,now,old));
        }
        else {
            addDiffInfo(diffInfos,createDiffInfo(Status.MODIFIED,now,old));
        }
    }

    /**
     * 扫描uniqueKey相同的node
     * @param diffInfos
     * @param otherSnapshoot
     * @param needDiffNodes
     */
    protected void scanNeedDiffNodes(DiffResultSet<DIFF_INFO> diffInfos, Snapshoot<DIFF_INFO, NODE> otherSnapshoot, Set<NODE> needDiffNodes) {
        if (needDiffNodes == null || needDiffNodes.isEmpty()) {
            return;
        }
        for (NODE node : needDiffNodes) {
            NODE now = node;
            String uniqueKey = node.getUniqueKey();
            if (uniqueKey == null || uniqueKey.length() == 0) {
                throw new RuntimeException("UniqueKey can not be null or empty!!");
            }
            NODE old = otherSnapshoot.getNodeByUniqueKey(uniqueKey);
            diffNode(diffInfos,otherSnapshoot,now,old);
        }
    }

    @Override
    public void serializeTo(OutputStream outputStream) throws IOException {
        SerializeUtils.serializeTo(outputStream,this);
    }

    public static Snapshoot load(InputStream inputStream, Class type) throws Exception {
        Snapshoot snapshoot = (Snapshoot) SerializeUtils.load(inputStream,type);
        if (snapshoot != null) {
            Constructor constructor = type.getConstructor(snapshoot.getClass());
            snapshoot = (Snapshoot) constructor.newInstance(snapshoot);
        }
        return snapshoot;
    }

    public static Snapshoot load(File path, Class type) throws Exception {
        Snapshoot snapshoot = (Snapshoot) SerializeUtils.load(new FileInputStream(path),type);
        if (snapshoot != null) {
            Constructor constructor = type.getConstructor(snapshoot.getClass());
            snapshoot = (Snapshoot) constructor.newInstance(snapshoot);
        }
        return snapshoot;
    }
}
