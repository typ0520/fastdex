package com.dx168.fastdex.sample.javalib2;

import com.dx168.fastdex.sample.javalib3.JavaLib3;

/**
 * Created by tong on 17/4/14.
 */
public class JavaLib2 {
    public static String str = JavaLib2.class.getSimpleName() + ".str";

    public JavaLib2() {
        JavaLib3 javaLib3 = new JavaLib3();
        System.out.println(javaLib3.str);
    }
}
