package euphoria.psycho.browser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import euphoria.psycho.browser.floating.FloatingActionButton;
import euphoria.psycho.browser.floating.FloatingActionsMenu;
import euphoria.psycho.common.Utils;

import java.io.*;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class BrowserFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final int CACHE_SIZE = 1024 * 1024 * 8;
    private static final String DEFAULT_URI = "https://m.youtube.com";
    private static final int INITIAL_PROGRESS = 5;
    private static final String KEY_FILTER = "filter";
    private static final String KEY_URI = "uri";
    private static final String KEY_IS_FILTER = "is_filter";
    private static final String PATTERN_URL = "m.youtube.com";
    private static final String SEARCH_MARK = "/*mark for changed*/";
    private String DEFAULT_FILTER;
    private String mCurrentUri = "";
    private String mFilters;
    private String mFilterSource;
    private Boolean mIsFilter = true;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private FloatingActionsMenu mFloating;
    private WeakReference<BottomSheet> mBottomSheet;

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

    private void handleLongClick() {
        WebView.HitTestResult result = mWebView.getHitTestResult();
        switch (result.getType()) {
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                showMenu(result.getExtra());
                break;
        }
    }

    private void loadUri() {
        mWebView.loadUrl(mCurrentUri);
    }

    // setting filters
    private void setFilter() {
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
                    filter = sortFilters(filter);
                    preferences.edit().putString(KEY_FILTER, filter).apply();
                    mFilters = filter;
                    getFilterSource();

                    dialog.dismiss();
                }))
                .show();

    }

    // setting event handlers
    private void setupControls(View view) {
        if (mFloating == null) mFloating = view.findViewById(R.id.floating_menu);
        view.findViewById(R.id.floating_refresh).setOnClickListener(e -> {
            mWebView.reload();
            mFloating.collapse();
        });
        view.findViewById(R.id.floating_menu_button).setOnClickListener(e -> {
            List<BottomSheet.Item> items = new ArrayList<>();

            BottomSheet.Item item5 = new BottomSheet.Item();

            item5.title = "设置屏蔽频道";
            item5.imageResId = R.drawable.ic_filter_list_black_24dp;
            items.add(item5);


            BottomSheet.Item item3 = new BottomSheet.Item();

            if (mIsFilter) {
                item3.title = "开启屏蔽";
                item3.imageResId = R.drawable.ic_lock_outline_black_24dp;
            } else {
                item3.title = "关闭屏蔽";
                item3.imageResId = R.drawable.ic_lock_open_black_24dp;
            }
            items.add(item3);

            BottomSheet.Item item1 = new BottomSheet.Item();

            item1.title = "复制";
            item1.imageResId = R.drawable.ic_content_copy_black_24dp;
            items.add(item1);

            BottomSheet.Item item2 = new BottomSheet.Item();

            item2.title = "分享";
            item2.imageResId = R.drawable.ic_share_black_24dp;
            items.add(item2);

            BottomSheet.Item item6 = new BottomSheet.Item();

            item6.title = "返回";
            item6.imageResId = R.drawable.ic_chevron_left_black_24dp;
            items.add(item6);

            BottomSheet bottomSheet = new BottomSheet(getActivity(), items);
            mBottomSheet = new WeakReference<BottomSheet>(bottomSheet);
            bottomSheet.show(getView().findViewById(R.id.relative_layout), this);
            mFloating.collapse();
        });
        view.findViewById(R.id.floating_go_back).setOnClickListener(e -> {

            if (mWebView.canGoBack()) mWebView.goBack();
            mFloating.collapse();
        });
        view.findViewById(R.id.floating_home).setOnClickListener(e -> {
            mCurrentUri = DEFAULT_URI;
            loadUri();
            mFloating.collapse();
        });


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
            public void onPageFinished(WebView view, String url) {
                if (mProgressBar.getVisibility() == View.VISIBLE) {


                    mProgressBar.setVisibility(View.GONE);
                }
                applyFilter();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(INITIAL_PROGRESS);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                mCurrentUri = url;
                mWebView.loadUrl(url);
                return true;
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

    private void toggleFilter() {
        mIsFilter = !mIsFilter;
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
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
            return stringBuilder.toString();
        }

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

        if (Utils.isNullOrBlank(mFilters)) {
            List<String> list = Utils.readLines(getActivity(), R.raw.filter);
            if (!Utils.isEmpty(list)) {
                mFilters = Utils.joining(list, "|");
            }
        }

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
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                setFilter();
                break;
            case 1:
                toggleFilter();
                if (mIsFilter)
                    Toast.makeText(getActivity(), "开启屏蔽频道功能", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getActivity(), "关闭屏蔽频道功能", Toast.LENGTH_LONG).show();

                break;
            case 2:
                Utils.copyText(getActivity(), mCurrentUri);
                Toast.makeText(getActivity(), "成功复制链接到剪切板", Toast.LENGTH_LONG).show();
                break;
            case 3:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mCurrentUri);
                getActivity().startActivity(Intent.createChooser(intent, ""));
                break;
            default:
                break;
        }
        if (mBottomSheet.get() != null) {
            mBottomSheet.get().dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(KEY_URI, mCurrentUri)
                .putBoolean(KEY_IS_FILTER, mIsFilter).apply();
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
