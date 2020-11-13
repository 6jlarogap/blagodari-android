package blagodarie.rating.ui.keys

import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.KeysFragmentBinding
import blagodarie.rating.model.IKey
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import java.util.*

class KeysFragment : Fragment() {

    interface UserActionListener {
        fun onAddKeyClick()
    }

    companion object {
        private val TAG = KeysFragment::class.java.simpleName
    }

    private lateinit var mViewModel: KeysViewModel

    private lateinit var mBinding: KeysFragmentBinding

    private lateinit var mKeysAdapter: KeysAdapter

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
        val args = KeysFragmentArgs.fromBundle(requireArguments())
        mUserId = args.userId
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        initViewModel()
        initKeysAdapter()
        setupBinding()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        refreshKeys()
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            mViewModel.isOwn.set(it != null && it.name == mUserId.toString())
        }
    }

    private fun initKeysAdapter() {
        Log.d(TAG, "initOperationsAdapter")
        mKeysAdapter = KeysAdapter(mViewModel.isOwn, object : KeysAdapter.AdapterCommunicator {
            override fun onEditKey(key: IKey) {
                attemptToEditKey(key)
            }

            override fun onDeleteKey(key: IKey) {
                attemptToDeleteKey(key)
            }
        })
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = KeysFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(KeysViewModel::class.java)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.list.recyclerView.adapter = mKeysAdapter
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            refreshKeys()
        }
        mBinding.userActionListener = object : UserActionListener {
            override fun onAddKeyClick() {
                val action = KeysFragmentDirections.actionKeysFragmentToAddKeyFragment()
                NavHostFragment.findNavController(this@KeysFragment).navigate(action)
            }
        }
    }

    private fun refreshKeys() {
        Log.d(TAG, "refreshKeys")
        mViewModel.downloadInProgress.set(true)
        mViewModel.keys = mAsyncRepository.getLiveDataPagedListFromDataSource(KeysDataSource.KeysDataSourceFactory(mUserId))
        mViewModel.keys?.observe(requireActivity()) { pagedList: PagedList<IKey?>? ->
            mViewModel.isEmpty.set(pagedList?.isEmpty() ?: true)
            mKeysAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        }
    }

    private fun attemptToEditKey(
            key: IKey
    ) {
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            if (it != null) {
                AccountProvider.getAuthToken(requireActivity(), it) { authToken: String? -> editKey(authToken, key) }
            }
        }
    }

    private fun attemptToDeleteKey(
            key: IKey
    ) {
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            if (it != null) {
                AccountProvider.getAuthToken(requireActivity(), it) { authToken: String? -> deleteKey(authToken, key) }
            }
        }
    }

    private fun editKey(
            authToken: String?,
            key: IKey
    ) {
        if (authToken != null) {
            mAsyncRepository.setAuthToken(authToken)
            mAsyncRepository.updateKey(
                    key,
                    {
                        Toast.makeText(requireContext(), R.string.info_msg_key_saved, Toast.LENGTH_LONG).show()
                        refreshKeys()
                    }
            ) { throwable: Throwable ->
                if (throwable is BadAuthorizationTokenException) {
                    AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                    attemptToEditKey(key)
                } else {
                    Log.e(TAG, Log.getStackTraceString(throwable))
                    Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteKey(
            authToken: String?,
            key: IKey
    ) {
        if (authToken != null) {
            mAsyncRepository.setAuthToken(authToken)
            mAsyncRepository.deleteKey(
                    key,
                    {
                        Toast.makeText(requireContext(), R.string.info_msg_key_deleted, Toast.LENGTH_LONG).show()
                        refreshKeys()
                    }
            ) { throwable: Throwable ->
                if (throwable is BadAuthorizationTokenException) {
                    AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                    attemptToEditKey(key)
                } else {
                    Log.e(TAG, Log.getStackTraceString(throwable))
                    Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
        }
    }
}