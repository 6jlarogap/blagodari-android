package blagodarie.rating.ui.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import blagodarie.rating.AppExecutors
import blagodarie.rating.BuildConfig
import blagodarie.rating.R
import blagodarie.rating.databinding.ContactsFragmentBinding
import blagodarie.rating.model.IKeyPair
import blagodarie.rating.model.IProfile
import blagodarie.rating.repository.AsyncRepository
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.ui.people.PeopleAdapter
import java.util.*
import java.util.concurrent.Executors

class ContactsFragment : Fragment() {

    interface UserActionListener {
        fun onSwipeRefresh()
    }

    companion object {
        private const val READ_CONTACT_PERMISSION_REQUEST_CODE = 1
        private val TAG: String = ContactsFragment::class.java.name
    }

    val mUserActionListener = object : UserActionListener {
        override fun onSwipeRefresh() {
            mViewModel.downloadInProgress.set(true)
            attemptToReadContacts()
            mViewModel.downloadInProgress.set(false)
        }
    }

    private var mMiSearch: MenuItem? = null
    private lateinit var mBinding: ContactsFragmentBinding
    private lateinit var mViewModel: ContactsViewModel
    private var mNeedContactsUpdate: Boolean = true
    private var mPermissionDeniedExplanationShowed = false
    private var mContactsChangeObserverRegistered = false
    private val mContactsChangeObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            mNeedContactsUpdate = true
        }
    }

    private val mContactsRepository = ContactsRepository()
    private val mAsyncRepository: AsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    private var mSearchView: SearchView? = null
    private val mPeopleAdapter = PeopleAdapter {
        onProfileClick(it)
    }
    private val mOnQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            mViewModel.textFilter.value = newText
            return false
        }
    }

    private val mTextFilterObserver = Observer { o: String -> refreshPeople(o, mViewModel.keysFilter.value) }
    private val mKeysFilterObserver = Observer { o: List<IKeyPair> ->
        refreshPeople(mViewModel.textFilter.value, o)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        setHasOptionsMenu(true)
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
        attemptToReadContacts()
        mSearchView?.setOnQueryTextListener(mOnQueryTextListener)
        mSearchView?.setQuery(mViewModel.textFilter.value, false)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        mSearchView?.setOnQueryTextListener(null)
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        requireActivity().contentResolver.unregisterContentObserver(mContactsChangeObserver)
        mViewModel.textFilter.removeObserver(mTextFilterObserver)
        mViewModel.keysFilter.removeObserver(mKeysFilterObserver)
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater
    ) {
        Log.d(TAG, "onCreateOptionsMenu")
        super.onCreateOptionsMenu(menu, inflater)
        requireActivity().menuInflater.inflate(R.menu.contacts_fragment, menu)
        mMiSearch = menu.findItem(R.id.miSearch)
        if (mViewModel.textFilter.value != null &&
                mViewModel.textFilter.value!!.isNotEmpty()) {
            mMiSearch?.expandActionView()
        }
        mSearchView = mMiSearch?.actionView as SearchView
        mSearchView?.setOnQueryTextListener(mOnQueryTextListener)
        mSearchView?.setQuery(mViewModel.textFilter.value, false)
    }

    private fun attemptToReadContacts() {
        Log.d(TAG, "attemptToReadContacts")
        if (mNeedContactsUpdate) {
            if (isReadContactsAllowed()) {
                mViewModel.isShowExplanation.set(false)
                readContacts()
            } else {
                requestPermissions()
            }
        }
    }

    private fun onProfileClick(userId: UUID) {
        Log.d(TAG, "onProfileClick")
        val action = ContactsFragmentDirections.actionContactsFragmentToProfileFragment(userId.toString())
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        val shouldProvideRationale: Boolean = shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
        if (shouldProvideRationale) {
            mPermissionDeniedExplanationShowed = false
            showExplanation(android.R.string.ok) {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                        READ_CONTACT_PERMISSION_REQUEST_CODE)
            }
        } else {
            if (!mPermissionDeniedExplanationShowed) {
                requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        READ_CONTACT_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.viewModel = mViewModel
        mBinding.list.recyclerView.adapter = mPeopleAdapter
        mBinding.userActionListener = mUserActionListener
        mBinding.refreshListener = SwipeRefreshLayout.OnRefreshListener {
            refreshPeople(mViewModel.textFilter.value, mViewModel.keysFilter.value)
        }
    }

    private fun isReadContactsAllowed(): Boolean {
        Log.d(TAG, "isReadContactsAllowed")
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun readContacts() {
        Log.d(TAG, "readContacts")
        if (!mContactsChangeObserverRegistered) {
            requireActivity().contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, false, mContactsChangeObserver)
        }
        mContactsRepository.getKeys(
                Executors.newSingleThreadExecutor(),
                AppExecutors.getInstance().mainThread(),
                requireActivity().contentResolver,
                object : IContactsRepository.OnLoadListener {
                    override fun onLoad(value: List<IKeyPair>) {
                        mViewModel.keysFilter.value = value
                        //refreshPeople()
                    }
                },
                object : IContactsRepository.OnErrorListener {
                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, Log.getStackTraceString(throwable))
                    }
                }

        )
        mNeedContactsUpdate = false
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult")
        when (requestCode) {
            READ_CONTACT_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mViewModel.isShowExplanation.set(false)
                    readContacts()
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
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = ContactsFragmentBinding.inflate(inflater, container, false)
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)
        mViewModel.textFilter.observe(requireActivity(), mTextFilterObserver)
        mViewModel.keysFilter.observe(requireActivity(), mKeysFilterObserver)
    }

    private fun showExplanation(actionStringId: Int, listener: View.OnClickListener) {
        Log.d(TAG, "showExplanation")
        mViewModel.isShowExplanation.set(true)
        mBinding.btnRequirePermission.setOnClickListener(listener)
        mBinding.btnRequirePermission.setText(actionStringId)
    }

    private fun showSettings() {
        Log.d(TAG, "showSettings")
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun refreshPeople(
            textFilter: String?,
            keysFilter: List<IKeyPair>?
    ) {
        Log.d(TAG, "refreshPeople")
        mViewModel.downloadInProgress.set(true)
        mViewModel.people = mAsyncRepository.getLiveDataPagedListFromDataSource(ProfilesDataSource.ProfilesDataSourceFactory(textFilter, keysFilter))
        mViewModel.people.observe(requireActivity(), { pagedList: PagedList<IProfile> ->
            mViewModel.isEmpty.set(pagedList.isEmpty())
            mPeopleAdapter.submitList(pagedList)
            mViewModel.downloadInProgress.set(false)
        })
    }

}