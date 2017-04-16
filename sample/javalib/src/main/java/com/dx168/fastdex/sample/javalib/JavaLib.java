package com.dx168.fastdex.sample.javalib;

import com.dx168.fastdex.sample.javalib2.JavaLib2;

/**
 * Created by tong on 17/4/14.
 */
public class JavaLib {
    public static String str = JavaLib.class.getSimpleName() + ".str";

    public JavaLib() {
        JavaLib2 javaLib2 = new JavaLib2();
        System.out.println(javaLib2.str);
    }
}
