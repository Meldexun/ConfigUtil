package meldexun.configutil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

public class ByteBufUtil {

	private static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>();
	static {
		registerSerializer(ByteBuf::writeBoolean, ByteBuf::readBoolean, boolean.class, Boolean.class);
		registerSerializer(ByteBufUtil::writeByte, ByteBuf::readByte, byte.class, Byte.class);
		registerSerializer(ByteBufUtil::writeShort, ByteBuf::readShort, short.class, Short.class);
		registerSerializer(ByteBuf::writeInt, ByteBuf::readInt, int.class, Integer.class);
		registerSerializer(ByteBuf::writeLong, ByteBuf::readLong, long.class, Long.class);
		registerSerializer(ByteBuf::writeFloat, ByteBuf::readFloat, float.class, Float.class);
		registerSerializer(ByteBuf::writeDouble, ByteBuf::readDouble, double.class, Double.class);
		registerSerializer(ByteBufUtil::writeChar, ByteBuf::readChar, char.class, Character.class);
		registerSerializer(ByteBufUtil::writeString, ByteBufUtil::readString, String.class);
	}

	private static void writeByte(ByteBuf buf, byte b) {
		buf.writeByte(b);
	}

	private static void writeShort(ByteBuf buf, short s) {
		buf.writeShort(s);
	}

	private static void writeChar(ByteBuf buf, char c) {
		buf.writeChar(c);
	}

	private static void writeString(ByteBuf buf, String string) {
		byte[] data = string.getBytes(StandardCharsets.UTF_8);
		buf.writeInt(data.length);
		buf.writeBytes(data);
	}

	private static String readString(ByteBuf buf) {
		return buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString();
	}

