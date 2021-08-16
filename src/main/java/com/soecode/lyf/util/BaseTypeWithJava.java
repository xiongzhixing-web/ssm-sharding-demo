package com.soecode.lyf.util;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

public class BaseTypeWithJava {

    public static Boolean isBaseType(Class proCls) {
        if (Boolean.class == proCls || boolean.class == proCls) {
            return Boolean.TRUE;
        } else if (Byte.class == proCls || byte.class == proCls) {
            return Boolean.TRUE;
        } else if (Character.class == proCls || char.class == proCls) {
            return Boolean.TRUE;
        } else if (Short.class == proCls || short.class == proCls) {
            return Boolean.TRUE;
        } else if (Integer.class == proCls || int.class == proCls) {
            return Boolean.TRUE;
        } else if (Long.class == proCls || long.class == proCls) {
            return Boolean.TRUE;
        } else if (Float.class == proCls || float.class == proCls) {
            return Boolean.TRUE;
        } else if (Double.class == proCls || double.class == proCls) {
            return Boolean.TRUE;
        } else if (String.class == proCls) {
            return Boolean.TRUE;
        } else if (Date.class == proCls) {
            return Boolean.TRUE;
        } else if (BigDecimal.class == proCls) {
            return Boolean.TRUE;
        } else if (proCls.isEnum()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static Object setBaseType(Class proCls) {
        if (Boolean.class == proCls || boolean.class == proCls) {
            return generateBoolean();
        } else if (Byte.class == proCls || byte.class == proCls) {
            return generateByte();
        } else if (Character.class == proCls || char.class == proCls) {
            return generateChar();
        } else if (Short.class == proCls || short.class == proCls) {
            return generateShort();
        } else if (Integer.class == proCls || int.class == proCls) {
            return generateInteger();
        } else if (Long.class == proCls || long.class == proCls) {
            return generateLong();
        } else if (Float.class == proCls || float.class == proCls) {
            return generateFloat();
        } else if (Double.class == proCls || double.class == proCls) {
            return generateDouble();
        } else if (String.class == proCls) {
            return generateString();
        } else if (Date.class == proCls) {
            return generateDate();
        } else if (BigDecimal.class == proCls) {
            return generateBigDecimal();
        } else if (proCls.isEnum()) {
            //枚举
            return EnumUtils.getEnumList(proCls).get(0);
        }
        return null;
    }

    private static boolean generateBoolean() {
        return ((int) (Math.random() * 2) == 0) ? false : true;
    }

    private static byte generateByte() {
        return (byte) ((int) (Math.random() * 256) - 128);
    }

    private static char generateChar() {
        int temp = (int) (Math.random() * 26);
        return (char) (temp + (temp % 2 == 0 ? 65 : 97));
    }

    private static short generateShort() {
        return (short) ((int) (Math.random() * Math.pow(2, 16)) + Short.MIN_VALUE);
    }

    public static double generateDouble() {
        return Double.MIN_VALUE + ((Double.MAX_VALUE - Double.MIN_VALUE) * new Random().nextDouble());
    }

    public static float generateFloat() {
        return Float.MIN_VALUE + ((Float.MAX_VALUE - Float.MIN_VALUE) * new Random().nextFloat());
    }

    private static String generateString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ((int) (Math.random() * 20) + 5); i++) {
            stringBuilder.append(generateChar());
        }
        return stringBuilder.toString();
    }

    private static Date generateDate() {
        Date now = new Date();
        return DateUtils.addDays(now, ((int) (Math.random() * 365) - 365));
    }

    private static BigDecimal generateBigDecimal() {
        return new BigDecimal(((int) (Math.random() * 10)));
    }

    private static Integer generateInteger() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    private static Long generateLong() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }
}
