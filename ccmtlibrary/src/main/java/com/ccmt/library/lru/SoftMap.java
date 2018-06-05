package com.ccmt.library.lru;

import com.ccmt.library.global.Global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * 软引用的map,使用说明:
 * <p>
 * 见随SoftMap.java文件在同一目录下的说明.txt文件的的内容.
 * <p>
 * 该类其实就相当于一个Map,专门用来各种数据类型的对象,如果内存不够虚拟机会自己去删除软引用中的对象,
 * <p>
 * 不需要程序员去操心内存溢出的问题了,而且无论何时,
 * <p>
 * 取出来的对象的所有非静态属性值都跟你保存进软引用时的非静态属性值完全一样.
 * <p>
 * 真正意义上的用户只需要new一次对象,以后再不需要new该对象.
 *
 * @param <K>
 * @param <V>
 * @author Administrator
 */
public class SoftMap<K, V> extends HashMap<K, V> {

    public static final long serialVersionUID = 8154715303448341206L;
    @SuppressWarnings("rawtypes")
    private static SoftMap instance;

    /**
     * 序列化文件的后缀名
     */
    public static String serializableFileSuffix = ".ser";

    private HashMap<K, SoftValue<K, V>> temp;

    /**
     * 将传递进来的BaseView的子类对象转换成软引用的对象
     */
    private ReferenceQueue<V> queue;

    /**
     * 产生的系列化文件的目录名,默认在程序的当前根目录中创建一个名为Ser的目录.
     */
    private String serializableFileDir;

    /**
     * 产生的序列化文件的文件名
     */
    private String serializableFilePath;

    /**
     * 序列化文件要保存的目录默认为当前应用根目录下的Ser目录
     */
    private SoftMap() {
        // this(System.getProperty("user.dir") + "/Ser");
        this(Global.serializableFileDir);
    }

    /**
     * 序列化文件要保存的目录为指定路径serializableFileDir所在目录
     *
     * @param serializableFileDir 指定序列化文件要保存的目录
     */
    private SoftMap(String serializableFileDir) {
        temp = new HashMap<K, SoftValue<K, V>>();
        queue = new ReferenceQueue<V>();
        this.serializableFileDir = serializableFileDir;

        // FileUtil.deleteDir(this.serializableFileDir);
    }

    public String getSerializableFileDir() {
        return serializableFileDir;
    }

    public void setSerializableFileDir(String serializableFileDir) {
        this.serializableFileDir = serializableFileDir;
    }

    public static String getSerializableFileSuffix() {
        return SoftMap.serializableFileSuffix;
    }

    public static void setSerializableFileSuffix(String serializableFileSuffix) {
        SoftMap.serializableFileSuffix = serializableFileSuffix;
    }

