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
import blagodarie.rating.databinding.AbilitiesFragmentBinding
import blagodarie.rating.model.IAbility
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.ui.user.abilities.AbilitiesAdapter
import blagodarie.rating.ui.user.abilities.AbilitiesDataSource
import java.util.*

class AbilitiesFragment : Fragment() {

    interface UserActionListener {
        fun onAddAbilityClick()
    }

    companion object {
        private val TAG = AbilitiesFragment::class.java.simpleName
    }

    private lateinit var mViewModel: AbilitiesViewModel

    private lateinit var mBinding: AbilitiesFragmentBinding

    private lateinit var mAbilitiesAdapter: AbilitiesAdapter

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
        val args = AbilitiesFragmentArgs.fromBundle(requireArguments())
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
        refreshAbilities()
        AccountSource.getAccount(
                requireActivity(),
                false
        ) {
            mViewModel.isOwn.set(it != null && it.name == mUserId.toString())
        }
    }

    private fun initOperationsAdapter() {
        Log.d(TAG, "initOperationsAdapter")
        mAbilitiesAdapter = AbilitiesAdapter(mViewModel.isOwn)
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = AbilitiesFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(AbilitiesViewModel::class.java)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.list.recyclerView.adapter = mAbilitiesAdapter
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            refreshAbilities()
        }
        mBinding.userActionListener = object : UserActionListener {
            override fun onAddAbilityClick() {
                val action: NavDirections = AbilitiesFragmentDirections.actionAbilitiesFragmentToAddAbilityFragment()
                NavHostFragment.findNavController(this@AbilitiesFragment).navigate(action)
            }
        }
    }

    private fun refreshAbilities() {
        Log.d(TAG, "refreshAbilities")
        mViewModel.downloadInProgress.set(true)
        mViewModel.abilities = mAsyncRepository.getLiveDataPagedListFromDataSource(AbilitiesDataSource.AbilitiesDataSourceFactory(mUserId))
        mViewModel.abilities?.observe(requireActivity()) { pagedList: PagedList<IAbility?>? ->
            mViewModel.isEmpty.set(pagedList?.isEmpty() ?: true)
            mAbilitiesAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        }
    }
}