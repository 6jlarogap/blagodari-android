package blagodarie.rating.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.databinding.WishesFragmentBinding
import blagodarie.rating.model.IWish
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.ui.user.wishes.WishesDataSource
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
        refreshOwnOperations()
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            mViewModel.isOwn.set(it != null && it.name == mUserId.toString())
        }
    }

    private fun initOperationsAdapter() {
        Log.d(TAG, "initOperationsAdapter")
        mWishesAdapter = WishesAdapter(mViewModel.isOwn)
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
            refreshOwnOperations()
        }
        mBinding.userActionListener = object : UserActionListener {
            override fun onAddWishClick() {
                val action: NavDirections = WishesFragmentDirections.actionWishesFragmentToAddWishFragment()
                NavHostFragment.findNavController(this@WishesFragment).navigate(action)
            }
        }
    }

    private fun refreshOwnOperations() {
        Log.d(TAG, "refreshOperations")
        mViewModel.downloadInProgress.set(true)
        mViewModel.wishes = mAsyncRepository.getLiveDataPagedListFromDataSource(WishesDataSource.WishesDataSourceFactory(mUserId))
        mViewModel.wishes?.observe(requireActivity()) { pagedList: PagedList<IWish?>? ->
            mViewModel.isEmpty.set(pagedList?.isEmpty() ?: true)
            mWishesAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        }
    }
}