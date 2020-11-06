package blagodarie.rating.ui.wishes

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.WishFragmentBinding
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import blagodarie.rating.model.entities.Wish
import java.util.*

class WishFragment : Fragment() {

    companion object {
        private val TAG = WishFragment::class.java.simpleName
    }

    private lateinit var mViewModel: WishViewModel

    private lateinit var mBinding: WishFragmentBinding

    private lateinit var mWishId: UUID

    private var mMiEdit: MenuItem? = null

    private var mMiDelete: MenuItem? = null

    private val mAsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    private val mIsOwnObserver = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            updateMenuItemsVisibility()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        setHasOptionsMenu(true)
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
        Log.d(TAG, "initWishId")
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

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        mViewModel.isOwn.removeOnPropertyChangedCallback(mIsOwnObserver)
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater
    ) {
        Log.d(TAG, "onCreateOptionsMenu")
        super.onCreateOptionsMenu(menu, inflater)
        requireActivity().menuInflater.inflate(R.menu.wish_fragment, menu)
        mMiEdit = menu.findItem(R.id.miEdit)
        mMiDelete = menu.findItem(R.id.miDelete)
        updateMenuItemsVisibility()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.miEdit -> {
                val action = WishFragmentDirections.actionWishFragmentToEditWishFragment(mViewModel.wish.get() as Wish)
                NavHostFragment.findNavController(this).navigate(action)
            }
            R.id.miDelete -> {
                showDeleteWishConfirmDialog()
            }
            else -> {
                throw IllegalArgumentException("Unknown menu item")
            }
        }
        return super.onOptionsItemSelected(item)
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
        mViewModel.discardValues()
        mViewModel.isOwn.addOnPropertyChangedCallback(mIsOwnObserver)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            downloadWish()
        }
    }

    private fun updateMenuItemsVisibility() {
        Log.d(TAG, "updateMenuItemsVisibility")
        mMiEdit?.isVisible = mViewModel.isOwn.get()
        mMiDelete?.isVisible = mViewModel.isOwn.get()
    }

    private fun downloadWish() {
        Log.d(TAG, "downloadWish")
        mViewModel.downloadInProgress.set(true)
        mAsyncRepository.getWish(
                mWishId,
                {
                    mViewModel.wishLiveData.value = it
                    mViewModel.downloadInProgress.set(false)
                }
        ) {
            Log.e(TAG, Log.getStackTraceString(it))
            mViewModel.downloadInProgress.set(false)
        }
    }

    private fun showDeleteWishConfirmDialog() {
        Log.d(TAG, "showDeleteWishConfirmDialog")
        AlertDialog.Builder(requireContext()).setMessage(R.string.qstn_delete_wish).setPositiveButton(
                R.string.btn_delete
        ) { _: DialogInterface, _: Int ->
            deleteWish()
        }.setNegativeButton(
                R.string.btn_cancel,
                null).show()
    }

    private fun deleteWish() {
        Log.d(TAG, "deleteWish")
        AccountSource.getAccount(
                requireActivity(),
                true
        ) { account: Account? ->
            if (account != null) {
                deleteWish(account)
            }
        }
    }

    private fun deleteWish(
            account: Account
    ) {
        Log.d(TAG, "deleteWish account=$account")
        AccountProvider.getAuthToken(
                requireActivity(),
                account
        ) { authToken: String? ->
            if (authToken != null) {
                deleteWish(authToken)
            } else {
                Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteWish(
            authToken: String
    ) {
        Log.d(TAG, "deleteWish authToken=$authToken")
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.deleteWish(
                mWishId,
                {
                    Toast.makeText(requireContext(), R.string.info_msg_wish_deleted, Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                }
        ) { throwable: Throwable ->
            if (throwable is BadAuthorizationTokenException) {
                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                deleteWish()
            } else {
                Log.e(TAG, Log.getStackTraceString(throwable))
                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}