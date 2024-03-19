package com.sky.utils;

import com.sky.constant.MessageConstant;
import com.sky.exception.PasswordEditFailedException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5EncoderUtil {

        // 将字节数组转换为十六进制字符串
        private static String bytesToHex(byte[] bytes) {
                StringBuilder result = new StringBuilder();
                for (byte b : bytes) {
                        result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                }
                return result.toString();
        }

        // 对字符串进行 MD5 加密
        public static String encode(String input) {
                try {
                        // 创建一个 MD5 摘要算法实例
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        // 计算摘要
                        byte[] digest = md.digest(input.getBytes());
                        // 将摘要转换为十六进制字符串
                        return bytesToHex(digest);
                } catch (NoSuchAlgorithmException e) {
                        return null;
                }
        }
}

