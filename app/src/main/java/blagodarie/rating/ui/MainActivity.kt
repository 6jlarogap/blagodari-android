package blagodarie.rating.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import blagodarie.rating.BuildConfig
import blagodarie.rating.R
import blagodarie.rating.databinding.MainActivityBinding
import blagodarie.rating.update.UpdateManager

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.name
        fun startActivity(context: Context) = context.startActivity(Intent(context, MainActivity::class.java))
    }

    private lateinit var mBinding: MainActivityBinding
    private lateinit var mNavController: NavController

    override fun onCreate(
            savedInstanceState: Bundle?
    ) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        setupToolbar()
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(mBinding.bottomMenu, mNavController)
        UpdateManager.INSTANCE.checkUpdate(
                this,
                BuildConfig.VERSION_CODE,
                { mBinding.bottomMenu.menu.findItem(R.id.updateFragment).isVisible = true },
                { throwable: Throwable -> Toast.makeText(this, throwable.localizedMessage, Toast.LENGTH_LONG).show() }
        )
    }

    private fun setupToolbar() {
        Log.d(TAG, "setupToolbar")
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

}