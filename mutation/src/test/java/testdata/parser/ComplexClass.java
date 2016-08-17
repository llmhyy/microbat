package testdata.parser;


import java.util.List;

import mutation.parser.ClassDescriptor;

/**
 * Created by hoangtung on 3/31/15.
 */
public class ComplexClass extends ClassDescriptor implements Comparable<ComplexClass>
{
    public static byte aByte = 0;
    public static final int aInt = 10;
    private static final float aFloat = -10.0f;
    protected static final double aDouble = 10.3;

    static String aString = "aString";
    int firstInt, secondInt, array[];
    int[][] intArr, secondArr[];


    public void emptyMethod()
    {
    }

    private double methodWithParams(double aDouble, int aInt, String aString, ComplexClass complexClass, List<Integer> integerList)
    {
        System.out.println(aString);
        double f = 0;
        f = 3 * 2 - aDouble + aInt;
        int g = (int)f / 2, h = 3 / 2;
        int i, j, k;
        for (int idx = 0; idx < 10; ++idx)
        {
            int gdx = 10, hdx = 32, tdx[] = new int[3];
            g = gdx / hdx / tdx[0] + 2 - 3 - idx;

            while (gdx < 10)
            {
                int temp = gdx / 2;
                gdx += 2 * temp - 3;
            }
        }

        return 0.5 * aDouble + aInt;
    }

    @Override
    public int compareTo(ComplexClass complexClass)
    {
        return 0;
    }

    static class InnerClass
    {
        protected int test;
        char[] cArray;

        class InnClass
        {

        }

        void inFunc()
        {
            System.out.println("abc");
        }
    }

}

class SecondClass
{
    private int anInt;

    public String[] toStringArr()
    {
        return null;
    }
}