package fastdex.build.util;

import java.lang.reflect.Field;

/**
 * Created by tong on 17/11/3.
 */
class ReflectUtils {
    static Field getFieldByName(Class<?> aClass, String name) {
        Class<?> currentClass = aClass
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(name)
            } catch (NoSuchFieldException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass()
        }
        return null
    }
}
