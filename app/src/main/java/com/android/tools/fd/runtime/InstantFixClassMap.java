/*
 *
 *  * Copyright (C) 2017 meili-inc company
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.android.tools.fd.runtime;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;


/**
 * Holds global map.
 * Created by wangzhi on 16/12/22.
 */

public class InstantFixClassMap {
    private static AtomMap sAtomMap = new AtomMap();
    private static ClassLoader sClassLoader = null;


    public static class AtomMap {
        HashMap<Integer, IncrementalChange> classChangeMap;
        HashMap<Integer, String> classIndexMap;
        HashMap<Integer, ReadWriteLock> lockMap = new HashMap<>();

        private AtomMap() {
            classChangeMap = new HashMap<>();
            classIndexMap = new HashMap<>();
            lockMap = new HashMap<>();
        }

        public AtomMap(HashMap<Integer, String> classIndexMap, HashMap<Integer, ReadWriteLock> lockMap) {
            this.classIndexMap = classIndexMap;
            this.lockMap = lockMap;
            classChangeMap = new HashMap<>();
        }
    }

    public static void setAtomMap(AtomMap atomMap) {
        sAtomMap = atomMap;
    }

    public static void setClassLoader(ClassLoader classLoader) {
        sClassLoader = classLoader;
    }

    /**
     * All methods by instrumentation will automatically call this method.
     *
     * @return IncrementalChange object if the method of calling this method has been fixed,
     *         or null if the method of calling this method not fixed.
     */
    public static IncrementalChange get(int classIndex, int mtdIndex) {
        if (sClassLoader == null) {
            return null;
        }
        ReadWriteLock lock = sAtomMap.lockMap.get(classIndex);
        if (lock == null) {
            return null;
        }
        String classNameInPackage = sAtomMap.classIndexMap.get(classIndex);
        //classNameInPackage!=null 说明该类被hotfix了
        if (classNameInPackage != null) {
            lock.readLock().lock();
            IncrementalChange incrementalChange = sAtomMap.classChangeMap.get(classIndex);
            lock.readLock().unlock();
            //该类被hotfix后第一次调用它的方法。
            if (incrementalChange == null) {
                try {
                    Class<?> aClass = sClassLoader.loadClass(classNameInPackage + "$override");
                    incrementalChange = (IncrementalChange) aClass.newInstance();
                    lock.writeLock().lock();
                    sAtomMap.classChangeMap.put(classIndex, incrementalChange);
                    lock.writeLock().unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (incrementalChange != null && incrementalChange.isSupport(mtdIndex)) {
                return incrementalChange;
            }
        }
        return null;
    }


}
