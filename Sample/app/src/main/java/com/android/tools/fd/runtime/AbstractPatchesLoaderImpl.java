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

package com.android.tools.fd.runtime;


import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractPatchesLoaderImpl implements PatchesLoader {
    private static final String TAG = "AbstractPatchesLoaderIm";

    public abstract String[] getPatchedClasses();

    public abstract int[] getPatchedClassIndexes();

    @Override
    public boolean load() {
        try {
            InstantFixClassMap.setClassLoader(getClass().getClassLoader());
            HashMap<Integer, ReadWriteLock> lockMap = new HashMap<>();
            HashMap<Integer, String> classIndexMap = new HashMap<>();
            String[] patchedClasses = getPatchedClasses();
            int[] patchedClassIndexes = getPatchedClassIndexes();
            if (patchedClasses.length != patchedClassIndexes.length) {
                throw new IllegalArgumentException("patchedClasses's len is " + patchedClasses.length + ", but patchedClassIndexs's len is " + patchedClassIndexes.length);
            }
            for (int i = 0; i < patchedClasses.length; i++) {
                String className = patchedClasses[i];
                int classIndex = patchedClassIndexes[i];
                lockMap.put(classIndex, new ReentrantReadWriteLock());
                classIndexMap.put(classIndex, className);
                Log.i(TAG, String.format("patched %s", className));
            }
            InstantFixClassMap.setAtomMap(new InstantFixClassMap.AtomMap(classIndexMap, lockMap));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