    public String getSerializableFilePath() {
        return serializableFilePath;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> SoftMap<K, V> getInstance(String serializableFileDir) {
        if (instance == null) {
            if (serializableFileDir != null) {
                instance = new SoftMap<K, V>(serializableFileDir);
            } else {
                instance = new SoftMap<K, V>();
            }
        }
        return instance;
    }

    public static <K, V> SoftMap<K, V> getInstance() {
        return getInstance(null);
    }

    /**
     * 将指定key的对象保存进软引用中,并且根据指定的对象是否有非静态属性来决定
     * <p>
     * 是否将对象序列化到文件中.只有在对象有自己的非静态属性,或者有从父类继承而来的非静态属性,
     * <p>
     * 或者有自己的或者从父类继承而来的public权限的setXxx()方法,满足前面3个条件其中之一的前提下,
     * <p>
     * 也就是说调用serializable()方法返回的值为true时,才会将对象序列化进文件中,否则将不序列化.
     * <p>
     * 此方法将一直返回null,不建议使用此方法,因为调用此方法后,只要指定key的对象满足以上条件,
     * <p>
     * 将默认将对象序列化到文件中,所以建议使用putElement(K key, V value, boolean serializable)方法,
     * <p>
     * 可以让用户自主选择是否将对象序列化到文件中.
     *
     * @see putElement(K key, V value, boolean serializable)
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public V put(K key, V value) {
        clearSoftReference();
        temp.put(key, new SoftValue<K, V>(key, value, queue));

        // 标识对象是否可以序列化,为true时表示可以序列化.
        boolean serializable = false;
        serializable = serializable(value);

        // 只有在对象有自己的非静态属性,或者有从父类继承而来的非静态属性,
        // 或者有public权限的setXxx()方法,满足前面3个条件其中之一的前提下,
        // 并且调用serializable()方法返回的值为true时,才会将对象序列化进文件中,否则将不序列化.
        serializ(key, value, serializable);

        return null;
    }

    /**
     * 将指定key的对象保存进软引用中,并且根据指定的对象是否有非静态属性来决定
     * <p>
     * 是否将对象序列化到文件中.只有在对象有自己的非静态属性,或者有从父类继承而来的非静态属性,
     * <p>
     * 或者有自己的或者从父类继承而来的public权限的setXxx()方法,满足前面3个条件其中之一的前提下,
     * <p>
     * 也就是说调用serializable()方法返回的值为true时,并且指定参数serializable的值也为true时,
     * <p>
     * 才会将对象序列化进文件中,否则将不序列化.
     * <p>
     * 当用户在调用此方法时,不管指定key的对象是否已经被序列化到文件中,
     * <p>
     * 此方法调用后,只要serializable()方法的返回值为true,
     * <p>
     * 并且指定serializable参数的值也为true,都会将指定key的新对象value重新序列化到文件中.
     * <p>
     * 功能与put(K key, V value)方法相同,
     * <p>
     * 所以,当用户如果想将指定key的新对象的非静态属性值做了修改
     * <p>
     * 再保存到软引用中与序列化文件中以备后续使用,
     * <p>
     * 此方法和put(K key, V value)方法都可以,但是如果用户只想将指定key的新对象的非静态属性值做了修改,
     * <p>
     * 再保存到软引用中,而不想序列化后保存到文件中,那么请使用此方法.
     *
     * @param key
     * @param value
     * @param serializable 标识对象是否可以序列化,为true时表示可以序列化.
     * @see put(K key, V value)
     */
    @SuppressWarnings({"JavaDoc", "JavadocReference"})
    public void putElement(K key, V value, boolean serializable) {
        clearSoftReference();
        temp.put(key, new SoftValue<K, V>(key, value, queue));

        // 只有在对象有自己的非静态属性,或者有从父类继承而来的非静态属性,
        // 或者有public权限的setXxx()方法,满足前面3个条件其中之一的前提下,
        // 并且指定参数serializable的值与调用serializable()方法返回的值都为true时,
        // 才会将对象序列化进文件中,否则将不序列化.
        serializ(key, value, serializable && serializable(value));
    }

    /**
     * 序列化操作
     *
     * @param key
     * @param value
     * @param serializable
     */
    public void serializ(K key, V value, boolean serializable) {
        ObjectOutputStream oos = null;
        try {
            if (serializable) {
                serializableFilePath = serializableFileDir + "/"
                        + key.toString() + serializableFileSuffix;
                File f = new File(serializableFilePath);
                File parentFile = f.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(value);
                oos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public V deserializ(K key) {
        ObjectInputStream ois = null;
        try {
            String serializableFilePath = serializableFileDir + "/"
                    + key.toString() + serializableFileSuffix;
            File f = new File(serializableFilePath);
            if (f.exists()) {
                ois = new ObjectInputStream(new FileInputStream(f));
                return (V) ois.readObject();
            }
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void serializNotDelete(K key, V value, String serializePath) {
        if (!serializable(value)) {
            return;
        }
        ObjectOutputStream oos = null;
        try {
            if (serializePath != null) {
                String serializableFilePath = serializePath + "/"
                        + key.toString() + serializableFileSuffix;
                File f = new File(serializableFilePath);
                File parentFile = f.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(value);
                oos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public V deserializNotDelete(File f) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(f));
            return (V) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 判断对象是否可以序列化,返回true时表示可以序列化.
     *
     * @param value 指定被校验的具体对象
     * @return 返回true时表示可以序列化, 否则表示不可以被序列化.
     */
    public boolean serializable(Object value) {
        if (!(value instanceof Serializable)) {
            return false;
        }
        boolean serializable = true;
        Class<? extends Object> cla = value.getClass();
        Field[] fs1 = cla.getDeclaredFields();
        Field[] fs2 = cla.getFields();
        // Method[] m = cla.getMethods();

        if (fs1.length != 0 || fs2.length != 0) {
            // 如果传进来的对象有自己的非静态属性和没有transient修饰符,而且有从父类继承而来的非静态属性和没有transient修饰符,
            // 同时如果有1个属性没有实现Serializable接口,就将serializable设置为false.
            boolean flag = false;
            for (int i = 0; i < fs1.length; i++) {
                if (!Modifier.isStatic(fs1[i].getModifiers())
                        && !Modifier.isTransient(fs1[i].getModifiers())) {
                    fs1[i].setAccessible(true);
                    try {
                        Object o = fs1[i].get(value);
                        if (o != null) {
                            if (!(o instanceof Serializable)) {
                                serializable = false;
                                flag = true;
                                break;
                            }
                            // serializable = serializable(o);
                            // if (!serializable) {
                            // flag = true;
                            // }
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!flag) {
                for (int i = 0; i < fs2.length; i++) {
                    if (!Modifier.isStatic(fs2[i].getModifiers())
                            && !Modifier.isTransient(fs2[i].getModifiers())) {
                        try {
                            Object o = fs2[i].get(value);
                            if (o != null) {
                                if (!(o instanceof Serializable)) {
                                    serializable = false;
                                    flag = true;
                                    break;
                                }
                                // serializable = serializable(o);
                                // if (!serializable) {
                                // flag = true;
                                // }
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        // else if (m.length != 0) {
        // // 如果传进来的对象有自己的setXxx()方法
        // // 或者有从父类继承而来的public权限的setXxx()方法
        // // 就将serializable设置为true.
        // String name = null;
        // for (int i = 0; i < m.length; i++) {
        // name = m[i].getName();
        // if (name.startsWith("set")
        // && !Modifier.isStatic(m[i].getModifiers())) {
        // serializable = true;
        // break;
        // }
        // }
        // }
        return serializable;
    }

    @Override
    public String toString() {
        return temp.toString();
    }

    /**
     * 如果temp集合中存在指定key的value则返回true,否则返回false.
     */
    @Override
    public boolean containsKey(Object key) {
        clearSoftReference();
        return temp.containsKey(key);
    }

    /**
     * 返回temp集合中指定key的value,如果不存在则返回null.
     */
    @Override
    public V get(Object key) {
        clearSoftReference();
        SoftValue<K, V> softReference = temp.get(key);
        if (softReference == null) {
            return null;
        }
        return softReference.get();
    }

    /**
     * 返回temp集合的大小
     */
    @Override
    public int size() {
        clearSoftReference();
        return temp.size();
    }

    /**
     * 删除temp集合中指定key的value,必然会删除成功,一直返回null.
     */
    @Override
    public V remove(Object key) {
        clearSoftReference();
        return (V) temp.remove(key).get();
    }

    /**
     * 删除temp集合中指定key的value,同时根据指定removeSerializableFileByKey的值决定是否
     * <p>
     * 删除key对应的在序列化目录中的序列化文件,为true时则删除对应的序列化文件,否则不删除.
     */
    public V remove(K key, boolean removeSerializableFileByKey) {
        clearSoftReference();
        SoftValue<K, V> o = temp.remove(key);

        if (removeSerializableFileByKey) {
            deleteSerializableFile(key);
        }

        return o == null ? null : o.get();
    }

    /**
     * 删除指定key的对象对应的序列化文件
     *
     * @param key
     */
    public void deleteSerializableFile(K key) {
        File f = new File(serializableFileDir + "/" + key.toString()
                + serializableFileSuffix);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * 删除SerNotDelete目录下指定的序列化文件
     *
     * @param key
     * @param serializableFileDir
     */
    public void deleteSerializableFile(K key, String serializableFileDir) {
        File f = new File(serializableFileDir + "/" + key.toString()
                + serializableFileSuffix);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * 删除产生的所有对象的序列化文件
     *
     * @return 删除成功则返回true, 否则返回false.
     */
    public boolean deleteAllSerializableFile() {
        return FileUtil.deleteDir(serializableFileDir);
    }

    /**
     * 删除SerNotDelete目录下所有的序列化文件
     *
     * @param serializableFileDir
     * @return
     */
    public boolean deleteAllSerializableFile(String serializableFileDir) {
        return FileUtil.deleteDir(serializableFileDir);
    }

    /**
     * 轮循引用队列,如果引用队列取出来的值不为null,证明软引用中保存的对象已经被回收,
     * <p>
     * 这个时候就要一并把temp集合中的那个软引用对象也一并删除掉.
     */
    @SuppressWarnings("unchecked")
    void clearSoftReference() {
        SoftValue<String, Object> poll = (SoftValue<String, Object>) queue
                .poll();
        while (poll != null) {
            temp.remove(poll.key);
            poll = (SoftValue<String, Object>) queue.poll();
        }
    }

    /**
     * 将temp集合的数据都清空
     */
    @Override
    public void clear() {
        temp.clear();
    }

    /**
     * 在清空temp集合的同时,根据指定deleteSerializableFile参数值决定
     * <p>
     * 是否也将产生的对象的序列化文件也删掉.
     *
     * @param deleteSerializableFile
     */
    public void clear(boolean deleteSerializableFile) {
        if (deleteSerializableFile) {
            deleteAllSerializableFile();
        }
        clear();
    }

    /**
     * 扩展软引用的功能,增加一个key属性,在调用clearSoftReference()方法时会用到这个key属性.
     *
     * @param <K>
     * @param <V>
     * @author Administrator
     */
    @SuppressWarnings("hiding")
    private class SoftValue<K, V> extends SoftReference<V> {
        K key;

        public SoftValue(K key, V r, ReferenceQueue<? super V> q) {
            super(r, q);
            this.key = key;
        }

        @Override
        public String toString() {
            return "SoftValue [key=" + key + "]";
        }
    }

    public interface ICreateObjectAble<V> {
        public V createObject();
    }

    /**
     * 该方法只供LruMap类调用
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public V obtainElement(K key) {
        V v = get(key);
        if (v == null) {
            serializableFilePath = serializableFileDir + "/" + key
                    + serializableFileSuffix;
            File f = new File(serializableFilePath);
            ObjectInputStream ois = null;
            if (f.exists()) {
                // 如果传进来的类在之前有对应的序列化文件存在,那么通过反序列化技术,
                // 产生一个新的对象，在之后调用put()方法后保存到temp集合中,
                // 并同时重新序列化到文件中.
                try {
                    ois = new ObjectInputStream(new FileInputStream(f));
                    v = (V) ois.readObject();
                    putElement(key, v, false);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (ois != null) {
                        try {
                            ois.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return v;
    }

    /**
     * 如果temp中存在指定key的元素,就取出.如果不存在,并且cla有默认无参构造方法,
     * <p>
     * 通过反射方式创建一个新的元素.如果不存在,并且cla没有默认无参构造方法,
     * <p>
     * 通过实现createObjectAble接口方式创建一个新的元素,再放进temp中,并返回新创建的元素.
     *
     * @param key              指定key
     * @param cla              指定Class对象
     * @param createObjectAble 指定创建没有默认无参构造方法的对象的接口.
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object createOrGetElement(K key, Class<V> cla,
                                     ICreateObjectAble<V> createObjectAble) {
        // 以下一行代码必须要有,这样可以确保取出来的元素是正确的.
        // clearSoftReference();

        V v = get(key);
        if (v != null) {
            // 如果temp集合中存在指定key的value,就直接返回该value.
            return v;
        }
        ObjectInputStream ois = null;
        try {
            if (cla.isInterface()) {
                // 如果传进来的是个接口,就根据配置文件,实例化一个该接口的实现类对象.
                v = BeanFactory.getImpl(cla);
            } else {
                // 如果传进来的是一个类
                serializableFilePath = serializableFileDir + "/" + key
                        + serializableFileSuffix;
                File f = new File(serializableFilePath);
                if (!f.exists()) {
                    // 如果传进来的类在之前没有对应的序列化文件存在,那么就直接实例化该类的对象,
                    // 在之后调用put()方法后保存到temp集合中,
                    // 如果该对象自己和父类都没有非静态属性,那么在后续调用put()方法后就不会序列化,
                    // 否则将序列化到文件中,以后再取该类的对象,
                    // 就不需要再实例化了.
                    v = cla.newInstance();
                } else {
                    // 如果传进来的类在之前有对应的序列化文件存在,那么通过反序列化技术,
                    // 产生一个新的对象，在之后调用put()方法后保存到temp集合中,
                    // 并同时重新序列化到文件中.
                    ois = new ObjectInputStream(new FileInputStream(f));
                    v = (V) ois.readObject();
                }
            }
            if (v != null) {
                put(key, v);
            }
            return v;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if (createObjectAble != null) {
                v = createObjectAble.createObject();
                if (v != null) {
                    put(key, v);
                }
                return v;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            if (createObjectAble != null) {
                v = createObjectAble.createObject();
                if (v != null) {
                    put(key, v);
                }
                return v;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}