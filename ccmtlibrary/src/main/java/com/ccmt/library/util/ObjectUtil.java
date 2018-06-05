package com.ccmt.library.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.manager.JsonManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjectUtil {

    /**
     * 从sp中通过指定的key获取对象,如果是获取List或Map对象,指定cla参数传List.class或Map.class就可以,
     * 也可以调用obtainList()方法或obtainMap()方法.
     *
     * @param context
     * @param cla
     * @param key
     * @param <T>
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static <T> T obtainObject(Context context, Class<T> cla, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String str = sharedPreferences.getString(key, null);
        if (str == null) {
            return null;
        }
        return ObjectUtil.obtainJsonManager(cla, key).fromJsonAsObject(str, cla);
    }

    @SuppressWarnings("unused")
    public static <T> List<T> obtainList(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String str = sharedPreferences.getString(key, null);
        if (str == null) {
            return null;
        }
        return ObjectUtil.obtainJsonManager(List.class, key).fromJsonAsList(str);
    }

    @SuppressWarnings("unused")
    public static <K, V> Map<K, V> obtainMap(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String str = sharedPreferences.getString(key, null);
        if (str == null) {
            return null;
        }
        return ObjectUtil.obtainJsonManager(Map.class, key).fromJsonAsMap(str);
    }

    /**
     * 把对象直接保存到sp,不会保存到内存中.
     *
     * @param context
     * @param cla
     * @param key
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings({"rawtypes", "JavaDoc"})
    public static <T> boolean saveObject(Context context, Class<T> cla, String key, T obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Collection) {
            if (((Collection) obj).size() == 0) {
                return false;
            }
        } else if (obj instanceof Map) {
            if (((Map) obj).size() == 0) {
                return false;
            }
        }
        String json = ObjectUtil.obtainJsonManager(cla, key).toJson(obj);
        if (json == null || "".equals(json)) {
            return false;
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(key, json).commit();
        return true;
    }

    /**
     * 从sp中通过指定的key删除对象
     *
     * @param context
     * @param key
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static boolean removeObject(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (!sharedPreferences.contains(key)) {
            return false;
        }
        sharedPreferences.edit().remove(key).commit();
        LruMap.getInstance().remove(key);
        return true;
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> JsonManager obtainJsonManager(Class<T> targetClass, String key) {
        Class<JsonManager> cla = JsonManager.class;
        JsonManager jsonManager = LruMap.getInstance()
                .createOrGetElement(cla.getName(), cla, null);
        jsonManager.init(JsonManager.DATE_FORMAT, targetClass, key);
        return jsonManager;
    }

    public static JsonManager obtainJsonManager() {
        return obtainJsonManager(null, null);
    }

}