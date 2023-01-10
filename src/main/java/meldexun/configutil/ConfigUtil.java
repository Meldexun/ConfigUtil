package meldexun.configutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.netty.buffer.ByteBuf;

public class ConfigUtil {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Sync {

	}

	public static <T> void copyClientSettings(T src, T dest) throws ReflectiveOperationException {
		CopyUtil.copy(src, dest, field -> !field.isAnnotationPresent(Sync.class));
	}

	public static <T> void copyAllSettings(T src, T dest) throws ReflectiveOperationException {
		CopyUtil.copy(src, dest, null);
	}

	public static <T> void writeServerSettings(T src, ByteBuf buffer) throws ReflectiveOperationException {
		ByteBufUtil.write(src, buffer, field -> field.isAnnotationPresent(Sync.class));
	}

	public static <T> void readServerSettings(T dest, ByteBuf buffer) throws ReflectiveOperationException {
		ByteBufUtil.read(dest, buffer, field -> field.isAnnotationPresent(Sync.class));
	}

}
