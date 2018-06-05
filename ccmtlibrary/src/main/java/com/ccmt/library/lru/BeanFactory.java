package com.ccmt.library.lru;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * 创建实例的工厂
 * 
 * @author Administrator
 */
public class BeanFactory {

	private static Properties properties;
	private static URL url;

	static {
		properties = new Properties();
		url = BeanFactory.class.getClassLoader().getResource("bean.properties");
	}

	/**
	 * 获取实例
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getImpl(Class<T> clazz) {
		load();
		String property = properties.getProperty(clazz.getName());
		// System.out.println("clazz.getSimpleName() -> " +
		// clazz.getSimpleName());
		// System.out.println("property -> " + property);
		try {
			return (T) Class.forName(property).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void load() {
		InputStream is = null;
		try {
			is = url.openStream();
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}