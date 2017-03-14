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

import java.io.File;

import dalvik.system.BaseDexClassLoader;

/**
 * Created by xieguo on 1/10/17.
 */

public class InstantRunClassLoader extends BaseDexClassLoader {

    public InstantRunClassLoader(String dexPath, File optimizedDirectory, ClassLoader parent) {
        super(dexPath, optimizedDirectory, null, parent);
    }

    /**
     * find a native code library.
     *
     * @param libraryName the name of the library.
     * @return the String of a path name to the library or <code>null</code>.
     * @category ClassLoader
     * @see ClassLoader#findLibrary(String)
     */
    public String  findLibrary(final String libraryName) {

        try {
            return (String)AndroidInstantRuntime.invokeProtectedMethod(this.getParent(), new Object[]{libraryName},
                    new Class[]{String.class}, "findLibrary");
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

}
