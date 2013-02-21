package inputOutput;
/**
 * 
 * @author http://www.programmersheaven.com/mb/java/296924/296924/int-to-bits-conversion/
 *
 */
public class IntToBytes {

    public static byte[] toBytes(int n) {
        byte[] barr = new byte[4];

        for (int i = barr.length - 1; i >= 0; i--) {
            barr[i] = (byte) n;
            n >>= 8;
        }

        return barr;
    }

    public static void println(byte[] barr) {
        for (int i = 0; i < barr.length; i++) {
            print(barr[i]);
        }
        System.out.println();
    }

    public static void print(byte b) {
        int mask = 0x80;
        while (mask > 0) {
            if ((mask & b) != 0) {
                System.out.print('1');
            } else {
                System.out.print('0');
            }
            mask >>= 1;
        }
    }

    public static void main(String[] args) {
        int[] tests = { 0x00000001, 0xFFFFFFFF, 0x80000000, };

        for (int i = 0; i < tests.length; i++) {
            System.out.print(tests[i] + "\t=\t");
            byte[] barr = toBytes(tests[i]);
            println(barr);
        }
    }
}

