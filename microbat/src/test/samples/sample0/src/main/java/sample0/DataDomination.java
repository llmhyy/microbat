package sample0;

public class DataDomination {
    public void assignmentDataDom() {
        int a = 0;
        a = 1 + 1;
        a--;
        int c = a;
        int b = c;
        b = a;
    }

    public void whileLoopDataDom() {
        int a = 0;
        int b = 0;
        while (a < 3) {
            b += a;
            a++;
        }
        int c = a;
    }

    public void forLoopDataDom() {
        int a = 0;
        int b = 0;
        for (int i = 0; i < 3; i++) {
            b += a;
            a++;
        }
        int c = a;
    }

    public void arrayDataDom() {
        int[] arr = {0, 3, 2};
        arr[1] = 1;
        int b = arr[1];
        int c = arr[0];
    }
}
