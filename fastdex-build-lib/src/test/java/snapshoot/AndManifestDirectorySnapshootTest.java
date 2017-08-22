package snapshoot;

import junit.framework.TestCase;
import org.junit.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import fastdex.build.lib.snapshoot.api.DiffResultSet;
import fastdex.build.lib.snapshoot.file.FileDiffInfo;
import fastdex.build.lib.snapshoot.file.FileNode;
import fastdex.build.lib.snapshoot.res.AndManifestDirectorySnapshoot;
import fastdex.common.utils.FileUtils;

/**
 * Created by tong on 17/8/22.
 */
public class AndManifestDirectorySnapshootTest extends TestCase {
    private File workDir;
    private File outputFile;

    private List<File> testManifestFiles = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        workDir = new File(new File(".test").getAbsolutePath());
        workDir.mkdir();

        outputFile = new File(workDir,"androidManifest.json");
        System.out.println(outputFile);

        testManifestFiles.add(new File(workDir,"app/src/main/AndroidManifest.xml"));
        testManifestFiles.add(new File(workDir,"common/src/main/AndroidManifest.xml"));
        testManifestFiles.add(new File(workDir,"common-group/common3/src/main/AndroidManifest.xml"));


        for (File file : testManifestFiles) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        FileUtils.deleteDir(workDir);
    }

    @Test
    public void testSave() throws Throwable {
        AndManifestDirectorySnapshoot snapshoot = new AndManifestDirectorySnapshoot();
        for (File file : testManifestFiles) {
            snapshoot.addFile(file);
        }
        snapshoot.serializeTo(new FileOutputStream(outputFile));

        assertEquals(testManifestFiles.size(),snapshoot.nodes.size());
        assertTrue(outputFile.isFile() && outputFile.exists());
    }

    @Test
    public void testMustXml() throws Throwable {
        AndManifestDirectorySnapshoot snapshoot = new AndManifestDirectorySnapshoot();
        for (File file : testManifestFiles) {
            snapshoot.addFile(file);
        }

        File notXmlFile = new File(workDir,"app/src/main/fastdex.jar");

        System.out.println(notXmlFile.lastModified());
        snapshoot.addFile(notXmlFile);

        assertEquals(testManifestFiles.size(),snapshoot.nodes.size());
    }

    @Test
    public void testModify() throws Throwable {

        AndManifestDirectorySnapshoot snapshoot = new AndManifestDirectorySnapshoot();
        for (File file : testManifestFiles) {
            snapshoot.addFile(file);
        }

        snapshoot.serializeTo(new FileOutputStream(outputFile));

        ((FileNode)snapshoot.nodes.toArray()[0]).lastModified = 100;


        AndManifestDirectorySnapshoot oldSnapshoot =
                (AndManifestDirectorySnapshoot) AndManifestDirectorySnapshoot.load(outputFile
                        ,AndManifestDirectorySnapshoot.class);


        DiffResultSet<FileDiffInfo> resultSet = snapshoot.diff(oldSnapshoot);
        System.out.println(resultSet);

        assertEquals(1,resultSet.changedDiffInfos.size());
    }

    @Test
    public void testAdd() throws Throwable {
        AndManifestDirectorySnapshoot snapshoot = new AndManifestDirectorySnapshoot();
        for (File file : testManifestFiles) {
            snapshoot.addFile(file);
        }

        AndManifestDirectorySnapshoot snapshoot2 = new AndManifestDirectorySnapshoot();
        for (File file : testManifestFiles) {
            snapshoot2.addFile(file);
        }

        File file = new File(workDir,"common-group/common3/src/main/AndroidManifest2.xml");
        file.getParentFile().mkdir();
        file.createNewFile();
        snapshoot2.addFile(file);

        DiffResultSet<FileDiffInfo> resultSet = snapshoot.diff(snapshoot2);
        System.out.println(resultSet);

        assertEquals(1,resultSet.changedDiffInfos.size());
    }
}
