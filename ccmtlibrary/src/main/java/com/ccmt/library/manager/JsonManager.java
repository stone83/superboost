package com.ccmt.library.manager;

import android.util.Log;

import com.ccmt.library.lru.LruMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonManager {

    @SuppressWarnings("WeakerAccess")
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @SuppressWarnings("unused")
    public static final String DATE_FORMAT_NO_SECOND = "yyyy-MM-dd HH:mm";
    private Gson gson;
    @SuppressWarnings("WeakerAccess")
    public SimpleDateFormat simpleDateFormat;
    private GsonBuilder gsonBuilder;
    private String pattern;

    public JsonManager() {
//        init(DATE_FORMAT,null);
    }

    public <T> void init(final String pattern, final Class<T> cla, final String key) {
        if (gsonBuilder == null) {
            gsonBuilder = new GsonBuilder();
        }
        if (!pattern.equals(this.pattern)) {
            this.pattern = pattern;
            simpleDateFormat = new SimpleDateFormat(pattern,
                    Locale.getDefault());
            gsonBuilder.registerTypeHierarchyAdapter(
                    Date.class, new JsonSerializer<Date>() {
                        public JsonElement serialize(Date src, Type typeOfSrc,
                                                     JsonSerializationContext context) {
                            if (!simpleDateFormat.toPattern().equals(pattern)) {
                                simpleDateFormat.applyPattern(pattern);
                            }
                            return new JsonPrimitive(simpleDateFormat
                                    .format(src));
                        }
                    }).registerTypeAdapter(Date.class,
                    new JsonDeserializer<Date>() {
                        public Date deserialize(JsonElement json, Type typeOfT,
                                                JsonDeserializationContext context)
                                throws JsonParseException {
                            if (!simpleDateFormat.toPattern().equals(pattern)) {
                                simpleDateFormat.applyPattern(pattern);
                            }
                            String dateStr = json.getAsString();
                            try {
                                return simpleDateFormat.parse(dateStr);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    });
        }
        if (cla != null) {
            gsonBuilder.registerTypeHierarchyAdapter(cla,
                    new JsonSerializer<T>() {
                        @SuppressWarnings("unchecked")
                        public JsonElement serialize(T src,
                                                     Type typeOfSrc, JsonSerializationContext context) {
                            Log.i("MyLog", "serialize -> " + src);

                            if (src == null) {
                                return null;
                            }

                            if (src instanceof List) {
                                if (((List) src).size() == 0) {
                                    return null;
                                }
                                LruMap.getInstance().put(key, src);
                            } else if (src instanceof Map) {
                                if (((Map) src).size() == 0) {
                                    return null;
                                }
                                LruMap.getInstance().put(key, src, true);
                            } else {
                                boolean isBaseOrStringType = false;
                                if (src instanceof String) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Integer) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Long) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Float) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Double) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Short) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Byte) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Character) {
                                    isBaseOrStringType = true;
                                } else if (src instanceof Boolean) {
                                    isBaseOrStringType = true;
                                }
                                if (!isBaseOrStringType) {
                                    LruMap.getInstance().put(key, src);
                                }
                            }

                            return new JsonPrimitive(src.toString());
                        }
                    }).registerTypeAdapter(cla,
                    new JsonDeserializer<T>() {
                        @SuppressWarnings("unchecked")
                        public T deserialize(JsonElement json,
                                             Type typeOfT, JsonDeserializationContext context)
                                throws JsonParseException {
                            if (json == null) {
                                return null;
                            }

                            String str = json.getAsString();
                            Log.i("MyLog", "deserialize -> " + str);

                            T t = (T) LruMap.getInstance().get(key);
                            if (t instanceof List) {
                                return t;
                            } else if (t instanceof Map) {
                                return t;
                            } else if (t != null) {
                                return t;
                            } else {
                                if (str == null || "".equals(str)) {
                                    return null;
                                }
//                                str = str.substring(1);
//                                str = str.substring(0, str.length() - 1);
//                                String[] strs = str.split(",");
                                if (cla == String.class) {
                                    return (T) str;
                                } else if (cla == Integer.class) {
                                    return (T) Integer.valueOf(str);
                                } else if (cla == Long.class) {
                                    return (T) Long.valueOf(str);
                                } else if (cla == Float.class) {
                                    return (T) Float.valueOf(str);
                                } else if (cla == Double.class) {
                                    return (T) Double.valueOf(str);
                                } else if (cla == Short.class) {
                                    return (T) Short.valueOf(str);
                                } else if (cla == Byte.class) {
                                    return (T) Byte.valueOf(str);
                                } else if (cla == Character.class) {
                                    return (T) Character.valueOf(str.charAt(0));
                                } else if (cla == Boolean.class) {
                                    return (T) Boolean.valueOf(str);
                                }

                                return (T) str;
                            }
                        }
                    });
        }
        gson = gsonBuilder.setDateFormat(DATE_FORMAT).create();
    }

    public <T> String toJson(T ts) {
        return gson.toJson(ts);
    }

    public <T> String toJson(T ts, String dateformat) {
        return gsonBuilder.setDateFormat(dateformat).create().toJson(ts);
    }

    // public static <T> String toJsonDateSerializer(T ts) {
    // return toJson(ts, DATE_FORMAT);
    // }

    public <T> List<T> fromJsonAsList(String jsonStr) {
        return fromJson(jsonStr,
                new com.google.gson.reflect.TypeToken<List<T>>() {
                }.getType());
    }

    @SuppressWarnings("WeakerAccess")
    public <T> List<T> fromJson(String jsonStr, java.lang.reflect.Type type) {
        return gson.fromJson(jsonStr, type);
    }

    @SuppressWarnings("WeakerAccess")
    public <K, V> Map<K, V> fromJsonAsMap(String jsonStr) {
        return gson.fromJson(jsonStr,
                new com.google.gson.reflect.TypeToken<Map<K, V>>() {
                }.getType());
    }

    public <T> T fromJsonAsObject(String jsonStr, Class<T> cla) {
        return gson.fromJson(jsonStr, cla);
    }

    @SuppressWarnings("unused")
    public <T> T fromJsonAsObject(String jsonStr, Class<T> cla, String pattern) {
        return gsonBuilder.setDateFormat(pattern).create()
                .fromJson(jsonStr, cla);
    }

    @SuppressWarnings("unused")
    public <K, V> V optJsonValue(String jsonStr, K key) {
        V rulsObj = null;
        Map<K, V> rulsMap = fromJsonAsMap(jsonStr);
        if (rulsMap != null && rulsMap.size() > 0) {
            rulsObj = rulsMap.get(key);
        }
        return rulsObj;
    }

}