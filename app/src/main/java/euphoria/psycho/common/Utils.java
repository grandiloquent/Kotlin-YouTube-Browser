package euphoria.psycho.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static final String UTF_8 = "UTF-8";
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    private static final int DEFAULT_BLOCK_SIZE = 4096;
    private static final int MINIMUM_BLOCK_SIZE = 512;

    private static ThreadLocal<Rect> sThreadLocalRect;

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

    private static void compatOffsetLeftAndRight(View view, int offset) {
        view.offsetLeftAndRight(offset);
        if (view.getVisibility() == View.VISIBLE) {
            tickleInvalidationFlag(view);

            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                tickleInvalidationFlag((View) parent);
            }
        }
    }

    private static void compatOffsetTopAndBottom(View view, int offset) {
        view.offsetTopAndBottom(offset);
        if (view.getVisibility() == View.VISIBLE) {
            tickleInvalidationFlag(view);

            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                tickleInvalidationFlag((View) parent);
            }
        }
    }

    private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
        return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
    }

    public static int compositeColors(int foreground, int background) {
        int bgAlpha = Color.alpha(background);
        int fgAlpha = Color.alpha(foreground);
        int a = compositeAlpha(fgAlpha, bgAlpha);

        int r = compositeComponent(Color.red(foreground), fgAlpha,
                Color.red(background), bgAlpha, a);
        int g = compositeComponent(Color.green(foreground), fgAlpha,
                Color.green(background), bgAlpha, a);
        int b = compositeComponent(Color.blue(foreground), fgAlpha,
                Color.blue(background), bgAlpha, a);

        return Color.argb(a, r, g, b);
    }

    private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
        if (a == 0) return 0;
        return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
    }

    public static void copyText(Context context, String text) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("", text));
    }

    public static int dp2px(int value, DisplayMetrics metrics) {
        float f = (float) value * metrics.density;
        final int res = (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
        if (res != 0) return res;
        if (value == 0) return 0;
        if (value > 0) return 1;
        return -1;


        // TypedValue.complexToDimensionPixelSize
    }

    private static Rect getEmptyTempRect() {
        if (sThreadLocalRect == null) {
            sThreadLocalRect = new ThreadLocal<>();
        }
        Rect rect = sThreadLocalRect.get();
        if (rect == null) {
            rect = new Rect();
            sThreadLocalRect.set(rect);
        }
        rect.setEmpty();
        return rect;
    }

//    public static void launchIntent(Context context, Intent intent, List<ResolveInfo> handlers) {
//        if (handlers == null || handlers.size() == 0) {
//            openFallbackBrowserActionsMenu(context, intent);
//            return;
//        } else if (handlers.size() == 1) {
//            intent.setPackage(handlers.get(0).activityInfo.packageName);
//        } else {
//            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TEST_URL));
//            PackageManager pm = context.getPackageManager();
//            ResolveInfo defaultHandler =
//                    pm.resolveActivity(viewIntent, PackageManager.MATCH_DEFAULT_ONLY);
//            if (defaultHandler != null) {
//                String defaultPackageName = defaultHandler.activityInfo.packageName;
//                for (int i = 0; i < handlers.size(); i++) {
//                    if (defaultPackageName.equals(handlers.get(i).activityInfo.packageName)) {
//                        intent.setPackage(defaultPackageName);
//                        break;
//                    }
//                }
//            }
//        }
//        context.startActivity(intent, null);
//    }

    public static void offsetLeftAndRight(View view, int offset) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.offsetLeftAndRight(offset);
        } else if (Build.VERSION.SDK_INT >= 21) {
            final Rect parentRect = getEmptyTempRect();
            boolean needInvalidateWorkaround = false;

            final ViewParent parent = view.getParent();
            if (parent instanceof View) {
                final View p = (View) parent;
                parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
                // If the view currently does not currently intersect the parent (and is therefore
                // not displayed) we may need need to invalidate
                needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(),
                        view.getRight(), view.getBottom());
            }

            // Now offset, invoking the API 14+ implementation (which contains its own workarounds)
            compatOffsetLeftAndRight(view, offset);

            // The view has now been offset, so let's intersect the Rect and invalidate where
            // the View is now displayed
            if (needInvalidateWorkaround && parentRect.intersect(view.getLeft(), view.getTop(),
                    view.getRight(), view.getBottom())) {
                ((View) parent).invalidate(parentRect);
            }
        } else {
            compatOffsetLeftAndRight(view, offset);
        }
    }

    /**
     * Offset this view's vertical location by the specified number of pixels.
     *
     * @param offset the number of pixels to offset the view by
     */
    public static void offsetTopAndBottom(View view, int offset) {
        if (Build.VERSION.SDK_INT >= 23) {
            view.offsetTopAndBottom(offset);
        } else if (Build.VERSION.SDK_INT >= 21) {
            final Rect parentRect = getEmptyTempRect();
            boolean needInvalidateWorkaround = false;

            final ViewParent parent = view.getParent();
            if (parent instanceof View) {
                final View p = (View) parent;
                parentRect.set(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
                // If the view currently does not currently intersect the parent (and is therefore
                // not displayed) we may need need to invalidate
                needInvalidateWorkaround = !parentRect.intersects(view.getLeft(), view.getTop(),
                        view.getRight(), view.getBottom());
            }

            // Now offset, invoking the API 14+ implementation (which contains its own workarounds)
            compatOffsetTopAndBottom(view, offset);

            // The view has now been offset, so let's intersect the Rect and invalidate where
            // the View is now displayed
            if (needInvalidateWorkaround && parentRect.intersect(view.getLeft(), view.getTop(),
                    view.getRight(), view.getBottom())) {
                ((View) parent).invalidate(parentRect);
            }
        } else {
            compatOffsetTopAndBottom(view, offset);
        }
    }

    public static void postInvalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postInvalidateOnAnimation();
        } else {
            view.postInvalidate();
        }
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

    public static List<String> readLines(File file, String charset) throws IOException {
        InputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) lines.add(line);
        closeSilently(in);

        return lines;
    }

    public static String readText(File file, String charsetName) throws IOException {
        return new String(readBytes(file), charsetName);


    }

    public static int setAlphaComponent(int color,
                                        int alpha) {
        if (alpha < 0 || alpha > 255) {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
        return (color & 0x00ffffff) | (alpha << 24);
    }

    public static String substringAfterLast(String value, char delimiter) {
        int position = value.lastIndexOf(delimiter);
        if (position >= 0) {

        }
        return value;
    }

    private static void tickleInvalidationFlag(View view) {
        final float y = view.getTranslationY();
        view.setTranslationY(y + 1);
        view.setTranslationY(y);
    }

    public static long copyTo(InputStream in, OutputStream out) throws IOException {
        long bytesCopied = 0L;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytes = in.read(buffer);
        while (bytes >= 0) {
            out.write(buffer, 0, bytes);
            bytesCopied += bytes;
            bytes = in.read(buffer);
        }
        return bytesCopied;
    }

    public static boolean isNullOrBlank(String str) {
        if (str == null || str.length() == 0) return true;
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (!Character.isWhitespace(array[i])) return false;
        }
        return true;
    }

    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }

    public static String joining(List<String> list, String delimiter) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return list.stream().collect(Collectors.joining(delimiter));
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            int length = list.size();
            for (int i = 0; i < length; i++) {

                stringBuilder.append(list.get(i));
                if (i + 1 < length)
                    stringBuilder.append(delimiter);
            }
            return stringBuilder.toString();
        }
    }

    public static List<String> readLines(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8));
            String line;
            List<String> result = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            closeSilently(is);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
