package meldexun;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import meldexun.configutil.ByteBufUtil;

public class ByteBufTest {

	@Test
	public void testWriteRead() throws ReflectiveOperationException {
		ByteBuf buffer = Unpooled.buffer();

		Data src = Data.randomData();
		src.data = Data.randomData();
		src.dataa = new Data[] { Data.randomData() };

		ByteBufUtil.write(src, buffer, null);
		Data dest = ByteBufUtil.read(Data.class, buffer, null);
		Data.assertEquals(src, dest);
		Assertions.assertEquals(buffer.writerIndex(), buffer.readerIndex());
	}

}
