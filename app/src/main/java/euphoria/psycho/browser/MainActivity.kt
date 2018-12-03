package euphoria.psycho.browser

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class MainActivity : AppCompatActivity() {
// or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION                     or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    val applyFullScreen = {
        window.decorView.run {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

    }

    val initialize: () -> Unit = {
        applyFullScreen()
        setContentView(R.layout.activity_main)
        showBrowserFragment()
    }
    val showBrowserFragment = {
        val browserFragment = BrowserFragment()
        supportFragmentManager.beginTransaction().replace(R.id.container, browserFragment).commitAllowingStateLoss()
    }


    val checkPermissions = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_PERMISSIONS_CODE
            )
        } else {
            initialize()
        }

        window.decorView.setOnSystemUiVisibilityChangeListener {
            applyFullScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        initialize()
    }

    companion object {

        private const val REQUEST_PERMISSIONS_CODE = 1 shl 1
    }

}
