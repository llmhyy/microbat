package sample0;

public class Operation {
	public void runAssignment() {
		int a = 0;
		int b = a;
	}
	
	public void runIfStmt() {
		int a = 1;
		int b;
		if (a == 1) {
			b = 0;
		} else if (a == 2) {
			b = 1;
		} else {
			b = 2;
		}
		a = 2;
	}

	public void runWhileLoop() {
		int a = 0;
		while (a < 5) {
			int b = 0;
			a++;
		}
	}

	public void runForLoop() {
		int a = 0;
		for (int i = 0; i < 5; i++) {
			a++;
		}
	}

	public void runForEachLoop() {
		int arr[] = new int[] {0, 1};
		int a = 0;
		for (int num : arr) {
			a += num;
		}
	}

	public int callAnotherMethod() {
		Miscellaneous sample1 = new Miscellaneous();
		return sample1.method(1);
	}
}
