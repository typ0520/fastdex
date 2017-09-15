package fastdex.build.util

import fastdex.build.lib.snapshoot.file.FileSuffixFilter

/**
 * Created by tong on 17/9/14.
 */
public class Exclude$SourceSuffixFilter extends FileSuffixFilter {
    Exclude$SourceSuffixFilter(String... suffixs) {
        super(".java",".kt")
    }

    @Override
    boolean preVisitFile(File file) {
        boolean result = super.preVisitFile(file)
        if (result) {
            if (file.getName().contains("\$")) {
                result = false
            }
        }
        return result
    }
}
