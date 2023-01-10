package meldexun.configutil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class CopyUtil {

	private static final Set<Class<?>> IMMUTABLE_CLASSES = new HashSet<>();
	static {
		registerImmutableClass(Boolean.class);
		registerImmutableClass(Byte.class);
		registerImmutableClass(Short.class);
		registerImmutableClass(Integer.class);
		registerImmutableClass(Long.class);
		registerImmutableClass(Float.class);
		registerImmutableClass(Double.class);
		registerImmutableClass(Character.class);
		registerImmutableClass(String.class);
	}

	public static void registerImmutableClass(Class<?> type) {
		IMMUTABLE_CLASSES.add(type);
	}

	private static boolean isImmutable(Class<?> type) {
		return type.isPrimitive() || IMMUTABLE_CLASSES.contains(type);
	}

	public static <T> T copy(@Nullable T src, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		return copy(src, null, predicate);
	}

	public static <T> T copy(Class<T> type, @Nullable T src, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		return copy(type, src, null, predicate);
	}

	@SuppressWarnings("unchecked")
	public static <T> T copy(@Nullable T src, @Nullable T dest, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		if (src == null) {
			return null;
		}

		return copy((Class<T>) src.getClass(), src, dest, predicate);
	}

	public static <T> T copy(Class<T> type, @Nullable T src, @Nullable T dest, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		if (src == null) {
			return null;
		}

		if (isImmutable(type)) {
			return src;
		}

		if (type.isArray()) {
			return copyArray(type, src, predicate);
		}

		return copyObject(type, src, dest, predicate);
	}

	@SuppressWarnings("unchecked")
	private static <T, R> T copyArray(Class<T> type, T src, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		Class<R> componentType = (Class<R>) type.getComponentType();
		int length = Array.getLength(src);
		T dest = (T) Array.newInstance(componentType, length);
		for (int i = 0; i < length; i++) {
			copyComponent(componentType, src, dest, i, predicate);
		}
		return dest;
	}

	@SuppressWarnings("unchecked")
	private static <T, R> void copyComponent(Class<R> componentType, T src, T dest, int index,
			@Nullable Predicate<Field> predicate) throws ReflectiveOperationException {
		if (componentType == boolean.class) {
			Array.setBoolean(dest, index, Array.getBoolean(src, index));
		} else if (componentType == byte.class) {
			Array.setByte(dest, index, Array.getByte(src, index));
		} else if (componentType == short.class) {
			Array.setShort(dest, index, Array.getShort(src, index));
		} else if (componentType == int.class) {
			Array.setInt(dest, index, Array.getInt(src, index));
		} else if (componentType == long.class) {
			Array.setLong(dest, index, Array.getLong(src, index));
		} else if (componentType == float.class) {
			Array.setFloat(dest, index, Array.getFloat(src, index));
		} else if (componentType == double.class) {
			Array.setDouble(dest, index, Array.getDouble(src, index));
		} else if (componentType == char.class) {
			Array.setChar(dest, index, Array.getChar(src, index));
		} else {
			Array.set(dest, index, copy(componentType, (R) Array.get(src, index), predicate));
		}
	}

	private static <T> T copyObject(Class<T> type, T src, @Nullable T dest, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		T result = dest != null ? dest : type.newInstance();
		StreamUtil.forEachUnchecked(streamFields(type, predicate), field -> {
			field.setAccessible(true);
			copyField(src, result, field, predicate);
		}, ReflectiveOperationException.class);
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T, R> void copyField(T src, T dest, Field field, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		Class<R> fieldType = (Class<R>) field.getType();
		if (fieldType == boolean.class) {
			field.setBoolean(dest, field.getBoolean(src));
		} else if (fieldType == byte.class) {
			field.setByte(dest, field.getByte(src));
		} else if (fieldType == short.class) {
			field.setShort(dest, field.getShort(src));
		} else if (fieldType == int.class) {
			field.setInt(dest, field.getInt(src));
		} else if (fieldType == long.class) {
			field.setLong(dest, field.getLong(src));
		} else if (fieldType == float.class) {
			field.setFloat(dest, field.getFloat(src));
		} else if (fieldType == double.class) {
			field.setDouble(dest, field.getDouble(src));
		} else if (fieldType == char.class) {
			field.setChar(dest, field.getChar(src));
		} else {
			field.set(dest, copy(fieldType, (R) field.get(src), (R) field.get(dest), predicate));
		}
	}

	private static Stream<Field> streamFields(Class<?> type, @Nullable Predicate<Field> predicate) {
		Stream<Field> fields = StreamUtil.streamDeclaredFields(type)
				.filter(field -> !Modifier.isStatic(field.getModifiers()));
		if (predicate != null) {
			fields = fields.filter(predicate);
		}
		fields = fields.sorted(Comparator.comparing(Field::getName));
		return fields;
	}

}
