package navigatorteam.cryptoproxy;



/**
 * Utility class which provides {@code byte <-> string in hexadecimal} conversion static methods.
 */
public class HexStrings {
    /**
     * Utility method to generate a String representation in hexadecimals of a byte array.
     *
     * @param buf the input array
     * @return a String containing the hexadecimal representation of buf
     */
    public static String toHexString(byte[] buf) {
        StringBuilder stringBuilder = new StringBuilder();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < buf.length; i++) {
            stringBuilder
                    .append(Integer.toHexString((buf[i] >> 4) & 0x0f))
                    .append(Integer.toHexString(buf[i] & 0x0f));
        }
        return stringBuilder.toString();

    }

    /**
     * Utility method to generate a byte array by parsing a String containing an exadecimal representation of the byte
     * sequence.
     *
     * @param str the input string
     * @return the byte array
     */
    public static byte[] fromHexStringToBuffer(String str) {
        byte[] buf = new byte[str.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
        }
        return buf;
    }
}
