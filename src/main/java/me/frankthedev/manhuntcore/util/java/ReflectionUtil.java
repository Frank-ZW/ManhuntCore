package me.frankthedev.manhuntcore.util.java;

import java.lang.reflect.Field;

public class ReflectionUtil {

	@SuppressWarnings("rawtypes")
	public static Field access(Class class_, String string) {
		try {
			Field field = class_.getDeclaredField(string);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException noSuchFieldException) {
			throw new IllegalArgumentException(class_.getSimpleName() + ":" + string, noSuchFieldException);
		}
	}

	@SuppressWarnings({ "unchecked"})
	public static <T> T fetch(Field field, Object object) {
		try {
			return (T)field.get(object);
		} catch (IllegalAccessException illegalAccessException) {
			throw new IllegalArgumentException(illegalAccessException);
		}
	}

	@SuppressWarnings({ "rawtypes"})
	public static <T> void setLocalField(Class class_, Object object, String string, T t) {
		ReflectionUtil.set(ReflectionUtil.access(class_, string), object, t);
	}

	public static <T> void set(Field field, Object object, T t) {
		try {
			field.set(object, t);
		} catch (IllegalAccessException illegalAccessException) {
			throw new IllegalArgumentException(illegalAccessException);
		}
	}

	@SuppressWarnings("rawtypes")
	public static <T> T getLocalField(Class class_, Object object, String string) {
		return ReflectionUtil.fetch(ReflectionUtil.access(class_, string), object);
	}
}
