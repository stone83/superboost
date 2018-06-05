package com.ccmt.library.lru;

import com.ccmt.library.global.Global;
import com.ccmt.library.lru.SoftMap.ICreateObjectAble;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LruMap {

    /**
     * 可以开辟的总内存除以该倍数就是lru缓存对象可以占用的最大内存
     */
    private static final int MULTIPLE = 3;

    private static LruMap instance;

    /**
     * lru缓存
     */
    private LinkedHashMap<String, Object> linkedHashMap;

    /**
     * 软引用
     */
    private SoftMap<String, Object> softMap;

    private Runtime runtime;

    /**
     * 软引用做文件序列化时保存文件的目录
     */
    @SuppressWarnings("FieldCanBeLocal")
    private String serializableFileDir;

    /**
     * 如果不手动删除指定的文件,那么在当前类的对象被创建时,该目录下的序列化文件不会被删除.
     */
    private String serializableFileDirNotDelete;

    private LruMap() {
        // this(System.getProperty("user.dir") + "/Ser");
        this(Global.serializableFileDir);
    }

    private LruMap(String serializableFileDir) {
        this.linkedHashMap = new LinkedHashMap<>(32, 0.75F, true);
        this.runtime = Runtime.getRuntime();
        this.serializableFileDir = serializableFileDir;
        // this.serializableFileDirNotDelete = System.getProperty("user.dir")
        // + "/SerNotDelete";
        this.serializableFileDirNotDelete = Global.serializableFileDirNotDelete;
        FileUtil.deleteDir(this.serializableFileDir);
    }

    public static LruMap getInstance() {
        return getInstance(null);
    }

    private static LruMap getInstance(String serializableFileDir) {
        if (instance == null) {
            if (serializableFileDir != null) {
                instance = new LruMap(serializableFileDir);
            } else {
                instance = new LruMap();
            }
        }
        return instance;
    }

    public boolean put(String key, Object value) {
        return put(key, value, false);
    }

    public boolean put(String key, Object value, boolean serialize) {
        Object obj = linkedHashMap.get(key);
        if (obj == null) {
            if (softMap == null) {
                // softMap = SoftMap.getInstance(serializableFileDir);
                softMap = SoftMap.getInstance();
            }
            if (softMap.obtainElement(key) == null) {
                if (!isMemreyEnough()) {
                    // 内存不够
                    if (!linkedHashMap.isEmpty()) {
                        Iterator<Entry<String, Object>> ite = linkedHashMap
                                .entrySet().iterator();
                        Entry<String, Object> next = ite.next();

                        // 把lru缓存中最不常用的元素放到软引用中
                        softMap.put(next.getKey(), next.getValue());

                        // 把lru缓存中最不常用的元素删除
                        ite.remove();

                        // 把lru缓存中被删除的元素对应的序列化文件删除
                        deleteSerializableFile(key,
                                serializableFileDirNotDelete);
                    }
                }
                linkedHashMap.put(key, value);
                if (serialize) {
                    softMap.serializNotDelete(key, value,
                            serializableFileDirNotDelete);
                }
                return true;
            }
        } else {
            if (!obj.equals(value)) {
                linkedHashMap.put(key, value);
                if (serialize) {
                    softMap.serializNotDelete(key, value,
                            serializableFileDirNotDelete);
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public Object remove(String key) {
        return remove(key, true);
    }

    public Object remove(String key, boolean deleteSerializeFile) {
        if (deleteSerializeFile) {
            deleteSerializableFile(key, serializableFileDirNotDelete);
        }
        if (linkedHashMap.containsKey(key)) {
            return linkedHashMap.remove(key);
        } else {
            if (softMap == null) {
                // softMap = SoftMap.getInstance(serializableFileDir);
                softMap = SoftMap.getInstance();
            }
            return softMap.remove(key, deleteSerializeFile);
        }
    }

    public Object get(String key) {
        if (linkedHashMap.containsKey(key)) {
            return linkedHashMap.get(key);
        } else {
            if (softMap == null) {
                // softMap = SoftMap.getInstance(serializableFileDir);
                softMap = SoftMap.getInstance();
            }
            Object value = softMap.obtainElement(key);
            if (value != null) {
                return value;
            }
            File f = new File(serializableFileDirNotDelete + "/"
                    + key + SoftMap.serializableFileSuffix);
            if (f.exists()) {
                return softMap.deserializNotDelete(f);
            }
            return null;
        }
    }

    @SuppressWarnings("unused")
    public boolean isSerializable(Object value) {
        if (softMap == null) {
            // softMap = SoftMap.getInstance(serializableFileDir);
            softMap = SoftMap.getInstance();
        }
        return softMap.serializable(value);
    }

    @SuppressWarnings("WeakerAccess")
    public void deleteSerializableFile(String key, String serializablePath) {
        if (softMap == null) {
            // softMap = SoftMap.getInstance(serializableFileDir);
            softMap = SoftMap.getInstance();
        }
        softMap.deleteSerializableFile(key, serializablePath);
    }

    @SuppressWarnings("unused")
    public boolean deleteAllSerializableFile(String serializablePath) {
        if (softMap == null) {
            // softMap = SoftMap.getInstance(serializableFileDir);
            softMap = SoftMap.getInstance();
        }
        return softMap.deleteAllSerializableFile(serializablePath);
    }

    /**
     * 如果linkedHashMap中存在指定key的元素,就取出.如果不存在,则在软引用对象中通过指定的key寻找,
     * <p>
     * 找到了就取出.如果没找到,并且cla有默认无参构造方法,通过反射方式创建一个新的元素.
     * <p>
     * 如果不存在,并且cla没有默认无参构造方法,
     * <p>
     * 通过实现createObjectAble接口方式创建一个新的元素,再放进linkedHashMap中,并返回新创建的元素.
     *
     * @param key              指定key
     * @param cla              指定Class对象
     * @param createObjectAble 指定创建没有默认无参构造方法的对象的接口.
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public <T> T createOrGetElement(String key, Class<T> cla,
                                    ICreateObjectAble<T> createObjectAble) {
        return createOrGetElement(key, cla, createObjectAble, false);
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public <T> T createOrGetElement(String key, Class<T> cla,
                                    ICreateObjectAble<T> createObjectAble, boolean serialize) {
        T t = (T) get(key);
        if (t != null) {
            // 如果temp集合中存在指定key的talue,就直接返回该talue.
            return t;
        }
        try {
            if (cla.isInterface()) {
                // 如果传进来的是个接口,就根据配置文件,实例化一个该接口的实现类对象.
                t = BeanFactory.getImpl(cla);
            } else {
                // 如果传进来的是一个类
                // 如果传进来的类在之前没有对应的序列化文件存在,那么就直接实例化该类的对象,
                // 在之后调用put()方法后保存到temp集合中,
                // 如果该对象自己和父类都没有非静态属性,那么在后续调用put()方法后就不会序列化,
                // 否则将序列化到文件中,以后再取该类的对象,
                // 就不需要再实例化了.
                t = cla.newInstance();
            }
            if (t != null) {
                if (serialize) {
                    put(key, t, true);
                } else {
                    put(key, t);
                }
            }
            return t;
        } catch (InstantiationException e) {
            // e.printStackTrace();
            if (createObjectAble != null) {
                t = createObjectAble.createObject();
                if (t != null) {
                    if (serialize) {
                        put(key, t, true);
                    } else {
                        put(key, t);
                    }
                }
                return t;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            if (createObjectAble != null) {
                t = createObjectAble.createObject();
                if (t != null) {
                    if (serialize) {
                        put(key, t, true);
                    } else {
                        put(key, t);
                    }
                }
                return t;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return linkedHashMap.toString();
    }

    /**
     * 判断可开辟的内存是否足够
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private boolean isMemreyEnough() {
        return runtime.totalMemory() - runtime.freeMemory() < runtime
                .maxMemory() / MULTIPLE;
    }

    @SuppressWarnings("unused")
    public void serializ(String key, Object value) {
        SoftMap.getInstance().serializ(key, value, true);
    }

    @SuppressWarnings("unused")
    public Object deserializ(String key) {
        return SoftMap.getInstance().deserializ(key);
    }

}