	@SafeVarargs
	public static <T> void registerSerializer(BiConsumer<ByteBuf, T> writer, Function<ByteBuf, T> reader,
			Class<T>... types) {
		for (Class<T> type : types) {
			SERIALIZERS.put(type, new Serializer<T>() {

				@Override
				public void write(ByteBuf buffer, T t) {
					writer.accept(buffer, t);
				}

				@Override
				public T read(ByteBuf buffer) {
					return reader.apply(buffer);
				}

			});
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Serializer<T> getSerializer(Class<T> type) {
		return (Serializer<T>) SERIALIZERS.get(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> void write(T src, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		if (src == null) {
			buffer.writeBoolean(false);
			return;
		}
		write((Class<T>) src.getClass(), src, buffer, predicate);
	}

	public static <T> void write(Class<T> type, T src, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		buffer.writeBoolean(src != null);
		if (src == null) {
			return;
		}

		Serializer<T> serializer = getSerializer(type);
		if (serializer != null) {
			serializer.write(buffer, src);
			return;
		}

		if (type.isArray()) {
			writeArray(type, src, buffer, predicate);
			return;
		}

		writeObject(type, src, buffer, predicate);
	}

	@SuppressWarnings("unchecked")
	private static <T, R> void writeArray(Class<T> type, T src, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		Class<R> componentType = (Class<R>) type.getComponentType();
		int length = Array.getLength(src);
		buffer.writeInt(length);
		for (int i = 0; i < length; i++) {
			writeComponent(componentType, src, i, buffer, predicate);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T, R> void writeComponent(Class<R> componentType, T src, int index, ByteBuf buffer,
			@Nullable Predicate<Field> predicate) throws ReflectiveOperationException {
		if (componentType == boolean.class) {
			buffer.writeBoolean(Array.getBoolean(src, index));
		} else if (componentType == byte.class) {
			buffer.writeByte(Array.getByte(src, index));
		} else if (componentType == short.class) {
			buffer.writeShort(Array.getShort(src, index));
		} else if (componentType == int.class) {
			buffer.writeInt(Array.getInt(src, index));
		} else if (componentType == long.class) {
			buffer.writeLong(Array.getLong(src, index));
		} else if (componentType == float.class) {
			buffer.writeFloat(Array.getFloat(src, index));
		} else if (componentType == double.class) {
			buffer.writeDouble(Array.getDouble(src, index));
		} else if (componentType == char.class) {
			buffer.writeChar(Array.getChar(src, index));
		} else {
			write(componentType, (R) Array.get(src, index), buffer, predicate);
		}
	}

	private static <T> void writeObject(Class<T> type, T src, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		StreamUtil.forEachUnchecked(streamFields(type, predicate), field -> {
			field.setAccessible(true);
			writeField(src, field, buffer, predicate);
		}, ReflectiveOperationException.class);
	}

	@SuppressWarnings("unchecked")
	private static <T, R> void writeField(T src, Field field, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		Class<R> fieldType = (Class<R>) field.getType();
		if (fieldType == boolean.class) {
			buffer.writeBoolean(field.getBoolean(src));
		} else if (fieldType == byte.class) {
			buffer.writeByte(field.getByte(src));
		} else if (fieldType == short.class) {
			buffer.writeShort(field.getShort(src));
		} else if (fieldType == int.class) {
			buffer.writeInt(field.getInt(src));
		} else if (fieldType == long.class) {
			buffer.writeLong(field.getLong(src));
		} else if (fieldType == float.class) {
			buffer.writeFloat(field.getFloat(src));
		} else if (fieldType == double.class) {
			buffer.writeDouble(field.getDouble(src));
		} else if (fieldType == char.class) {
			buffer.writeChar(field.getChar(src));
		} else {
			write(fieldType, (R) field.get(src), buffer, predicate);
		}
	}

	public static <T> T read(Class<T> type, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		return read(type, null, buffer, predicate);
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(T dest, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		return read((Class<T>) dest.getClass(), dest, buffer, predicate);
	}

	public static <T> T read(Class<T> type, @Nullable T dest, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		if (!buffer.readBoolean()) {
			return null;
		}

		Serializer<T> serializer = getSerializer(type);
		if (serializer != null) {
			return serializer.read(buffer);
		}

		if (type.isArray()) {
			return readArray(type, buffer, predicate);
		}

		return readObject(type, dest, buffer, predicate);
	}

	@SuppressWarnings("unchecked")
	private static <T, R> T readArray(Class<T> type, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		Class<R> componentType = (Class<R>) type.getComponentType();
		int length = buffer.readInt();
		T dest = (T) Array.newInstance(componentType, length);
		for (int i = 0; i < length; i++) {
			readComponent(componentType, dest, i, buffer, predicate);
		}
		return dest;
	}

	private static <T, R> void readComponent(Class<R> componentType, T dest, int index, ByteBuf buffer,
			@Nullable Predicate<Field> predicate) throws ReflectiveOperationException {
		if (componentType == boolean.class) {
			Array.setBoolean(dest, index, buffer.readBoolean());
		} else if (componentType == byte.class) {
			Array.setByte(dest, index, buffer.readByte());
		} else if (componentType == short.class) {
			Array.setShort(dest, index, buffer.readShort());
		} else if (componentType == int.class) {
			Array.setInt(dest, index, buffer.readInt());
		} else if (componentType == long.class) {
			Array.setLong(dest, index, buffer.readLong());
		} else if (componentType == float.class) {
			Array.setFloat(dest, index, buffer.readFloat());
		} else if (componentType == double.class) {
			Array.setDouble(dest, index, buffer.readDouble());
		} else if (componentType == char.class) {
			Array.setChar(dest, index, buffer.readChar());
		} else {
			Array.set(dest, index, read(componentType, null, buffer, predicate));
		}
	}

	private static <T> T readObject(Class<T> type, @Nullable T dest, ByteBuf buffer,
			@Nullable Predicate<Field> predicate) throws ReflectiveOperationException {
		T result = dest != null ? dest : type.newInstance();
		StreamUtil.forEachUnchecked(streamFields(type, predicate), field -> {
			field.setAccessible(true);
			readField(result, field, buffer, predicate);
		}, ReflectiveOperationException.class);
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T, R> void readField(T dest, Field field, ByteBuf buffer, @Nullable Predicate<Field> predicate)
			throws ReflectiveOperationException {
		Class<R> fieldType = (Class<R>) field.getType();
		if (fieldType == boolean.class) {
			field.setBoolean(dest, buffer.readBoolean());
		} else if (fieldType == byte.class) {
			field.setByte(dest, buffer.readByte());
		} else if (fieldType == short.class) {
			field.setShort(dest, buffer.readShort());
		} else if (fieldType == int.class) {
			field.setInt(dest, buffer.readInt());
		} else if (fieldType == long.class) {
			field.setLong(dest, buffer.readLong());
		} else if (fieldType == float.class) {
			field.setFloat(dest, buffer.readFloat());
		} else if (fieldType == double.class) {
			field.setDouble(dest, buffer.readDouble());
		} else if (fieldType == char.class) {
			field.setChar(dest, buffer.readChar());
		} else {
			field.set(dest, read(fieldType, (R) field.get(dest), buffer, predicate));
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

	public interface Serializer<T> {

		void write(ByteBuf buffer, T t) throws ReflectiveOperationException;

		T read(ByteBuf buffer) throws ReflectiveOperationException;

	}

}
