package meldexun.configutil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public class StreamUtil {

	public static Stream<Class<?>> streamClassHierarchy(Class<?> type) {
		return streamClassHierarchy(Stream.builder(), type);
	}

	private static Stream<Class<?>> streamClassHierarchy(Stream.Builder<Class<?>> builder, Class<?> type) {
		return type == Object.class ? builder.build() : streamClassHierarchy(builder.add(type), type.getSuperclass());
	}

	public static Stream<Field> streamFields(Class<?> type) {
		return streamClassHierarchy(type).map(Class::getFields).flatMap(Arrays::stream);
	}

	public static Stream<Field> streamDeclaredFields(Class<?> type) {
		return streamClassHierarchy(type).map(Class::getDeclaredFields).flatMap(Arrays::stream);
	}

	@SuppressWarnings("unchecked")
	public static <T, E extends Throwable> void forEachUnchecked(Stream<T> stream, UncheckedConsumer<T, E> action,
			Class<E> errorType) throws E {
		try {
			stream.forEach(t -> {
				try {
					action.accept(t);
				} catch (Throwable e) {
					if (!errorType.isInstance(e)) {
						throw new UnknownException(e);
					}
					throw new KnownException(e);
				}
			});
		} catch (KnownException e) {
			throw (E) e.getCause();
		}
	}

	public interface UncheckedConsumer<T, E extends Throwable> {

		void accept(T t) throws E;

	}

	@SuppressWarnings("serial")
	private static class KnownException extends RuntimeException {

		public KnownException(Throwable cause) {
			super(cause);
		}

	}

	@SuppressWarnings("serial")
	private static class UnknownException extends RuntimeException {

		public UnknownException(Throwable cause) {
			super(cause);
		}

	}

}
