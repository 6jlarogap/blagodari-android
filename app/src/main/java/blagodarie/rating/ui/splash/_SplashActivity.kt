package blagodarie.rating.ui.splash

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import blagodarie.rating.ui.MainActivity

class _SplashActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = _SplashActivity::class.java.name
    }

    override fun onCreate(
            savedInstanceState: Bundle?
    ) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        MainActivity.startActivity(this)
    }
}