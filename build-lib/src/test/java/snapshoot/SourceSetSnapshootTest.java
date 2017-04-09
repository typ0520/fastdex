package snapshoot;

import com.dx168.fastdex.build.snapshoot.file.FileNode;
import com.dx168.fastdex.build.snapshoot.sourceset.JavaDirectorySnapshoot;
import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetDiffResultSet;
import com.dx168.fastdex.build.snapshoot.sourceset.SourceSetSnapshoot;
import junit.framework.TestCase;
import org.junit.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by tong on 17/3/31.
 */
public class SourceSetSnapshootTest extends TestCase {
    String workDir;
    String source_set1;
    String source_set2;
    String source_set11;
    String source_set22;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File currentPath = new File(this.getClass().getResource("/").getPath());
        System.out.println(currentPath);

        workDir = "/Users/tong/Desktop/sourceSetTest";
        source_set1 = workDir + File.separator + "source_set1";
        source_set2 = workDir + File.separator + "source_set2";
        source_set11 = workDir + File.separator + "source_set11";
        source_set22 = workDir + File.separator + "source_set22";
    }

    @Test
    public void testCreate() throws Throwable {
        if (!isDir(source_set1) || !isDir(source_set2) || !isDir(source_set11) || !isDir(source_set22)) {
            System.err.println("Test-env not init!!");
        }

        SourceSetSnapshoot snapshoot = new SourceSetSnapshoot(new File(workDir),source_set1,source_set2);
        assertEquals(snapshoot.directorySnapshootSet.size(),2);
        SourceSetSnapshoot snapshoot2 = new SourceSetSnapshoot(new File(workDir),source_set1,source_set1);
        assertEquals(snapshoot2.directorySnapshootSet.size(),1);
    }

    @Test
    public void testDiffAddOneSourceSet() throws Throwable {
        if (!isDir(source_set1) || !isDir(source_set2) || !isDir(source_set11) || !isDir(source_set22)) {
            System.err.println("Test-env not init!!");
        }
        SourceSetSnapshoot now = new SourceSetSnapshoot(new File(workDir),source_set1,source_set2);
        SourceSetSnapshoot old = new SourceSetSnapshoot(new File(workDir),source_set1);

        SourceSetDiffResultSet sourceSetResultSet = (SourceSetDiffResultSet) now.diff(old);
        assertTrue(sourceSetResultSet.isJavaFileChanged());


        System.out.println(sourceSetResultSet);
    }

    @Test
    public void testSave() throws Throwable {
        if (!isDir(source_set1) || !isDir(source_set2) || !isDir(source_set11) || !isDir(source_set22)) {
            System.err.println("Test-env not init!!");
        }
        SourceSetSnapshoot now = new SourceSetSnapshoot(new File(workDir),source_set1,source_set2);
        now.serializeTo(new FileOutputStream(new File(workDir,"now.json")));
    }

    @Test
    public void testDiff1() throws Throwable {
        if (!isDir(source_set1) || !isDir(source_set2) || !isDir(source_set11) || !isDir(source_set22)) {
            System.err.println("Test-env not init!!");
        }

        SourceSetSnapshoot now = new SourceSetSnapshoot(new File(workDir),source_set1);
        SourceSetSnapshoot old = new SourceSetSnapshoot(new File(workDir),source_set11);

        SourceSetDiffResultSet sourceSetResultSet = (SourceSetDiffResultSet) now.diff(old);
        System.out.println(sourceSetResultSet.toString());
        sourceSetResultSet.serializeTo(new FileOutputStream(new File(workDir,"diff.json")));
    }

    @Test
    public void testDiff2() throws Throwable {
        if (!isDir(source_set1) || !isDir(source_set2) || !isDir(source_set11) || !isDir(source_set22)) {
            System.err.println("Test-env not init!!");
        }

        SourceSetSnapshoot now = new SourceSetSnapshoot(new File(workDir),source_set1);
        now.serializeTo(new FileOutputStream(new File(workDir,"snapshoot.json")));

        SourceSetSnapshoot old = (SourceSetSnapshoot) SourceSetSnapshoot.load(new File(workDir,"snapshoot.json"),SourceSetSnapshoot.class);
        JavaDirectorySnapshoot javaDirectorySnapshoot = new ArrayList<>(old.directorySnapshootSet).get(0);
        FileNode fileNode = new ArrayList<>(javaDirectorySnapshoot.nodes).get(0);
        fileNode.lastModified = System.currentTimeMillis();

        SourceSetDiffResultSet resultSet = (SourceSetDiffResultSet) now.diff(old);

        assertEquals(resultSet.changedJavaFileDiffInfos.size(),1);
        System.out.println(resultSet);
    }

    @Test
    public void testDiff3() throws Throwable {
        if (!isDir(source_set1) || !isDir(source_set2) || !isDir(source_set11) || !isDir(source_set22)) {
            System.err.println("Test-env not init!!");
        }

        SourceSetSnapshoot now = new SourceSetSnapshoot(new File("/Users/tong/Projects/fastdex/DevSample/app"),"");
    }


    public boolean isDir(File dir) {
        if (dir == null) {
            return false;
        }

        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        return true;
    }

    public boolean isDir(String dir) {
        if (dir == null) {
            return false;
        }
        return isDir(new File(dir));
    }
}
