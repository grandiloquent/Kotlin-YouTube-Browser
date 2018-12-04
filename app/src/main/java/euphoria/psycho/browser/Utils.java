package euphoria.psycho.browser;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
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
