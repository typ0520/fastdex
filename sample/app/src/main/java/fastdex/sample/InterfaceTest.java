package fastdex.sample;

/**
 * Created by tong on 17/11/14.
 */
public interface InterfaceTest {
    default String test() {
        System.out.println("默认方法");
        return "默认方法";
    }

    static String staticMethod() {
        System.out.println("静态方法");
        return "静态方法";
    }
}
