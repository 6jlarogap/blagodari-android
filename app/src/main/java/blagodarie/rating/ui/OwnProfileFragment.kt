package blagodarie.rating.ui

import android.accounts.Account
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.OwnProfileFragmentBinding
import blagodarie.rating.model.IProfile
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.GetThanksUsersResponse.ThanksUser
import blagodarie.rating.ui.user.GridAutofitLayoutManager
import blagodarie.rating.ui.user.profile.ThanksUsersAdapter
import blagodarie.rating.ui.user.profile.ThanksUsersDataSource.ThanksUserDataSourceFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

class OwnProfileFragment : Fragment() {

    interface UserActionListener {
        fun onShareClick()
        fun onOperationsClick()
        fun onWishesClick()
        fun onAbilitiesClick()
        fun onKeysClick()
        fun onSocialGraphClick()
    }

    companion object {
        private val TAG: String = OwnProfileFragment::class.java.name
    }

    private lateinit var mViewModel: OwnProfileViewModel

    private lateinit var mBinding: OwnProfileFragmentBinding

    private lateinit var mThanksUsersAdapter: ThanksUsersAdapter

    private val mAsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    val userActionListener = object : UserActionListener {
        override fun onShareClick() {
            Log.d(TAG, "onShareClick")
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mViewModel.account.get()?.name.toString()))
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "Поделиться"))
        }

        override fun onOperationsClick() {
            Log.d(TAG, "onOperationsClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalOperationsFragment().setUserId(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onWishesClick() {
            Log.d(TAG, "onAbilitiesClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalWishesFragment(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onAbilitiesClick() {
            Log.d(TAG, "onAbilitiesClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalAbilitiesFragment(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onKeysClick() {
            Log.d(TAG, "onKeysClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalKeysFragment(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onSocialGraphClick() {
            Log.d(TAG, "onSocialGraphClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalGraphFragment().setUserId(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }
    }

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
        initThanksUserAdapter()
        setupBinding()
    }

    override fun onResume() {
        super.onResume()
        AccountSource.getAccount(
                requireActivity(),
                true
        ) {
            if (mViewModel.account.get() != it) {
                mViewModel.account.set(it)
                if (it != null) {
                    mViewModel.qrCode.set(createQrCodeBitmap(UUID.fromString(it.name)))
                }
                refreshProfileData()
            }
        }
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = OwnProfileFragmentBinding.inflate(inflater, container, false)
    }

    private fun initThanksUserAdapter() {
        Log.d(TAG, "initThanksUserAdapter")
        mThanksUsersAdapter = ThanksUsersAdapter { userId: UUID -> onThanksUserClick(userId) }
        mThanksUsersAdapter.submitList(mViewModel.thanksUsers?.value)
    }

    private fun onThanksUserClick(userId: UUID) {
        Log.d(TAG, "onThanksUserClick")
        NavHostFragment.findNavController(this).navigate(Uri.parse(getString(R.string.url_profile, userId)))
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(OwnProfileViewModel::class.java)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.userActionListener = userActionListener
        mBinding.srlRefreshProfileInfo.setOnRefreshListener { this.refreshProfileData() }
        mBinding.rvThanksUsers.layoutManager = GridAutofitLayoutManager(requireContext(), (resources.getDimension(R.dimen.thanks_user_photo_width) + resources.getDimension(R.dimen.thanks_user_photo_margin) * 2).toInt())
        mBinding.rvThanksUsers.adapter = mThanksUsersAdapter
        mBinding.viewModel = mViewModel
    }

    private fun createQrCodeBitmap(
            userId: UUID
    ): Bitmap {
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
                    getString(R.string.url_profile, userId.toString()),
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

    private fun refreshProfileData() {
        Log.d(TAG, "refreshProfileData")
        AccountSource.getAccount(
                requireActivity(),
                false
        ) { account: Account? ->
            mViewModel.account.set(account)
            if (account != null) {
                AccountProvider.getAuthToken(
                        requireActivity(),
                        account
                ) { authToken: String? -> this.downloadProfileData(UUID.fromString(account.name), authToken) }
            }
        }
    }

    private fun downloadProfileData(
            userId: UUID,
            authToken: String?
    ) {
        Log.d(TAG, "downloadProfileData")
        mViewModel.downloadInProgress.set(true)
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.getProfileInfo(
                userId,
                { profileInfo: IProfile? ->
                    mViewModel.downloadInProgress.set(false)
                    mViewModel.profileInfo.set(profileInfo)
                },
                { throwable: Throwable ->
                    mViewModel.downloadInProgress.set(false)
                    Toast.makeText(requireActivity(), throwable.message, Toast.LENGTH_LONG).show()
                })
        refreshThanksUsers(userId)
    }

    private fun refreshThanksUsers(
            userId: UUID
    ) {
        Log.d(TAG, "refreshThanksUsers")
        mViewModel.thanksUsers = mAsyncRepository.getLiveDataPagedListFromDataSource(ThanksUserDataSourceFactory(userId))
        mViewModel.thanksUsers?.observe(requireActivity(), androidx.lifecycle.Observer { pagedList: PagedList<ThanksUser?>? -> mThanksUsersAdapter.submitList(pagedList) })
    }
}