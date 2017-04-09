package com.dx168.fastdex.build.snapshoot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/**
 * Created by tong on 17/3/30.
 */
public class SerializeUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T load(InputStream inputStream,Class<T> type) throws IOException {
        String json = new String(readStream(inputStream));
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


    public static byte[] readStream(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int bufferSize = 16384;
        try {
            final BufferedInputStream bIn = new BufferedInputStream(inputStream);
            int length;
            byte[] buffer = new byte[bufferSize];
            byte[] bufferCopy;
            while ((length = bIn.read(buffer, 0, bufferSize)) != -1) {
                bufferCopy = new byte[length];
                System.arraycopy(buffer, 0, bufferCopy, 0, length);
                output.write(bufferCopy);
            }
            bIn.close();
        } finally {
            output.close();
        }
        return output.toByteArray();
    }
}
