package blagodarie.rating.ui

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.OwnOperationsFragmentBinding
import blagodarie.rating.model.IDisplayOperation
import blagodarie.rating.model.entities.OperationType
import blagodarie.rating.operations.OperationToUserManager
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.user.operations.UserOperationsDataSource.UserOperationsDataSourceFactory
import java.util.*

class OwnOperationsFragment : Fragment() {

    companion object {
        private val TAG = OwnOperationsFragment::class.java.simpleName
    }

    private lateinit var mViewModel: OwnOperationsViewModel

    private lateinit var mBinding: OwnOperationsFragmentBinding

    private lateinit var mOwnOperationsAdapter: OwnOperationsAdapter

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
        val args = OwnOperationsFragmentArgs.fromBundle(requireArguments())
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
        refreshOwnOperations()
    }

    private fun initOperationsAdapter() {
        Log.d(TAG, "initOperationsAdapter")
        mOwnOperationsAdapter = OwnOperationsAdapter {
            attemptToAddThanks(it)
        }
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = OwnOperationsFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(OwnOperationsViewModel::class.java)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            refreshOwnOperations()
        }
        mBinding.list.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mBinding.list.recyclerView.adapter = mOwnOperationsAdapter
    }

    private fun refreshOwnOperations() {
        Log.d(TAG, "refreshOperations")
        mViewModel.downloadInProgress.set(true)
        mViewModel.operations = mAsyncRepository.getLiveDataPagedListFromDataSource(UserOperationsDataSourceFactory(mUserId))
        mViewModel.operations?.observe(requireActivity()) { pagedList: PagedList<IDisplayOperation?>? ->
            mViewModel.isEmpty.set(pagedList?.isEmpty() ?: true)
            mOwnOperationsAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        }
    }

    private fun attemptToAddThanks(
            userIdTo: UUID
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
                    OperationToUserManager().createOperationToUser(
                            requireActivity(),
                            UUID.fromString(account.name),
                            userIdTo,
                            OperationType.THANKS,
                            mAsyncRepository,
                            {
                                Toast.makeText(requireContext(), R.string.info_msg_saved, Toast.LENGTH_LONG).show()
                                refreshOwnOperations()
                            },
                            { throwable: Throwable? ->
                                Log.e(TAG, Log.getStackTraceString(throwable))
                                if (throwable is BadAuthorizationTokenException) {
                                    AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                                    attemptToAddThanks(userIdTo)
                                } else {
                                    Toast.makeText(requireContext(), R.string.err_msg_not_saved, Toast.LENGTH_LONG).show()
                                }
                            })
                }
            }
        }
    }
}