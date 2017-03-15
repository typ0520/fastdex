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

/**
 * Exception thrown by the instant reload runtime when something preventing the byte code enhanced
 * code to function properly. This is a generic error that something went wrong in the instant
 * reload runtime and it should be considered a implementation bug.
 *
 * For instance, this can be generated when trying to invoke a super method that the instant runtime
 * cannot find in the generated $super method.
 */
public class InstantReloadException extends Exception {

    public InstantReloadException(String s) {
        super(s);
    }
}
