package com.ccmt.library.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩、解压缩工具类
 * db文件过大时，考虑压缩后进行打包
 */
public class GzipUtil {

    public static void unzip(InputStream is, OutputStream os) {
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(is);
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuitely(gis);
            closeQuitely(os);
        }
    }

    public static void zip(File srcFile, File targetFile) {
        FileInputStream fis = null;
        GZIPOutputStream gos = null;
        try {
            fis = new FileInputStream(srcFile);
            gos = new GZIPOutputStream(new FileOutputStream(targetFile));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuitely(gos);
            closeQuitely(fis);
        }
    }

    public static boolean zipFiles(File fs[], File zf) {
        boolean result = false;
        ZipOutputStream gos = null;
        FileInputStream fis = null;
        try {
            final int buff_len = 1024;
            byte buff[] = new byte[buff_len];

            gos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zf), 1024 * 4));
            for (int i = 0; i < fs.length; i++) {
                if (fs[i] == null)
                    continue;
                ZipEntry ze = new ZipEntry(fs[i].getName());
                gos.putNextEntry(ze);
                fis = new FileInputStream(fs[i]);
                while (true) {
                    int count = fis.read(buff, 0, buff_len);
                    if (count <= 0) {
                        break;
                    }
                    gos.write(buff, 0, count);
                }
                gos.closeEntry();
                fis.close();
                fis = null;
            }
            gos.flush();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuitely(gos);
            closeQuitely(fis);
        }

        return result;
    }

    public static void unzip(File srcFile, File targetFile) {
        GZIPInputStream gis = null;
        FileOutputStream fos = null;
        try {
            gis = new GZIPInputStream(new FileInputStream(srcFile));
            fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuitely(gis);
            closeQuitely(fos);
        }
    }

    public static void closeQuitely(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
