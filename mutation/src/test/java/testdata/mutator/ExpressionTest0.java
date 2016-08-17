package testdata.mutator;

/**
 * Created by hoangtung on 4/5/15.
 */
public class ExpressionTest0
{
    public static void main(String[] args)
    {
        int a = 3 + 2 - 1;
        double b = 3.5;


        double c = a - b / 2 + 4;

        while (a < 3)
        {
            a = (int)(b - 2 + a / 3);
            c = c - 3;
            if (c < 10)
                break;
            else
                c = c + 1;
        }

        {
            double dd = 10;
            dd = dd / 2 * 3 + 1.5;
        }
    }
}
