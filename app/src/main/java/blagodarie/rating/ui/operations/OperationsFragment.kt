package blagodarie.rating.ui.operations

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.commands.CreateOperationToAnyTextCommand
import blagodarie.rating.commands.CreateOperationToUserCommand
import blagodarie.rating.databinding.OperationsFragmentBinding
import blagodarie.rating.model.IDisplayOperation
import blagodarie.rating.model.entities.OperationType
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import java.util.*

class OperationsFragment : Fragment() {

    interface UserActionListener {
        fun onThanksClick()
    }

    companion object {
        private val TAG = OperationsFragment::class.java.simpleName
    }

    private lateinit var mViewModel: OperationsViewModel

    private lateinit var mBinding: OperationsFragmentBinding

    private lateinit var mOperationsAdapter: OperationsAdapter

    private var mUserId: UUID? = null

    private var mAnyTextId: UUID? = null

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
        val args = OperationsFragmentArgs.fromBundle(requireArguments())
        mUserId = args.userId
        mAnyTextId = args.anyTextId
        if (mUserId == null && mAnyTextId == null) {
            throw IllegalArgumentException("No userId and no anyTextId")
        }
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
        mOperationsAdapter = OperationsAdapter(mViewModel.isOwn) {
            attemptToAddThanks(it, null)
        }
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = OperationsFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(OperationsViewModel::class.java)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.list.recyclerView.adapter = mOperationsAdapter
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            refreshOperations()
        }
        mBinding.userActionListener = object : UserActionListener {
            override fun onThanksClick() {
                attemptToAddThanks(mUserId, mAnyTextId)
            }
        }
    }

    private fun refreshOperations() {
        Log.d(TAG, "refreshOperations")
        mViewModel.downloadInProgress.set(true)
        if (mUserId != null) {
            mViewModel.operations = mAsyncRepository.getLiveDataPagedListFromDataSource(UserOperationsDataSource.UserOperationsDataSourceFactory(mUserId!!))
        } else if (mAnyTextId != null) {
            mViewModel.operations = mAsyncRepository.getLiveDataPagedListFromDataSource(AnyTextOperationsDataSource.AnyTextOperationsDataSourceFactory(mAnyTextId!!))
        }
        mViewModel.operations?.observe(requireActivity()) { pagedList: PagedList<IDisplayOperation>? ->
            mViewModel.isEmpty.set(pagedList?.isEmpty() ?: true)
            mOperationsAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        }
    }

    private fun attemptToAddThanks(
            userIdTo: UUID?,
            anyTextIdTo: UUID?
    ) {
        Log.d(TAG, "onAddOperation")

        AccountSource.getAccount(
                requireActivity(),
                true
        ) { account: Account? ->
            if (account != null) {
                AccountProvider.getAuthToken(
                        requireActivity(),
                        account
                ) { authToken: String? ->
                    mAsyncRepository.setAuthToken(authToken)
                    if (userIdTo != null) {
                        CreateOperationToUserCommand(
                                requireActivity(),
                                UUID.fromString(account.name),
                                userIdTo,
                                OperationType.THANKS,
                                mAsyncRepository,
                                {
                                    Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show()
                                    refreshOperations()
                                },
                                { throwable: Throwable? ->
                                    Log.e(TAG, Log.getStackTraceString(throwable))
                                    if (throwable is BadAuthorizationTokenException) {
                                        AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                                        attemptToAddThanks(userIdTo, null)
                                    } else {
                                        Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show()
                                    }
                                }
                        ).execute()
                    } else if (anyTextIdTo != null) {
                        CreateOperationToAnyTextCommand(
                                requireActivity(),
                                UUID.fromString(account.name),
                                anyTextIdTo,
                                OperationType.THANKS,
                                "",
                                mAsyncRepository,
                                {
                                    Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show()
                                    refreshOperations()
                                },
                                { throwable: Throwable? ->
                                    Log.e(TAG, Log.getStackTraceString(throwable))
                                    if (throwable is BadAuthorizationTokenException) {
                                        AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                                        attemptToAddThanks(null, anyTextIdTo)
                                    } else {
                                        Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show()
                                    }
                                }).execute()
                    }
                }
            }
        }
    }
}