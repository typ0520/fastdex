package com.dx168.fastdex.sample.common2;

import com.dx168.fastdex.sample.javalib2.JavaLib2;

/**
 * Created by tong on 17/4/13.
 */
public class Common2Utils {
    public static String str = Common2Utils.class.getSimpleName() + ".str";

    public Common2Utils() {
        JavaLib2 javaLib = new JavaLib2();

        System.out.println("==common2: " + javaLib.str);
    }
}
