package com.t13max.util;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author: t13max
 * @since: 11:36 2024/5/29
 */
@UtilityClass
public class TextUtil {

    public String readText(String fileName) {
        return readIgnore(fileName, null);
    }

    public String readSql(String fileName) {
        return readIgnore(fileName, "--");
    }

    public String readIgnore(String fileName, String ignore) {
        InputStream resourceAsStream = TextUtil.class.getClassLoader().getResourceAsStream(fileName);
        if (resourceAsStream == null) {
            throw new RuntimeException("资源找不到");
        }
        InputStreamReader isr = new InputStreamReader(resourceAsStream);
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(isr)) {

            String lineTxt = null;

            //将文件内容全部拼接到 字符串s
            while ((lineTxt = br.readLine()) != null) {
                if (ignore != null && lineTxt.contains(ignore)) {
                    continue;
                }
                stringBuilder.append(lineTxt);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}
