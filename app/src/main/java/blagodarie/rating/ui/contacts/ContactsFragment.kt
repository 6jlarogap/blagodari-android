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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import blagodarie.rating.AppExecutors
import blagodarie.rating.BuildConfig
import blagodarie.rating.R
import blagodarie.rating.databinding.ContactsFragmentBinding
import blagodarie.rating.model.IKeyPair
import blagodarie.rating.model.IProfile
import blagodarie.rating.repository.AsyncRepository
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.ui.people.PeopleAdapter
import com.google.android.material.snackbar.Snackbar
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

    val mUserActionListener = object: UserActionListener{
        override fun onSwipeRefresh() {
            mViewModel.downloadInProgress.set(true)
            attemptToReadContacts()
            mViewModel.downloadInProgress.set(false)
        }
    }

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


    private var mPeopleAdapter: PeopleAdapter? = null

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

        initOperationsAdapter()
        initViewModel()
        setupBinding()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        attemptToReadContacts()
    }

    private fun attemptToReadContacts(){
        if (mNeedContactsUpdate) {
            if (isReadContactsAllowed()) {
                readContacts()
            } else {
                requestPermissions()
            }
        }
    }

    private fun initOperationsAdapter() {
        mPeopleAdapter = PeopleAdapter {
            onProfileClick(it)
        }
    }

    private fun onProfileClick(userId: UUID) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(getString(R.string.url_profile, userId))
        startActivity(i)
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        val shouldProvideRationale: Boolean = shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
        if (shouldProvideRationale) {
            mPermissionDeniedExplanationShowed = false
            showSnackbar(android.R.string.ok) {
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
        mBinding.viewModel = mViewModel
        mBinding.rvPeople.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rvPeople.adapter = mPeopleAdapter
        mBinding.userActionListener = mUserActionListener
    }

    private fun isReadContactsAllowed(): Boolean {
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
                        mViewModel.keys = value
                        refreshPeople()
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
                    readContacts()
                } else {
                    if (!mPermissionDeniedExplanationShowed) {
                        showSnackbar(R.string.btn_settings) {
                            mPermissionDeniedExplanationShowed = false
                            showSettings()
                        }
                        mPermissionDeniedExplanationShowed = true
                    }
                }
            }
            else -> {
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
    }

    private fun showSnackbar(actionStringId: Int, listener: View.OnClickListener) {
        Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                getString(R.string.info_msg_need_read_contacts_permission),
                Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(actionStringId), listener).show()
    }

    private fun showSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun refreshPeople() {
        Log.d(TAG, "refreshPeople")
        mViewModel.people = mAsyncRepository.getLiveDataPagedListFromDataSource(ProfilesDataSource.ProfilesDataSourceFactory(mViewModel.keys))
        mViewModel.people?.observe(requireActivity(), Observer { pagedList: PagedList<IProfile?>? ->
            mPeopleAdapter?.submitList(pagedList)
        })
    }

}