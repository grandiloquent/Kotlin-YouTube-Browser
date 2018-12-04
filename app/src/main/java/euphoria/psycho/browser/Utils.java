package euphoria.psycho.browser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static final String UTF_8 = "UTF-8";

    public static String readText(File file, String charsetName) throws IOException {
        return new String(readBytes(file), charsetName);


    }

    public static List<String> readLines(File file, String charset) throws IOException {
        InputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) lines.add(line);
        closeSilently(in);
        return lines;
    }

    public static byte[] readBytes(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        int offset = 0;
        int remaining = (int) file.length();
        byte[] result = new byte[remaining];

        while (remaining > 0) {
            int read = in.read(result, offset, remaining);
            if (read < 0) break;
            remaining -= read;
            offset += read;
        }
        closeSilently(in);
        if (remaining == 0) return result;
        else return Arrays.copyOf(result, offset);
    }

    public static String substringAfterLast(String value, char delimiter) {
        int position = value.lastIndexOf(delimiter);
        if (position >= 0) {

        }
        return value;
    }

    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeSilently(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
