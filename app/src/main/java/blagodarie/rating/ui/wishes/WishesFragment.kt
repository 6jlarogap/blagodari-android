package blagodarie.rating.ui.wishes

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.WishesFragmentBinding
import blagodarie.rating.model.IWish
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import blagodarie.rating.ui.wishes.WishesFragmentArgs
import blagodarie.rating.ui.wishes.WishesFragmentDirections
import java.util.*

class WishesFragment : Fragment() {

    interface UserActionListener {
        fun onAddWishClick()
    }

    companion object {
        private val TAG = WishesFragment::class.java.simpleName
    }

    private lateinit var mViewModel: WishesViewModel

    private lateinit var mBinding: WishesFragmentBinding

    private lateinit var mWishesAdapter: WishesAdapter

    private lateinit var mUserId: UUID

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
        val args = WishesFragmentArgs.fromBundle(requireArguments())
        mUserId = args.userId
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        initViewModel()
        initOperationsAdapter()
        setupBinding()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        refreshOperations()
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            mViewModel.isOwn.set(it != null && it.name == mUserId.toString())
        }
    }

    private fun initOperationsAdapter() {
        Log.d(TAG, "initOperationsAdapter")
        mWishesAdapter = WishesAdapter(mViewModel.isOwn, object : WishesAdapter.AdapterCommunicator{
            override fun onDeleteClick(wish: IWish) {
                showDeleteWishConfirmDialog(wish)
            }
        })
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = WishesFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(WishesViewModel::class.java)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.list.recyclerView.adapter = mWishesAdapter
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            refreshOperations()
        }
        mBinding.userActionListener = object : UserActionListener {
            override fun onAddWishClick() {
                val action: NavDirections = WishesFragmentDirections.actionWishesFragmentToAddWishFragment()
                NavHostFragment.findNavController(this@WishesFragment).navigate(action)
            }
        }
    }

    private fun refreshOperations() {
        Log.d(TAG, "refreshOperations")
        mViewModel.downloadInProgress.set(true)
        mViewModel.wishes = mAsyncRepository.getLiveDataPagedListFromDataSource(WishesDataSource.WishesDataSourceFactory(mUserId))
        mViewModel.wishes?.observe(requireActivity()) { pagedList: PagedList<IWish?>? ->
            mViewModel.isEmpty.set(pagedList?.isEmpty() ?: true)
            mWishesAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        }
    }

    private fun showDeleteWishConfirmDialog(
            wish: IWish
    ) {
        AlertDialog.Builder(requireContext()).setMessage(R.string.qstn_delete_wish).setPositiveButton(
                R.string.btn_delete
        ) { dialogInterface: DialogInterface, i: Int ->
            attemptToDeleteWish(wish)
        }.setNegativeButton(
                R.string.btn_cancel,
                null).show()
    }

    private fun attemptToDeleteWish(
            wish: IWish
    ) {
        AccountSource.getAccount(
                requireActivity(),
                true
        ) { account: Account? ->
            if (account != null) {
                AccountProvider.getAuthToken(
                        requireActivity(),
                        account
                ) { authToken: String? ->
                    if (authToken != null) {
                        deleteWish(authToken, wish)
                    } else {
                        Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun deleteWish(
            authToken: String,
            wish: IWish
    ) {
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.deleteWish(
                wish.id,
                {
                    Toast.makeText(requireContext(), R.string.info_msg_wish_deleted, Toast.LENGTH_LONG).show()
                    refreshOperations()
                }
        ) { throwable: Throwable ->
            if (throwable is BadAuthorizationTokenException) {
                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                attemptToDeleteWish(wish)
            } else {
                Log.e(TAG, Log.getStackTraceString(throwable))
                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}