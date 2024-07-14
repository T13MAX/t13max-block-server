package com.t13max.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author: t13max
 * @since: 14:15 2024/5/23
 */
public class PackageUtil {

    /**
     * 扫描类
     *
     * @Author t13max
     * @Date 14:18 2024/5/23
     */
    public static Set<Class<?>> scan(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        boolean recursive = true;
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if (null != protocol) {
                    switch (protocol) {
                        case "file":
                            String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                            findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                            break;
                        case "jar":
                            JarFile jar;
                            try {
                                jar = ((JarURLConnection) url.openConnection()).getJarFile();
                                Enumeration<JarEntry> entries = jar.entries();
                                while (entries.hasMoreElements()) {
                                    JarEntry entry = entries.nextElement();
                                    String name = entry.getName();
                                    if (name.charAt(0) == '/') {
                                        name = name.substring(1);
                                    }
                                    if (name.startsWith(packageDirName)) {
                                        int idx = name.lastIndexOf('/');
                                        if (idx != -1) {
                                            packageName = name.substring(0, idx).replace('/', '.');
                                        }
                                        if ((idx != -1) || recursive) {
                                            if (name.endsWith(".class") && !entry.isDirectory()) {
                                                String className = name.substring(packageName.length() + 1, name.length() - 6);
                                                try {
                                                    classes.add(Class.forName(packageName + '.' + className));
                                                } catch (ClassNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @Author t13max
     * @Date 14:18 2024/5/23
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
