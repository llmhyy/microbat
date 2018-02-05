package microbat.instrumentation.trace.testdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sample {
	private int field;
	private static int randomValue;
	static {
		randomValue = new Random().nextInt();
	}
	static {
		System.out.println("another init");
	}

	public void testArr() {
		int[][] a = new int[][] { { 1, 2 }, { 3, 4 } };
		field = 2; 
		for (int i = 0; i < 2; i++) {
			field++;
			a[0][i] = field;
		}
	}

	public void testArrayList() {
		List<int[]> b = new ArrayList<>();
		b.add(new int[] { 1, 2 });
		b.add(new int[] { 3, 4 });
		field = 2;
		for (int i = 0; i < 2; i++) {
			field++;
			b.get(0)[i] = field;
			System.out.println(randomValue);
		}
	}

	public static void main(String[] args) {
		Sample s = new Sample();
		s.testArrayList();
	}
}
