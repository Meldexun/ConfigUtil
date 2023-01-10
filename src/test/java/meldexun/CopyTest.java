package meldexun;

import org.junit.jupiter.api.Test;

import meldexun.configutil.CopyUtil;

public class CopyTest {

	@Test
	public void testCopy() throws ReflectiveOperationException {
		Data src = Data.randomData();
		src.data = Data.randomData();
		src.dataa = new Data[] { Data.randomData() };

		Data dest = CopyUtil.copy(src, null);
		Data.assertEquals(src, dest);
	}

}
