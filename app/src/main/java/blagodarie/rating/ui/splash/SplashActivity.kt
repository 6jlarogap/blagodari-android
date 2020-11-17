package blagodarie.rating.ui.splash

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import blagodarie.rating.ui.MainActivity

class SplashActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = SplashActivity::class.java.name
    }

    override fun onCreate(
            savedInstanceState: Bundle?
    ) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        MainActivity.startActivity(this)
    }
}