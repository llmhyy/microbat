package mutation.parser;

/**
 * Created by hoangtung on 3/31/15.
 */
public class Modifier
{
    // from ModifierSet.java in Javaparser
    public static final int PUBLIC = 1;
    public static final int PRIVATE = 2;
    public static final int PROTECTED = 4;
    public static final int STATIC = 8;
    public static final int FINAL = 16;
    public static final int SYNCHRONIZED = 32;
    public static final int VOLATILE = 64;
    public static final int TRANSIENT = 128;
    public static final int NATIVE = 256;
    public static final int ABSTRACT = 1024;
    public static final int STRICTFP = 2048;
}
