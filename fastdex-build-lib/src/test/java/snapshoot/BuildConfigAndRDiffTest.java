package snapshoot;

import fastdex.build.lib.snapshoot.api.DiffResultSet;
import fastdex.build.lib.snapshoot.file.FileSuffixFilter;
import fastdex.build.lib.snapshoot.file.ScanFilter;
import fastdex.build.lib.snapshoot.sourceset.JavaDirectorySnapshoot;
import fastdex.build.lib.snapshoot.sourceset.SourceSetSnapshoot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by tong on 17/3/31.
 */
public class BuildConfigAndRDiffTest extends TestCase {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void test() throws Throwable {
//        SourceSetSnapshoot sourceSetSnapshoot = new SourceSetSnapshoot(new File("/Users/tong/Projects/fastdex/sample/app"),"/Users/tong/Projects/fastdex/sample/app/src/main/java");
//
//        final File rDir = new File("/Users/tong/Projects/fastdex/sample/app/build/generated/source/r/debug");
//        JavaDirectorySnapshoot rSnapshoot = new JavaDirectorySnapshoot(rDir){
//            @Override
//            protected void walkFileTree(File directory, ScanFilter scanFilter) throws IOException {
//                visitFile(new File("/Users/tong/Projects/fastdex/sample/app/build/generated/source/r/debug/com/dx168/fastdex/sample/R.java").toPath(),null,scanFilter);
//            }
//        };
//
//        File buildConfigDir = new File("/Users/tong/Projects/fastdex/sample/app/build/generated/source/buildConfig/debug");
//        JavaDirectorySnapshoot buildConfigSnapshoot = new JavaDirectorySnapshoot(buildConfigDir){
//            @Override
//            protected void walkFileTree(File directory, ScanFilter scanFilter) throws IOException {
//                visitFile(new File("/Users/tong/Projects/fastdex/sample/app/build/generated/source/buildConfig/debug/com/dx168/fastdex/sample/BuildConfig.java").toPath(),null,scanFilter);
//            }
//        };
//        sourceSetSnapshoot.addJavaDirectorySnapshoot(rSnapshoot);
//        sourceSetSnapshoot.addJavaDirectorySnapshoot(buildConfigSnapshoot);
//
//        SourceSetSnapshoot oldSourceSetSnapshoot = (SourceSetSnapshoot) SourceSetSnapshoot.load(new File("/Users/tong/Projects/fastdex/sample/app/build/fastdex/Debug/sourceSets.json"),SourceSetSnapshoot.class);
//        DiffResultSet diffResultSet = sourceSetSnapshoot.diff(oldSourceSetSnapshoot);
//
//        System.out.println(diffResultSet);
//        DiffResultSet diffResultSet2 = sourceSetSnapshoot.diff(oldSourceSetSnapshoot);
    }


    @Test
    public void test2() throws Throwable {
//        JavaDirectorySnapshoot snapshoot = new JavaDirectorySnapshoot(new File("/Users/tong/Projects/fastdex/sample/app/build/intermediates/classes/debug"),new FileSuffixFilter(".class"));
//        JavaDirectorySnapshoot oldSnapshoot = (JavaDirectorySnapshoot) JavaDirectorySnapshoot.load(new File("/Users/tong/Desktop/snapshoot.json"),JavaDirectorySnapshoot.class);
//
//        DiffResultSet diffResultSet = snapshoot.diff(oldSnapshoot);
//        System.out.println(GSON.toJson(diffResultSet.changedDiffInfos));
//
//        snapshoot.serializeTo(new FileOutputStream("/Users/tong/Desktop/snapshoot.json"));
    }
}
