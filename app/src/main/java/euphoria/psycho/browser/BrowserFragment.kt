package euphoria.psycho.browser

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.webkit.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import java.io.File
import java.nio.charset.Charset

class BrowserFragment : Fragment() {
    private var mCurrentUri = ""
    private var mFilters = ""
    private var mFilterSource = ""
    private var mIsFilter = true
    private var mIsFullScreen = false

    val applyFitler = {
        if (mIsFilter && mCurrentUri.contains("m.youtube.com")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                web_view?.evaluateJavascript(
                    mFilterSource
                ) {
                    requireContext().toast(it)
                }
            }
        }
    }
    val applySetFilterRule = {
        val editText = EditText(requireContext()).apply {
            setText(requireContext().sharedPreferences().string(KEY_FILTER))
        }
        AlertDialog.Builder(requireContext())
            .setView(editText)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    val setupSwipeLayout = {
        with(swipe_refresh) {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener {
                reload()
            }
        }
        swipe_refresh.isRefreshing = false
    }
    val setUriTextBox = {
        text_url.run {
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {

                }
            }
        }
    }
    val setupButtonGo = {
        image_go.setImageResource(R.drawable.ic_btn_reload)
    }
    val setupWebView = {
        web_view.let {
            it.clearCache(false)
            it.settings.run {
                val cachePath = File(Environment.getExternalStorageDirectory(), ".cache_browser")
                if (!cachePath.isDirectory) cachePath.mkdir()
                setAppCacheMaxSize(1024 * 1024 * 8)
                cacheMode = WebSettings.LOAD_DEFAULT
                domStorageEnabled = true
                databaseEnabled = true
                setAppCachePath(cachePath.absolutePath)
                javaScriptEnabled = true
                allowFileAccess = true
                setAppCacheEnabled(true)

            }
            it.isLongClickable = true
            it.setOnLongClickListener(linkHandler())
            it.webViewClient = getWebViewClient()

        }
    }
    val getFilterSource = {
        val inputStream = resources.openRawResource(R.raw.block_channel)
        val reader = inputStream.reader(Charset.forName("UTF-8"))
        if (mFilters.isNotBlank()) {
            val lines = reader.readLines()
            val stringBuilder = StringBuilder()
            for (i in lines.indices) {
                if (lines[i].trimStart().startsWith(SEARCH_MARK)) {
                    stringBuilder.append("$SEARCH_MARK\"$mFilters\";").append('\n')
                } else {
                    stringBuilder.append(lines[i]).append('\n')
                }
            }
            mFilterSource = stringBuilder.toString()
        } else {
            mFilterSource = reader.readText()
        }
        inputStream.close()
    }
    val getWebChromeClient: () -> WebChromeClient = {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.progress = newProgress
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
//                Log.e(TAG, "[onShowCustomView]")
//                mIsFullScreen = true
//                view?.let {
//                    browser_container.visibility = View.INVISIBLE
//                    val params = FrameLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
//                    )
//                } ?: run {
//                    appbar.setExpanded(false, true)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        web_view.isNestedScrollingEnabled = false
//                    }
//                }
//                switchToImmersiveMode()
            }
        }
    }
    val getWebViewClient: () -> WebViewClient = {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                favicon?.let {
                    text_url.setCompoundDrawables(BitmapDrawable(it), null, null, null)

                }
                progress.run {
                    progress = INITIAL_PROGRESS
                    visibility = View.VISIBLE
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {

                super.onLoadResource(view, url)
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                text_url.setText(mCurrentUri)
                if (progress.visibility == View.VISIBLE) {
                    progress.visibility = View.GONE
                }
                swipe_refresh.isRefreshing = false
                applyFitler()

            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    web_view.webChromeClient = getWebChromeClient()
                    view.loadUrl(request.url.toString())
                    return true
                } else return false
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                requireContext().toast(error.toString())
            }
        }
    }
    val linkHandler: () -> View.OnLongClickListener = {
        View.OnLongClickListener {
            val hitTestResult = web_view.hitTestResult
            when (hitTestResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    onLongPress(hitTestResult.extra)
                }
            }
            false
        }
    }
    val loadUri = {
        web_view.loadUrl(mCurrentUri)
    }
    val onLongPress: (String?) -> Unit = {
    }
    val reload = {
        web_view.reload()
    }
    val switchToImmersiveMode = {
        requireActivity().window.run {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val sharedPreferences = requireContext().sharedPreferences()
        mCurrentUri = sharedPreferences.string(KEY_URI, "https://m.youtube.com")
        mFilters = sharedPreferences.string(KEY_FILTER, DEFAULT_FILTER);
        getFilterSource()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_browser, container, false)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                applyFitler()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupButtonGo()
        setupSwipeLayout()
        setupWebView()
        loadUri()
    }

    override fun onResume() {
        super.onResume()
        view?.run {
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { v, keyCode, event ->
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (web_view.canGoBack()) {
                        web_view.goBack()

                        true;
                    } else {
                        false
                    }
                }
                false;
            }
        }
    }

    companion object {
        private const val TAG = "/BrowserFragment"
        private const val KEY_URI = "uri"
        private const val KEY_FILTER = "filter"
        private const val INITIAL_PROGRESS = 5
        private const val SEARCH_MARK = "/*mark for changed*/"
        private const val DEFAULT_FILTER =
            "54新觀點|从台湾看见世界|寰宇新聞 頻道|美国之音中文网|夢想街之全能事務所|民視新聞|明鏡火拍|年代向錢看|三立iNEWS|三立LIVE新聞|台視新聞 TTV NEWS|头条軍事【军事头条 軍情諜報 軍事解密 每日更新】歡迎訂閱|香港全城討論區|新聞面對面|新聞追追追"

    }

}