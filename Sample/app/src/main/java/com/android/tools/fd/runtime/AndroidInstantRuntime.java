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

import android.util.LruCache;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

/**
 * Generic Instant Run services. must not depend on Android APIs.
 * 
 * TODO: transform this static methods into interface/implementation.
 */
@SuppressWarnings("unused")
public class AndroidInstantRuntime {

    @Nullable
    public static Object getStaticPrivateField(Class targetClass, String fieldName) {
        return getPrivateField(null /* targetObject */, targetClass, fieldName);
    }

    public static void setStaticPrivateField(
            @NonNull Object value, @NonNull Class targetClass, @NonNull String fieldName) {
        setPrivateField(null /* targetObject */, value, targetClass, fieldName);
    }

    public static void setPrivateField(
            @Nullable Object targetObject,
            @Nullable Object value,
            @NonNull Class targetClass,
            @NonNull String fieldName) {

        try {
            Field declaredField = getField(targetClass, fieldName);
            declaredField.set(targetObject, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Object getPrivateField(
            @Nullable Object targetObject,
            @NonNull Class targetClass,
            @NonNull String fieldName) {

        try {
            Field declaredField = getField(targetClass, fieldName);
            return declaredField.get(targetObject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @NonNull
    private static Field getField(Class target, String name) {
        Field declareField = getFieldByName(target, name);
        if (declareField == null) {
            throw new RuntimeException(new NoSuchElementException(name));
        }
        declareField.setAccessible(true);
        return declareField;
    }

    public static Object invokeProtectedMethod(Object receiver,
                                               Object[] params,
                                               Class[] parameterTypes,
                                               String methodName) throws Throwable {

        try {
            Method toDispatchTo = getMethodByName(receiver.getClass(), methodName, parameterTypes);
            if (toDispatchTo == null) {
                throw new RuntimeException(new NoSuchMethodException(methodName));
            }
            toDispatchTo.setAccessible(true);
            return toDispatchTo.invoke(receiver, params);
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeProtectedStaticMethod(
            Object[] params,
            Class[] parameterTypes,
            String methodName,
            Class receiverClass) throws Throwable {

        try {
            Method toDispatchTo = getMethodByName(receiverClass, methodName, parameterTypes);
            if (toDispatchTo == null) {
                throw new RuntimeException(new NoSuchMethodException(
                        methodName + " in class " + receiverClass.getName()));
            }
            toDispatchTo.setAccessible(true);
            return toDispatchTo.invoke(null /* target */, params);
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static LruCache<String, Object> ClassCache = new LruCache<String, Object>(128 * 4);

    public static <T> T newForClass(Object[] params, Class[] paramTypes, Class<T> targetClass)
            throws Throwable {

        StringBuilder sb = new StringBuilder();
        sb.append(targetClass.getName()).append("(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(paramTypes[i].getName());

        }
        sb.append(")");
        String key = sb.toString();

        Constructor declaredConstructor = (Constructor) ClassCache.get(key);
        if (declaredConstructor == null) {
            try {
                declaredConstructor = targetClass.getDeclaredConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            declaredConstructor.setAccessible(true);
            ClassCache.put(key, declaredConstructor);
        }

        try {
            return targetClass.cast(declaredConstructor.newInstance(params));
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static LruCache<String, Field> FieldCache = new LruCache<String, Field>(256 * 4);

    private static Field getFieldByName(Class<?> aClass, String name) {

        String key = aClass.getName() + "." + name;
        Field field = FieldCache.get(key);
        if (field != null) {
            return field;
        }

        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                field = currentClass.getDeclaredField(name);
                if (field != null) {
                    FieldCache.put(key, field);
                }
                return field;
            } catch (NoSuchFieldException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    static LruCache<String, Method> methodCache = new LruCache<String, Method>(256 * 4);

    private static Method getMethodByName(Class<?> aClass, String name, Class[] paramTypes) {
        if (aClass == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(aClass.getName()).append(".").append(name).append("(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(paramTypes[i].getName());

        }
        sb.append(")");
        String key = sb.toString();
        Method method = methodCache.get(key);
        if (method != null) {
            return method;
        }

        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                method = currentClass.getDeclaredMethod(name, paramTypes);
                if (method != null) {
                    methodCache.put(key, method);
                }
                return method;
            } catch (NoSuchMethodException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }
}
