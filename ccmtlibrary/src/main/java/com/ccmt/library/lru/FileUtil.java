package com.ccmt.library.lru;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    public static boolean deleteDir(String dirPath) {
        if (dirPath == null) {
            return false;
        }
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            deleteFile(dir);
            // 目录中的所有子目录和文件删除完后,可以删除空目录,也可以不删除空目录.
            return dir.delete();
        }

        return false;
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File children[] = file.listFiles();
            if (children != null) {
                for (File f : children) {
                    deleteFile(f);
                    f.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    public static void createFile(File file, boolean readable, boolean writable, boolean executable, boolean readableOwnerOnly,
                                  boolean writableOwnerOnly, boolean executableOwnerOnly) {
        createParentFile(file, readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly);
        if (!file.exists()) {
            // 文件不存在
            try {
                if (file.createNewFile()) {
                    setRwe(readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 文件存在
            setRwe(readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly, file);
        }
    }

    public static void createFile(String path, boolean readable, boolean writable, boolean executable, boolean readableOwnerOnly,
                                  boolean writableOwnerOnly, boolean executableOwnerOnly) {
        createFile(new File(path), readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly);
    }

    public static void createFile(String path, boolean readable, boolean writable, boolean executable, boolean ownerOnly) {
        createFile(new File(path), readable, writable, executable, ownerOnly, ownerOnly, ownerOnly);
    }

    public static void createFile(String path, boolean ownerOnly) {
        createFile(new File(path), true, true, true, ownerOnly, ownerOnly, ownerOnly);
    }

    public static void createFile(File file) {
        createFile(file, true, true, true, true, true, true);
    }

    public static void createFile(String path) {
        createFile(new File(path), true, true, true, true, true, true);
    }

    /**
     * 设置可读可写可执行权限
     *
     * @param readable
     * @param writable
     * @param executable
     * @param readableOwnerOnly
     * @param writableOwnerOnly
     * @param executableOwnerOnly
     * @param file
     */
    private static void setRwe(boolean readable, boolean writable, boolean executable, boolean readableOwnerOnly, boolean writableOwnerOnly,
                               boolean executableOwnerOnly, File file) {
        if (readableOwnerOnly) {
            if (readable) {
                file.setReadable(true);
            } else {
                file.setReadable(false);
            }
        } else {
            if (readable) {
                file.setReadable(true, false);
            } else {
                file.setReadable(false, false);
            }
        }
        if (writableOwnerOnly) {
            if (writable) {
                file.setWritable(true);
            } else {
                file.setWritable(false);
            }
        } else {
            if (writable) {
                file.setWritable(true, false);
            } else {
                file.setWritable(false, false);
            }
        }
        if (executableOwnerOnly) {
            if (executable) {
                file.setExecutable(true);
            } else {
                file.setExecutable(false);
            }
        } else {
            if (executable) {
                file.setExecutable(true, false);
            } else {
                file.setExecutable(false, false);
            }
        }
    }

    public static void createParentFile(File file, boolean readable, boolean writable, boolean executable, boolean readableOwnerOnly,
                                        boolean writableOwnerOnly, boolean executableOwnerOnly) {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
            setRwe(readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly, parentFile);
        }
    }

    public static void createParentFile(File file, boolean ownerOnly) {
        createParentFile(file, true, true, true, ownerOnly, ownerOnly, ownerOnly);
    }

    public static void createParentFile(String path, boolean ownerOnly) {
        createParentFile(new File(path), true, true, true, ownerOnly, ownerOnly, ownerOnly);
    }

    public static void createParentFile(File file) {
        createParentFile(file, true, true, true, true, true, true);
    }

    public static void createParentFile(String path) {
        createParentFile(new File(path), true, true, true, true, true, true);
    }

    public static void createDir(File file, boolean readable, boolean writable, boolean executable, boolean readableOwnerOnly,
                                 boolean writableOwnerOnly, boolean executableOwnerOnly) {
        createParentFile(file, readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly);
        if (!file.exists()) {
            // 目录不存在
            if (file.mkdirs()) {
                setRwe(readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly, file);
            }
        } else {
            // 目录存在
            setRwe(readable, writable, executable, readableOwnerOnly, writableOwnerOnly, executableOwnerOnly, file);
        }
    }

    public static void createDir(File file, boolean readable, boolean writable, boolean executable, boolean ownerOnly) {
        createDir(file, readable, writable, executable, ownerOnly, ownerOnly, ownerOnly);
    }

    public static void createDir(File file, boolean ownerOnly) {
        createDir(file, true, true, true, ownerOnly, ownerOnly, ownerOnly);
    }

    public static void createDir(File file) {
        createDir(file, true, true, true, true, true, true);
    }

    public static void createDir(String path) {
        createDir(new File(path), true, true, true, true, true, true);
    }

}