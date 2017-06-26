package fastdex.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/**
 * Created by tong on 17/3/30.
 */
public class SerializeUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T load(InputStream inputStream,Class<T> type) throws IOException {
        String json = new String(FileUtils.readStream(inputStream));
        return GSON.fromJson(json,type);
    }

    public static void serializeTo(OutputStream outputStream,Object obj) throws IOException {
        String json = GSON.toJson(obj);
        try {
            outputStream.write(json.getBytes());
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static void serializeTo(File file,Object obj) throws IOException {
        String json = GSON.toJson(obj);
        FileOutputStream outputStream = null;
        FileUtils.ensumeDir(file.getParentFile());
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
