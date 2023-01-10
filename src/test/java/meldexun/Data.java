package meldexun;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Assertions;

public class Data {

	public boolean z;
	public byte b;
	public short s;
	public int i;
	public long l;
	public float f;
	public double d;
	public char c;
	public String string;
	public Data data;

	public boolean[] za;
	public byte[] ba;
	public short[] sa;
	public int[] ia;
	public long[] la;
	public float[] fa;
	public double[] da;
	public char[] ca;
	public String[] stringa;
	public Data[] dataa;

	public static void assertEquals(Data d1, Data d2) {
		Assertions.assertEquals(d1 == null, d2 == null);
		if (d1 == null) {
			return;
		}

		Assertions.assertEquals(d1.z, d2.z);
		Assertions.assertEquals(d1.b, d2.b);
		Assertions.assertEquals(d1.s, d2.s);
		Assertions.assertEquals(d1.i, d2.i);
		Assertions.assertEquals(d1.l, d2.l);
		Assertions.assertEquals(d1.f, d2.f);
		Assertions.assertEquals(d1.d, d2.d);
		Assertions.assertEquals(d1.c, d2.c);
		Assertions.assertEquals(d1.string, d2.string);
		assertEquals(d1.data, d2.data);

		Assertions.assertArrayEquals(d1.za, d2.za);
		Assertions.assertArrayEquals(d1.ba, d2.ba);
		Assertions.assertArrayEquals(d1.sa, d2.sa);
		Assertions.assertArrayEquals(d1.ia, d2.ia);
		Assertions.assertArrayEquals(d1.la, d2.la);
		Assertions.assertArrayEquals(d1.fa, d2.fa);
		Assertions.assertArrayEquals(d1.da, d2.da);
		Assertions.assertArrayEquals(d1.ca, d2.ca);
		Assertions.assertArrayEquals(d1.stringa, d2.stringa);
		Assertions.assertEquals(d1.dataa == null, d2.dataa == null);
		if (d1.dataa != null) {
			Assertions.assertEquals(d1.dataa.length, d2.dataa.length);
			for (int i = 0; i < d1.dataa.length; i++) {
				assertEquals(d1.dataa[i], d2.dataa[i]);
			}
		}
	}

	public static Data randomData() {
		Random rand = ThreadLocalRandom.current();
		Data data = new Data();
		data.z = rand.nextBoolean();
		data.b = (byte) rand.nextInt();
		data.s = (short) rand.nextInt();
		data.i = rand.nextInt();
		data.l = rand.nextLong();
		data.f = rand.nextFloat();
		data.d = rand.nextDouble();
		data.c = randomChar(rand);
		data.string = randomString(rand);

		data.za = new boolean[] { rand.nextBoolean() };
		data.ba = new byte[] { (byte) rand.nextInt() };
		data.sa = new short[] { (short) rand.nextInt() };
		data.ia = new int[] { rand.nextInt() };
		data.la = new long[] { rand.nextLong() };
		data.fa = new float[] { rand.nextFloat() };
		data.da = new double[] { rand.nextDouble() };
		data.ca = new char[] { randomChar(rand) };
		data.stringa = new String[] { randomString(rand) };
		return data;
	}

	private static String randomString(Random rand) {
		int length = rand.nextInt(16);
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(randomChar(rand));
		}
		return sb.toString();
	}

	private static char randomChar(Random rand) {
		int i = rand.nextInt(3);
		if (i == 0) {
			return (char) (rand.nextInt(10) + 48);
		}
		if (i == 1) {
			return (char) (rand.nextInt(26) + 65);
		}
		return (char) (rand.nextInt(26) + 97);
	}

}
