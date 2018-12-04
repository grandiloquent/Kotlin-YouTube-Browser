package euphoria.psycho.browser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import euphoria.psycho.browser.floating.FloatingActionButton;
import euphoria.psycho.browser.floating.FloatingActionsMenu;

import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class BrowserFragment extends Fragment {

    private static final int CACHE_SIZE = 1024 * 1024 * 8;
    private static final String DEFAULT_FILTER = "54新觀點|ADVChina|China Uncensored|laowhy86|serpentza|SETN三立新聞網|从台湾看见世界|風傳媒 The Storm Media|寰宇新聞 頻道|洛杉矶华人资讯网How视频|美国之音中文网|夢想街之全能事務所|民視新聞|民視綜合頻道|明鏡火拍|年代向錢看|三立iNEWS|三立LIVE新聞|台視新聞 TTV NEWS|头条軍事【军事头条 軍情諜報 軍事解密 每日更新】歡迎訂閱|香港全城討論區|新聞面對面|新聞追追追";


    private static final String DEFAULT_URI = "https://m.youtube.com";
    private static final int INITIAL_PROGRESS = 5;
    private static final String KEY_FILTER = "filter";
    private static final String KEY_URI = "uri";
    private static final String KEY_IS_FILTER = "is_filter";
    private static final String PATTERN_URL = "m.youtube.com";
    private static final String SEARCH_MARK = "/*mark for changed*/";
    private String mCurrentUri = "";
    private String mFilters;
    private String mFilterSource;
    private Boolean mIsFilter = true;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private FloatingActionsMenu mFloating;

    private void applyFilter() {
        if (mIsFilter && mCurrentUri.contains(PATTERN_URL)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(mFilterSource, it -> {

                });
            }
        }
    }

    private void setupControls(View view) {
        if (mFloating == null) mFloating = view.findViewById(R.id.floating_menu);
        view.findViewById(R.id.floating_refresh).setOnClickListener(e -> {
            mWebView.reload();
            mFloating.collapse();
        });
        view.findViewById(R.id.floating_go_back).setOnClickListener(e -> {
            mWebView.goBack();
            mFloating.collapse();
        });
        view.findViewById(R.id.floating_home).setOnClickListener(e -> {
            mCurrentUri = DEFAULT_URI;
            loadUri();
            mFloating.collapse();
        });
        FloatingActionButton actionButton = view.findViewById(R.id.floating_lock);
        actionButton.setOnClickListener(e -> {

            if (mIsFilter) {
                actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_black_24dp));
                actionButton.setTitle("未屏蔽");
            } else {
                actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_outline_black_24dp));
                actionButton.setTitle("屏蔽中");

            }
            mIsFilter = !mIsFilter;
            mFloating.collapse();
        });
        view.findViewById(R.id.floating_filter).setOnClickListener(e -> {
            final EditText editText = new EditText(getActivity());

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editText.setText(preferences.getString(KEY_FILTER, DEFAULT_FILTER));
            new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Dialog)
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                        String filter = editText.getText().toString();
                        preferences.edit().putString(KEY_FILTER, filter).apply();
                        mFilters = filter;
                        getFilterSource();

                        dialog.dismiss();
                    }))
                    .show();
            mFloating.collapse();
        });
    }

    private static String sortFilters(String value) {
        Collator collator = Collator.getInstance(Locale.CHINA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(value.split("\\|"))
                    .sorted(collator::compare)
                    .distinct()
                    .collect(Collectors.joining("|"));

        } else {
            List<String> list = new ArrayList<>();

            for (String i : value.split("\\|")) {
                if (list.indexOf(i) > -1) continue;
                list.add(i);
            }
            Collections.sort(list, collator::compare);

            StringBuilder stringBuilder = new StringBuilder();
            for (String i : list) {
                stringBuilder.append(i).append('|');
            }
            return stringBuilder.toString();
        }

    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(KEY_URI, mCurrentUri)
                .putBoolean(KEY_IS_FILTER, mIsFilter).apply();
        super.onDestroy();
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

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        mWebView.setOnLongClickListener(v ->
        {
            handleLongClick();
            return false;
        });

        mWebView.clearCache(false);
        mWebView.setLongClickable(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                mProgressBar.setProgress(newProgress);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(INITIAL_PROGRESS);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {


                mWebView.loadUrl(url);
                return true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                if (mProgressBar.getVisibility() == View.VISIBLE) {


                    mProgressBar.setVisibility(View.GONE);
                }
                applyFilter();
            }
        });
        WebSettings settings = mWebView.getSettings();
        File cacheDirectory = new File(Environment.getExternalStorageDirectory(), ".cache_browser");
        if (!cacheDirectory.isDirectory()) {
            cacheDirectory.mkdir();
        }
        // Cache
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(cacheDirectory.getAbsolutePath());
        settings.setAppCacheMaxSize(CACHE_SIZE);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Storage & Database
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);

        settings.setJavaScriptEnabled(true);

        settings.setSupportZoom(false);
        settings.setDisplayZoomControls(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupWebView();
        loadUri();

    }

    private void handleLongClick() {
        WebView.HitTestResult result = mWebView.getHitTestResult();
        switch (result.getType()) {
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                showMenu(result.getExtra());
                break;
        }
    }

    private void showMenu(String line) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.menu_link, null);

        ((TextView) view.findViewById(R.id.text_link)).setText(line);

        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        view.findViewById(R.id.text_copy_text).setOnClickListener(v -> {
            Utils.copyText(getActivity(), line);
            popupWindow.dismiss();
        });
        popupWindow.showAtLocation(getView().findViewById(R.id.relative_layout), Gravity.CENTER, 0, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mCurrentUri = preferences.getString(KEY_URI, DEFAULT_URI);
        mFilters = preferences.getString(KEY_FILTER, DEFAULT_FILTER);
        mIsFilter = preferences.getBoolean(KEY_IS_FILTER, true);
        getFilterSource();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);

        mWebView = view.findViewById(R.id.web_view);
        mProgressBar = view.findViewById(R.id.progress);
        setupControls(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mWebView.setOnKeyListener((v, keyCode, event) -> {
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
