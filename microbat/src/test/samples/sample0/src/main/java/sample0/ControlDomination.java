package sample0;

public class ControlDomination {
    public void ifStmtControlDom() {
        int a = 0;
        int b = 0;
        if (a == 0) {
            b = 1;
        } else {
            b = 2;
        }
        b = 3;
    }

    public void whileLoopControlDom() {
        int a = 0;
        int b = 0;
        while (a < 3) {
            b += a;
            a++;
        }
        b = 0;
    }

    public void forLoopControlDom() {
        int a = 0;
        int b = 0;
        for (int i = 0; i < 3; i++) {
            b += a;
            a++;
        }
        b = 0;
    }
}
