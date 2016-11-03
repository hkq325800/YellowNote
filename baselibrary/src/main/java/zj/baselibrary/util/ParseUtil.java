package zj.baselibrary.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParseUtil {

	private ParseUtil() {
	}

	public static <T> ArrayList<T> parseList(ArrayList<T> listData, JSONArray list, Class<T> clazz) {
		if (listData == null)
			listData = new ArrayList<T>();

		if (list == null || list.length() <= 0) {
			return listData;
		}
		int len = list.length();
		for (int i = 0; i < len; i++) {
			listData.add(parse(list.optJSONObject(i), clazz));
		}
		return listData;
	}
	
	public static  ArrayList<Object> parseListObject(ArrayList<Object> listData, JSONArray list, Class<?> clazz) {
		if (listData == null)
			listData = new ArrayList<Object>();

		if (list == null || list.length() <= 0) {
			return listData;
		}
		int len = list.length();
		for (int i = 0; i < len; i++) {
			listData.add(parse(list.optJSONObject(i), clazz));
		}
		return listData;
	}
	
	/**
	 * 
	 * @param obj
	 * @param clazz
	 * @return
	 */
	public static <T> T parse(JSONObject obj,  Class<T> clazz){
		Constructor<?> con;
		T model = null;
		try {
			con = clazz.getConstructor(JSONObject.class);
			model = clazz.cast(con.newInstance(obj));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return model;
	}
	
}
