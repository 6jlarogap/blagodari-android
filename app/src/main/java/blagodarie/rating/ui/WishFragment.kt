package blagodarie.rating.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.WishFragmentBinding
import blagodarie.rating.model.entities.Wish
import blagodarie.rating.repository.AsyncServerRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

class WishFragment : Fragment() {

    companion object {
        private val TAG = WishFragment::class.java.simpleName
    }

    private lateinit var mViewModel: WishViewModel

    private lateinit var mBinding: WishFragmentBinding

    private lateinit var mWishId: UUID

    private val mAsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        initBinding(inflater, container)
        return mBinding.root
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        initWishId()
    }

    private fun initWishId() {
        val args = WishFragmentArgs.fromBundle(requireArguments())
        mWishId = UUID.fromString(args.wishUuid)
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
        downloadWish()
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            mViewModel.account.value = it
        }
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = WishFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(WishViewModel::class.java)
        mViewModel.qrCode.set(createQrCodeBitmap())
        mViewModel.account.observe(requireActivity()) {
            mViewModel.isOwn.set(it != null && it.name == mViewModel.wish.get()?.ownerUuid.toString())
        }
        mViewModel.wish.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                mViewModel.isOwn.set(mViewModel.account.value != null && mViewModel.account.value!!.name == mViewModel.wish.get()?.ownerUuid.toString())
            }
        })
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            downloadWish()
        }
        mBinding.btnEdit.setOnClickListener{
            val action = WishFragmentDirections.actionWishFragmentToEditWishFragment(mViewModel.wish.get() as Wish)
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    private fun createQrCodeBitmap(): Bitmap {
        Log.d(TAG, "createQrCodeBitmap")
        val width = 500
        val height = 500
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val writer = QRCodeWriter()
        val hints: MutableMap<EncodeHintType, Any?> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 0 // default = 4
        try {
            val bitMatrix = writer.encode(
                    getString(R.string.url_wish, mWishId),
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            )
            for (x in 0 until width) {
                for (y in 0 until height) {
                    result.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.TRANSPARENT)
                }
            }
        } catch (e: WriterException) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
        return result
    }

    private fun downloadWish() {
        Log.d(TAG, "downloadWish")
        mViewModel.downloadInProgress.set(true)
        mAsyncRepository.getWish(
                mWishId,
                {
                    mViewModel.wish.set(it)
                    mViewModel.downloadInProgress.set(false)
                }
        ) {
            Log.e(TAG, Log.getStackTraceString(it))
            mViewModel.downloadInProgress.set(false)
        }
    }
}