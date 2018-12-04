package euphoria.psycho.browser;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.*;

public class BrowserFragment extends Fragment {

    private static final int CACHE_SIZE = 1024 * 1024 * 8;
    private static final String DEFAULT_FILTER =
            "54新觀點|从台湾看见世界|寰宇新聞 頻道|美国之音中文网|夢想街之全能事務所|民視新聞|明鏡火拍|年代向錢看|三立iNEWS|三立LIVE新聞|台視新聞 TTV NEWS|头条軍事【军事头条 軍情諜報 軍事解密 每日更新】歡迎訂閱|香港全城討論區|新聞面對面|新聞追追追";
    private static final String DEFAULT_URI = "https://m.youtube.com";
    private static final int INITIAL_PROGRESS = 5;
    private static final String KEY_FILTER = "filter";
    private static final String KEY_URI = "uri";
    private static final String PATTERN_URL = "m.youtube.com";
    private static final String SEARCH_MARK = "/*mark for changed*/";
    private String mCurrentUri = "";
    private String mFilters;
    private String mFilterSource;
    private Boolean mIsFilter = true;
    private WebView mWebView;
    private EditText mTextUri;
    private ProgressBar mProgressBar;

    private void applyFilter() {
        if (mIsFilter && mCurrentUri.contains(PATTERN_URL)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(mFilterSource, it -> {

                });
            }
        }
    }

    private void getFilterSource() {
        InputStream inputStream = getResources().openRawResource(R.raw.block_channel);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            if (TextUtils.isEmpty(mFilters)) {
                for (String line; (line = reader.readLine()) != null; ) {
                    stringBuilder.append(line).append('\n');
                }
            } else {
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.trim().startsWith(SEARCH_MARK)) {
                        stringBuilder.append(SEARCH_MARK).append('"').append(mFilters).append("\";\n");
                    } else {
                        stringBuilder.append(line).append('\n');
                    }
                }
            }
            mFilterSource = stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.closeSilently(inputStream);
    }

    private void loadUri() {
        mWebView.loadUrl(mCurrentUri);
    }

    private void setupWebView() {
        mWebView.clearCache(false);
        mWebView.setLongClickable(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setProgress(INITIAL_PROGRESS);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {
                        mProgressBar.setProgress(newProgress);
                    }
                });

                mWebView.loadUrl(url);
                return true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                mTextUri.setText(mCurrentUri);
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                applyFilter();
            }
        });
        WebSettings settings = mWebView.getSettings();
        File cacheDirectory = new File(Environment.getExternalStorageDirectory(), ".cache_browser");
        if (!cacheDirectory.isDirectory()) {
            cacheDirectory.mkdir();
        }
        settings.setAppCachePath(cacheDirectory.getAbsolutePath());
        settings.setAppCacheMaxSize(CACHE_SIZE);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupWebView();
        loadUri();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mCurrentUri = preferences.getString(KEY_URI, DEFAULT_URI);
        mFilters = preferences.getString(KEY_FILTER, DEFAULT_FILTER);
        getFilterSource();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);

        mWebView = view.findViewById(R.id.web_view);
        mProgressBar = view.findViewById(R.id.progress);
        mTextUri = view.findViewById(R.id.text_url);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                        return true;
                    }
                }
                return false;
            });
        }
    }

}
