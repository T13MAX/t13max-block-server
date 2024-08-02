package com.t13max.persist.modify;

import com.t13max.persist.data.IPersistData;
import com.t13max.util.PackageUtil;
import com.t13max.util.StringUtil;
import dev.morphia.annotations.Entity;
import dev.morphia.query.Update;
import javassist.*;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * 修改所有数据类 set方法加上update
 *
 * @author: t13max
 * @since: 18:05 2024/8/2
 */
public class DBDataGen {

    public static String X_BEAN_PATH = "com.t13max.persist.collection";

    public static void init() {
        try {
            Set<Class<?>> classSet = PackageUtil.scanCache("com.t13max.persist.data");
            for (Class<?> clazz : classSet) {
                if (!IPersistData.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                ClassPool cp = ClassPool.getDefault();
                // 解决按依赖和模块单独打成Jar包后找不到class问题
                if (javassist.bytecode.ClassFile.MAJOR_VERSION < javassist.bytecode.ClassFile.JAVA_9) {
                    cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
                }
                CtClass ctClass = cp.get(clazz.getName());
                Object annotation = ctClass.getAnnotation(Entity.class);
                if (annotation == null) {
                    continue;
                }
                ctClass.addInterface(cp.get(Update.class.getName()));
                ctClass.addField(CtField.make("public volatile byte _option;", ctClass));
                ctClass.addMethod(CtMethod.make("public void clear(){ _option = 0;}", ctClass));
                ctClass.addMethod(CtMethod.make("public byte option(){ return _option;}", ctClass));
                CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
                for (CtMethod method : declaredMethods) {
                    String methodName = method.getName();
                    if (methodName.startsWith("set")) {
                        method.insertAfter("update();");
                        CtClass[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes == null || parameterTypes.length == 0) {
                            continue;
                        }
                        CtClass ctParam = parameterTypes[0];
                        if (isDBContainer(ctParam)) {
                            method.insertAfter("$1.setUpdate($0);");
                        }
                    } else if (methodName.startsWith("get")) {
                        CtClass returnType = method.getReturnType();
                        if (isDBContainer(returnType)) {
                            String temp = methodName.substring(3, methodName.length());
                            String fieldName = StringUtil.toLowerCaseFirstOne(temp);
                            method.insertAfter("if(" + fieldName + " != null){" + fieldName + ".setUpdate($0);}");
                        }
                    }
                }
                ctClass.addMethod(CtMethod.make("public void update(){ _option = _option | 1;}", ctClass));
                ctClass.addMethod(CtMethod.make("public void insert(){ _option = _option | 2;}", ctClass));
                ctClass.toClass();
            }
        } catch (Exception e) {

        }
    }

    public static boolean isXList(CtClass ctClass) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        return ctClass == cp.get(X_BEAN_PATH + ".XList");
    }

    public static boolean isXMap(CtClass ctClass) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        return ctClass == cp.get(X_BEAN_PATH + ".XMap");
    }

    public static boolean isXSet(CtClass ctClass) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        return ctClass == cp.get(X_BEAN_PATH + ".XSet");
    }

    public static boolean isDBContainer(CtClass ctClass) throws Exception {
        if (ctClass == null) {
            return false;
        }
        ClassPool cp = ClassPool.getDefault();
        if (isXList(ctClass) || isXMap(ctClass) || isXSet(ctClass)) {
            return true;
        }
        return false;
    }

}
