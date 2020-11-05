package blagodarie.rating.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import blagodarie.rating.BuildConfig
import blagodarie.rating.R
import blagodarie.rating.databinding.QrScanFragmentBinding
import com.google.zxing.integration.android.IntentIntegrator
import java.util.*

class QrScanFragment : Fragment() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 102
        private val TAG: String = QrScanFragment::class.java.name
    }

    private var mPermissionDeniedExplanationShowed = false
    private lateinit var mBinding: QrScanFragmentBinding
    private lateinit var mViewModel: QrScanViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        initBinding(inflater, container)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        initViewModel()
        setupBinding()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        attemptToScanQrCode()
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = QrScanFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(QrScanViewModel::class.java)
    }

    private fun setupBinding() {
        mBinding.viewModel = mViewModel
    }

    private fun attemptToScanQrCode() {
        if (isCameraAllowed()) {
            scanQrCode()
        } else {
            requestPermissions()
        }
    }

    private fun scanQrCode() {
        IntentIntegrator.forSupportFragment(this).setPrompt(getString(R.string.rqst_scan_qr_code)).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES).setOrientationLocked(true).initiateScan()
    }

    private fun isCameraAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        val shouldProvideRationale: Boolean = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
        if (shouldProvideRationale) {
            mPermissionDeniedExplanationShowed = false
            showExplanation(android.R.string.ok) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE)
            }
        } else {
            if (!mPermissionDeniedExplanationShowed) {
                requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult")
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanQrCode()
                } else {
                    if (!mPermissionDeniedExplanationShowed) {
                        showExplanation(R.string.btn_settings) {
                            mPermissionDeniedExplanationShowed = false
                            showSettings()
                        }
                        mPermissionDeniedExplanationShowed = true
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun showExplanation(actionStringId: Int, listener: View.OnClickListener) {
        mViewModel.isShowExplanation.set(true)
        mBinding.btnRequirePermission.setOnClickListener(listener)
        mBinding.btnRequirePermission.setText(actionStringId)
    }

    private fun showSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    IntentIntegrator.REQUEST_CODE -> {
                        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                        if (result != null) {
                            val uri = Uri.parse(result.contents)
                            if (uri.host != null &&
                                    uri.host == getString(R.string.host) &&
                                    uri.path != null &&
                                    (((uri.path == "/profile" || uri.path == "/wish") && uri.getQueryParameter("id") != null))) {
                                val idString = uri.getQueryParameter("id")
                                try {
                                    UUID.fromString(idString)
                                    if (uri.path == "/profile") {
                                        NavHostFragment.findNavController(this).navigate(uri)
                                    } else if (uri.path == "/wish") {
                                        NavHostFragment.findNavController(this).navigate(uri)
                                    }
                                } catch (e: IllegalArgumentException) {
                                    val action = QrScanFragmentDirections.actionQrScanFragment2ToAnyTextFragment2(result.contents)
                                    NavHostFragment.findNavController(this).navigate(action)
                                }
                            } else {
                                val action = QrScanFragmentDirections.actionQrScanFragment2ToAnyTextFragment2(result.contents)
                                NavHostFragment.findNavController(this).navigate(action)
                            }
                        }
                    }
                }
            }
            else -> {
                NavHostFragment.findNavController(this).popBackStack()
            }
        }
    }

}