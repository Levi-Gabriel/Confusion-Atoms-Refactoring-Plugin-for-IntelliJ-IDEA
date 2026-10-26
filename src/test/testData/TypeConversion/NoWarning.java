package TypeConversion;

public class NoWarning {

    // without narrowing conversion

    void wideningConversions(int i, byte b, char c, float f) {
        long l = (long) i;
        double d = (double) i;
        int fromChar = (int) c;
        short s = (short) b;
        double fromFloat = (double) f;
    }

    void sameType(int i) {
        int x = (int) i;
    }

    void referenceTypes(Object o, Integer boxed) {
        String s = (String) o;
        Integer n = (Integer) o;
        int x = (int) boxed;
    }

    // literals within the range

    void literalsInRange() {
        byte a = (byte) 100;
        short b = (short) 1000;
        char c = (char) 65;
        byte d = (byte) 'a';
        int e = (int) 3.0;
        long f = (long) 2.0;
        float g = (float) 1.5;
    }

    // secure Math methods

    void safeMathMethods(double d) {
        int a = (int) Math.round(d);
        int b = (int) Math.floor(d);
        long c = (long) Math.ceil(d);
        int e = (int) Math.rint(d);
        int f = (int) (Math.round(d) + 1);
    }

    // module

    void moduloOperations(int i, long l) {
        byte a = (byte) (i % 256);
        char b = (char) (l % 65536);
        int c = (int) (l % 10);
    }

    // already limited

    void alreadyClamped(int i, long l) {
        byte a = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, i));
        short b = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, i));
        char c = (char) Math.max((int) Character.MIN_VALUE, Math.min((int) Character.MAX_VALUE, i));
        int d = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, l));
    }
}