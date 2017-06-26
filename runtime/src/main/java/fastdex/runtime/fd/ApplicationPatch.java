/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fastdex.runtime.fd;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import fastdex.runtime.fastdex.Fastdex;
import android.util.Log;

// This class is used in both the Android runtime and in the IDE.
// Technically we only need the write protocol on the IDE side and the
// read protocol on the Android app size, but keeping it all together and
// in sync right now.
public class ApplicationPatch {
    public final String path;
    public final byte[] data;

    public ApplicationPatch(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApplicationPatch{" +
                "path='" + path + '\'' +
                ", data.length='" + data.length + '\'' +
                '}';
    }

    // Only needed on the Android side
    public static List<ApplicationPatch> read(DataInputStream input) throws IOException {
        int changeCount = input.readInt();

        Log.d(Fastdex.LOG_TAG, "Receiving " + changeCount + " changes");
        List<ApplicationPatch> changes = new ArrayList<ApplicationPatch>(changeCount);
        for (int i = 0; i < changeCount; i++) {
            String path = input.readUTF();
            int size = input.readInt();
            byte[] bytes = new byte[size];
            input.readFully(bytes);
            changes.add(new ApplicationPatch(path, bytes));

            Log.d(Fastdex.LOG_TAG, "Receiving path: " + path);
        }

        return changes;
    }


    public String getPath() {
        return path;
    }


    public byte[] getBytes() {
        return data;
    }
}
