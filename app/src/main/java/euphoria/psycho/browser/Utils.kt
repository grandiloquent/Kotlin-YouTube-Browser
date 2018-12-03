package euphoria.psycho.browser

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Toast


fun Context.sharedPreferences() = PreferenceManager.getDefaultSharedPreferences(this)
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
fun SharedPreferences.string(key: String, defaultValue: String = "") = getString(key, defaultValue)