package org.blagodari.ui.newcontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.diagnosticlib.Diagnostic;

import org.blagodari.R;
import org.blagodari.databinding.NewContactsFragmentBinding;
import org.blagodari.db.addent.ContactWithKeyz;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NewContactsFragment
        extends Fragment
        implements SearchView.OnQueryTextListener {
    private NewContactsViewModel mContactsViewModel;
    private int mScrollYPosition = 0;
    private NewContactsFragmentBinding mContactsFragmentBinding;
    /**
     * Идентификатор запроса на разрешение чтения контактов.
     */
    static final int PERMISSION_REQUEST_READ_CONTACTS = 1;

    /**
     * Слушатель изменения контактов. Когда контакты претерпели изменения, утанавливает флаг
     * необходимости синхронизации контактов.
     */
    private static final ContentObserver mContactsChangeObserver = new ContentObserver(null) {
        @Override
        public void onChange (boolean selfChange) {
            super.onChange(selfChange);
            Diagnostic.i();
            mNeedSyncContacts = true;
        }
    };

    /**
     * Флаг необходимости синхронизации контактов.
     */
    private static boolean mNeedSyncContacts = true;

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostic.i();
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Diagnostic.i();
        this.mContactsFragmentBinding = NewContactsFragmentBinding.inflate(inflater, container, false);
        return this.mContactsFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Diagnostic.i();
        initViewModel();
        this.mContactsFragmentBinding.setViewModel(mContactsViewModel);
        setupContactsAdapter();
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Diagnostic.i();
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            requireActivity().getContentResolver().
                    registerContentObserver(ContactsContract.Data.CONTENT_URI,
                            false,
                            mContactsChangeObserver);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshContacts();
            }
        }
    }

    @Override
    public void onStart () {
        super.onStart();
        Diagnostic.i();
        checkPermissionReadContacts();
    }

    private void checkPermissionReadContacts () {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Diagnostic.i("Request permission");
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
        } else {
            Diagnostic.i("Permission granted");
            requireActivity().getContentResolver().
                    registerContentObserver(ContactsContract.Data.CONTENT_URI,
                            false,
                            mContactsChangeObserver);
            refreshContacts();
        }
    }

    private void refreshContacts () {
        if (mNeedSyncContacts) {
            Collection<ContactWithKeyz> contactWithKeyzList = mContactsViewModel.getContacts().getValue();
            if (contactWithKeyzList != null) {
                contactWithKeyzList.clear();
            }
            mContactsViewModel.getContacts().setValue(contactWithKeyzList);

            readContacts();
            mNeedSyncContacts = false;
        }
    }

    private void readContacts () {
        ContactProvider contactProvider = new ContactProvider(requireActivity().getContentResolver());
        contactProvider.setProgressListener(mContactsViewModel.contactProgressListener);
        Observable.fromPublisher(contactProvider).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(contactWithKeyz -> startGetContactSumInfo(contactWithKeyz));
    }

    ContactSumInfoLoader contactSumInfoLoader = new ContactSumInfoLoader(2L);

    private void startGetContactSumInfo (@NonNull final ContactWithKeyz contactWithKeyz) {
        mContactsViewModel.mGetInfoContactsCount.set(mContactsViewModel.mGetInfoContactsCount.get() + 1);
        Observable.fromCallable(() ->
                contactSumInfoLoader.call(contactWithKeyz, mContactsViewModel.mSyncDataInProgress, mContactsViewModel.mSyncDataApiClass)).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(newContactWithKeyz -> {
                    mContactsViewModel.mGetInfoContactsCount.set(mContactsViewModel.mGetInfoContactsCount.get() - 1);
                    Collection<ContactWithKeyz> contactWithKeyzList = mContactsViewModel.getContacts().getValue();
                    /*if (contactWithKeyzList == null) {
                        Comparator<ContactWithKeyz> contactWithKeyzComparator = new Comparator<ContactWithKeyz>() {
                            @Override
                            public int compare (ContactWithKeyz o1, ContactWithKeyz o2) {
                                return o1.getContact().getFame().compareTo(o2.getContact().getFame());
                            }
                        };
                        contactWithKeyzList = new TreeSet<>(contactWithKeyzComparator);
                    }*/
                    contactWithKeyzList.add(newContactWithKeyz);
                    mContactsViewModel.getContacts().setValue(contactWithKeyzList);
                    // mContactsViewModel.mSyncDataInProgress.set(false);
                });
    }

    private void initViewModel () {
        //создаем фабрику
        final NewContactsViewModel.Factory factory = new NewContactsViewModel.Factory(
        );

        //создаем ContactsViewModel
        this.mContactsViewModel = new ViewModelProvider(requireActivity(), factory).get(NewContactsViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu (@NonNull Menu menu, MenuInflater inflater) {
        Diagnostic.i();
        inflater.inflate(R.menu.fragment_contacts, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.miFilter);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        //searchView.setQuery(mContactsViewModel.getFilter(), false);
    }

    private void setupContactsAdapter () {
        Diagnostic.i();
        NewContactsAdapter contactsAdapter = new NewContactsAdapter();
        this.mContactsFragmentBinding.rvContacts.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.horizontal_portrets_count)));
        this.mContactsFragmentBinding.rvContacts.setAdapter(contactsAdapter);
        this.mContactsFragmentBinding.rvContacts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged (@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled (@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollYPosition = recyclerView.computeVerticalScrollOffset();
            }
        });
        this.mContactsViewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> filter());
        this.mContactsViewModel.getFilteredContacts().observe(getViewLifecycleOwner(), contactsAdapter::setData);
        mContactsViewModel.getFilter().observe(getViewLifecycleOwner(), filter -> filter());
    }

    private void filter () {
        final Collection<ContactWithKeyz> filteredContacts = new ArrayList<>();
        if (mContactsViewModel.getContacts().getValue() != null && mContactsViewModel.getFilter().getValue() != null) {
            for (ContactWithKeyz contactWithKeyz : mContactsViewModel.getContacts().getValue()) {
                if (mContactsViewModel.getFilter().getValue().equals("") || contactWithKeyz.getContact().getTitle().toLowerCase().contains(mContactsViewModel.getFilter().getValue().toLowerCase())) {
                    filteredContacts.add(contactWithKeyz);
                }
            }
        }
        mContactsViewModel.getFilteredContacts().setValue(filteredContacts);
    }

    @Override
    public boolean onQueryTextSubmit (String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange (String newText) {
        mContactsViewModel.getFilter().setValue(newText);
        return false;
    }
}
