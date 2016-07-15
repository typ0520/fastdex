package com.example.fertilizercrm.common.httpclient.expand;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class BeanUtils {
	/**
	 * 得到一个对象的字段的键值map(value不为空且为public的字段)
	 * @param obj
	 * @return
	 */
    public static Map<String, Object> optPublicFieldKeyValueMap(Object obj) {
    	Map<String,Object> map = new HashMap<String, Object>();
		if (obj != null) {
			Field[] fields = obj.getClass().getFields();
			for (Field f : fields) {
				try {
					boolean isStatic = Modifier.isStatic(f.getModifiers());
					if (!isStatic) {
						Object value = f.get(obj);
						if (value != null)
							map.put(f.getName(), value);
					}
				} catch (Exception e) {
					
				}
			}
		}
		return map;
	}
}
