package org.blagodari.ui.contacts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.BlagodariApp;
import org.blagodari.BuildConfig;
import org.blagodari.DataRepository;
import org.blagodari.R;
import org.blagodari.databinding.ContactsFragmentBinding;
import org.blagodari.ui.OnLikeCreateListener;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

public final class ContactsFragment
        extends Fragment
        implements SearchView.OnQueryTextListener, OnLikeCreateListener, OnContactSyncListener {

    private ContactsViewModel mContactsViewModel;
    private int mScrollYPosition = 0;
    private ContactsFragmentBinding mContactsFragmentBinding;
    /**
     * Источник доступа к БД.
     */
    private DataRepository mDataRepository;

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
        this.mContactsFragmentBinding = ContactsFragmentBinding.inflate(inflater, container, false);
        return this.mContactsFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Diagnostic.i();
        this.mDataRepository = ((BlagodariApp) requireActivity().getApplication()).getRepository();
        initViewModel();
        this.mContactsFragmentBinding.setViewModel(mContactsViewModel);

        setupContactsAdapter();
    }

    private void initViewModel () {
        //получаем идентификатор пользователя из Intent
        final Long userId = requireActivity().getIntent().getLongExtra(ContactsActivity.EXTRA_USER_ID, -1L);

        /*получаем из сохраненных настроек порядок сортировки контактов,
        если в настройках нет записи о порядке - сортируем по умолчанию*/
        final ContactsOrder contactsOrder = ContactsOrder.
                valueOf(
                        requireActivity().getSharedPreferences("pref", MODE_PRIVATE).
                                getString(ContactsActivity.PREF_ORDER_BY, ContactsOrder.getDefault().name())
                );

        //создаем фабрику
        final ContactsViewModel.Factory factory = new ContactsViewModel.Factory(
                this.mDataRepository,
                userId,
                contactsOrder
        );

        //создаем ContactsViewModel
        this.mContactsViewModel = new ViewModelProvider(requireActivity(), factory).get(ContactsViewModel.class);

        //mContactsViewModel.setContactItemNavigator(this);
    }

    @Override
    public void onCreateOptionsMenu (@NonNull Menu menu, MenuInflater inflater) {
        Diagnostic.i();
        inflater.inflate(R.menu.fragment_contacts, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.miFilter);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQuery(mContactsViewModel.getFilter(), false);
    }

    @Override
    public boolean onQueryTextSubmit (String query) {
        Diagnostic.i();
        switch (query) {
            case "\\log send":
                Diagnostic.sendLog((AppCompatActivity) getActivity(), BuildConfig.APPLICATION_ID);
                break;
            case "\\log on":
                Diagnostic.setIsLogEnabled(true);
                requireActivity().getSharedPreferences("pref", MODE_PRIVATE).edit().putBoolean("isLog", true).apply();
                Toast.makeText(getContext(), "Журнал включён", Toast.LENGTH_SHORT).show();
                break;
            case "\\log off":
                Diagnostic.setIsLogEnabled(false);
                requireActivity().getSharedPreferences("pref", MODE_PRIVATE).edit().putBoolean("isLog", false).apply();
                Toast.makeText(getContext(), "Журнал выключен", Toast.LENGTH_SHORT).show();
            case "\\error":
                throw new RuntimeException("Manual exception");
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange (String newText) {
        Diagnostic.i(newText);
        mContactsViewModel.setFilter(newText);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull final MenuItem item) {
        Diagnostic.i();
        if (item.getItemId() == R.id.miOrder) {
            showOrderingPopUpMenu();
        } else {
            switch (item.getItemId()) {
                case R.id.miOrderByName:
                    setOrder(ContactsOrder.NAME);
                    break;
                case R.id.miOrderByUpdateTimestamp:
                    setOrder(ContactsOrder.UPDATE_TIMESTAMP);
                    break;
                case R.id.miOrderByFame:
                    setOrder(ContactsOrder.FAME);
                    break;
                case R.id.miOrderByLikesCount:
                    setOrder(ContactsOrder.LIKES_COUNT);
                    break;
                case R.id.miOrderBySumLikesCount:
                    setOrder(ContactsOrder.SUM_LIKES_COUNT);
                    break;
            }
            item.setChecked(true);
        }
        return true;
    }

    private void setupContactsAdapter () {
        Diagnostic.i();
        ContactsAdapter contactsAdapter = new ContactsAdapter(
                (ContactItemClickListener) requireActivity(),
                this,
                this);
        mContactsFragmentBinding.rvContacts.setLayoutManager(new CustomGridLayoutManager(getContext(), getResources().getInteger(R.integer.horizontal_portrets_count)));
        mContactsFragmentBinding.rvContacts.setAdapter(contactsAdapter);
        mContactsFragmentBinding.rvContacts.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        mContactsViewModel.getContacts().observe(getViewLifecycleOwner(), contactsAdapter::submitList);
    }

    private void setOrder (ContactsOrder contactsOrder) {
        Diagnostic.i();
        mScrollYPosition = 0;
        requireActivity().getSharedPreferences("pref", MODE_PRIVATE).edit().putString(ContactsActivity.PREF_ORDER_BY, contactsOrder.name()).apply();
        mContactsViewModel.setContactsOrder(contactsOrder);
    }

    private void showOrderingPopUpMenu () {
        Diagnostic.i();
        PopupMenu popup = new PopupMenu(requireContext(), requireActivity().findViewById(R.id.miOrder));
        popup.getMenuInflater().inflate(R.menu.order_contacts, popup.getMenu());
        MenuItem checkedItem;
        switch (mContactsViewModel.getContactsOrder()) {
            case NAME:
                checkedItem = popup.getMenu().findItem(R.id.miOrderByName).setChecked(true);
                break;
            case UPDATE_TIMESTAMP:
                checkedItem = popup.getMenu().findItem(R.id.miOrderByUpdateTimestamp).setChecked(true);
                break;
            case FAME:
                checkedItem = popup.getMenu().findItem(R.id.miOrderByFame).setChecked(true);
                break;
            case LIKES_COUNT:
                checkedItem = popup.getMenu().findItem(R.id.miOrderByLikesCount).setChecked(true);
                break;
            case SUM_LIKES_COUNT:
                checkedItem = popup.getMenu().findItem(R.id.miOrderBySumLikesCount).setChecked(true);
                break;
            default:
                checkedItem = popup.getMenu().findItem(R.id.miOrderByName).setChecked(true);
        }
        checkedItem.setChecked(true);
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    @Override
    public synchronized void onCreateLike (@NonNull final Long contactId) {
        Diagnostic.i();
        final long createTimestamp = System.currentTimeMillis();
        Completable.fromAction(() ->
                this.mDataRepository.createLikeForContact(this.mContactsViewModel.getUserId(), contactId, createTimestamp)).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe();
    }

    @Override
    public void onSync (@NonNull Long contactId) {
        /*Completable.
                fromAction(() ->
                        GetLikesByKeyz.
                                getInstance().
                                execute(this.mDataRepository, contactId)).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe();*/
        /*Collection<Long> contactIds = new ArrayList<>();
        contactIds.add(contactId);
        //ServerSynchronizer.getInstance().startSyncDataApis(new GetContactSumInfo.DataIn(this.mDataRepository, contactIds), GetContactSumInfo.getInstance());
        Completable.
                fromAction(() -> GetContactSumInfo.getInstance().execute(new GetContactSumInfo.DataIn(this.mDataRepository, contactIds))).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe();*/
    }


    private final class CustomGridLayoutManager
            extends GridLayoutManager {

        CustomGridLayoutManager (Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public void onItemsMoved (RecyclerView recyclerView, int from, int to, int itemCount) {
            super.onItemsMoved(recyclerView, from, to, itemCount);
            scrollToPositionWithOffset(0, -mScrollYPosition);
        }
    }

}
