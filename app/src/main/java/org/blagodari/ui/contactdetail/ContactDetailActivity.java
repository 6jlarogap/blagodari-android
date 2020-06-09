package org.blagodari.ui.contactdetail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ex.diagnosticlib.Diagnostic;
import com.google.android.material.tabs.TabLayoutMediator;
import org.blagodari.BlagodariApp;
import org.blagodari.R;
import org.blagodari.databinding.ContactDetailActivityBinding;
import org.blagodari.databinding.UserLikeFragmentBinding;
import org.blagodari.db.scheme.Like;
import org.blagodari.server.api.GetAllLikesApi;
import org.blagodari.server.api.GetLikesByKeyz;
import org.blagodari.ui.BaseActivity;
import org.blagodari.ui.OnLikeCancelListener;
import org.blagodari.ui.OnLikeCreateListener;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public final class ContactDetailActivity
        extends BaseActivity<ContactDetailViewModel>
        implements OnLikeCreateListener {

    public static final String EXTRA_USER_ID = "com.vsdrozd.blagodarie.ui.contactdetail.USER_ID";
    public static final String EXTRA_CONTACT_ID = "com.vsdrozd.blagodarie.ui.contactdetail.CONTACT_ID";
    /**
     * Идентификатор запроса на разрешение чтения контактов.
     */
    static final int PERMISSION_REQUEST_READ_CONTACTS = 1;

    private ContactDetailActivityBinding mContactDetailActivityBinding;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostic.i();

        this.mContactDetailActivityBinding = DataBindingUtil.setContentView(this, R.layout.contact_detail_activity);
        this.mContactDetailActivityBinding.setViewModel(getViewModel());
        getViewModel().getContact().observe(this, mContactDetailActivityBinding::setContact);

        this.mContactDetailActivityBinding.setOnLikeCreateListener(this);

        setupToolbar();
        setupViewPager();
    }

    @Override
    protected final ContactDetailViewModel createViewModel () {
        //получить идентификатор пользователя
        final Long userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);
        //получить идентификатор контакта
        final Long contactId = getIntent().getLongExtra(EXTRA_CONTACT_ID, -1L);

        //создать фабрику
        final ContactDetailViewModel.Factory factory = new ContactDetailViewModel.Factory(
                getDataRepository(),
                userId,
                contactId
        );

        //создать ViewModel
        return new ViewModelProvider(this, factory).get(ContactDetailViewModel.class);
    }

    private void setupViewPager () {
        final FragmentStateAdapter pagerAdapter = new LikePagerAdapter(this);
        this.mContactDetailActivityBinding.vpLikes.setAdapter(pagerAdapter);
        new TabLayoutMediator(
                this.mContactDetailActivityBinding.tabLayout,
                this.mContactDetailActivityBinding.vpLikes,
                (tab, position) -> tab.setText(position == 0 ? R.string.user_like_fragment_tab_title : R.string.all_like_fragment_tab_title)
        ).attach();
    }

    @Override
    public void onStart () {
        super.onStart();
        Diagnostic.i();
        checkPermissionReadContacts();
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Diagnostic.i();
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //mContactDetailViewModel.setContentObserver();
                    //mContactDetailViewModel.refreshContacts();
                }
                break;
            }
        }
    }

    private void setupToolbar () {
        Diagnostic.i();
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermissionReadContacts () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Diagnostic.i("Request permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
        } else {
            Diagnostic.i("Permission granted");
            //mContactDetailViewModel.setContentObserver();
            //mContactDetailViewModel.refreshContacts();
        }
    }

    @Override
    public void onCreateLike (@NonNull final Long contactId) {
        Diagnostic.i();
        final long createTimestamp = System.currentTimeMillis();
        Completable.fromAction(() ->
                getDataRepository().createLikeForContact(getViewModel().getUserId(), contactId, createTimestamp)).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe();
    }

    public static class UserLikeFragment
            extends Fragment
            implements OnLikeCancelListener {

        private ContactDetailViewModel mContactDetailViewModel;
        private UserLikeFragmentBinding mUserLikeFragmentBinding;

        @NotNull
        @Override
        public View onCreateView (
                @NonNull final LayoutInflater inflater,
                @Nullable final ViewGroup container,
                @Nullable final Bundle savedInstanceState
        ) {
            Diagnostic.i();
            this.mUserLikeFragmentBinding = UserLikeFragmentBinding.inflate(inflater, container, false);
            return mUserLikeFragmentBinding.getRoot();
        }

        @Override
        public void onActivityCreated (@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Diagnostic.i();
            initViewModel();
            setupLikesAdapter();
        }

        private void initViewModel () {
            //получить идентификатор пользователя
            final Long userId = requireActivity().getIntent().getLongExtra(EXTRA_USER_ID, -1L);
            //получить идентификатор контакта
            final Long contactId = requireActivity().getIntent().getLongExtra(EXTRA_CONTACT_ID, -1L);

            //создать фабрику
            final ContactDetailViewModel.Factory factory = new ContactDetailViewModel.Factory(
                    ((BlagodariApp) requireActivity().getApplication()).getRepository(),
                    userId,
                    contactId
            );

            //создать ViewModel
            this.mContactDetailViewModel = new ViewModelProvider(this, factory).get(ContactDetailViewModel.class);
        }

        private void setupLikesAdapter () {
            LikeAdapter likeAdapter = new LikeAdapter(this.mContactDetailViewModel.getUserId(), this);
            this.mUserLikeFragmentBinding.rvLikes.setAdapter(likeAdapter);
            this.mUserLikeFragmentBinding.rvLikes.addItemDecoration(new DividerItemDecoration(mUserLikeFragmentBinding.rvLikes.getContext(), DividerItemDecoration.VERTICAL));
            this.mContactDetailViewModel.getLikes().observe(getViewLifecycleOwner(), likeAdapter::setData);
        }

        @Override
        public void onCancelLike (@NonNull final Like like) {
            ((BlagodariApp) requireActivity().getApplication()).
                    getRepository().
                    cancelLike(like).
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe();
        }
    }

    public static class AllLikeFragment
            extends Fragment
            implements OnLikeCancelListener {

        private ContactDetailViewModel mContactDetailViewModel;
        private UserLikeFragmentBinding mAllLikeFragmentBinding;
        private final CompositeDisposable mDisposables = new CompositeDisposable();

        @NotNull
        @Override
        public View onCreateView (
                @NonNull final LayoutInflater inflater,
                @Nullable final ViewGroup container,
                @Nullable final Bundle savedInstanceState
        ) {
            Diagnostic.i();
            this.mAllLikeFragmentBinding = UserLikeFragmentBinding.inflate(inflater, container, false);
            return this.mAllLikeFragmentBinding.getRoot();
        }

        @Override
        public void onActivityCreated (@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Diagnostic.i();
            initViewModel();
            getAllLikes();
        }

        private void initViewModel () {
            //получить идентификатор пользователя
            final Long userId = requireActivity().getIntent().getLongExtra(EXTRA_USER_ID, -1L);
            //получить идентификатор контакта
            final Long contactId = requireActivity().getIntent().getLongExtra(EXTRA_CONTACT_ID, -1L);

            //создать фабрику
            final ContactDetailViewModel.Factory factory = new ContactDetailViewModel.Factory(
                    ((BlagodariApp) requireActivity().getApplication()).getRepository(),
                    userId,
                    contactId
            );

            //создать ViewModel
            this.mContactDetailViewModel = new ViewModelProvider(this, factory).get(ContactDetailViewModel.class);
        }

        private void getAllLikes () {
            Diagnostic.i();
            this.mDisposables.add(
                    Observable.fromCallable(() -> GetLikesByKeyz.getInstance().execute(new GetAllLikesApi.DataIn(((BlagodariApp) requireActivity().getApplication()).getRepository(), this.mContactDetailViewModel.getContactId())))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setupLikesAdapter)
            );
        }

        private void setupLikesAdapter (@NonNull final GetAllLikesApi.Result result) {
            final LikeAdapter likeAdapter = new LikeAdapter(this.mContactDetailViewModel.getUserId(), this);
            likeAdapter.setData(result.getLikes());
            this.mAllLikeFragmentBinding.rvLikes.setAdapter(likeAdapter);
            this.mAllLikeFragmentBinding.rvLikes.addItemDecoration(new DividerItemDecoration(this.mAllLikeFragmentBinding.rvLikes.getContext(), DividerItemDecoration.VERTICAL));
        }

        @Override
        public void onCancelLike (@NonNull final Like like) {
            ((BlagodariApp) requireActivity().getApplication()).
                    getRepository().
                    cancelLike(like).
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe();
        }
    }

    private class LikePagerAdapter extends FragmentStateAdapter {
        LikePagerAdapter (@NonNull final FragmentActivity fa) {
            super(fa);
        }

        @NotNull
        @Override
        public Fragment createFragment (final int position) {
            switch (position) {
                case 0:
                    return new UserLikeFragment();
                case 1:
                    return new AllLikeFragment();
                default:
                    throw new IllegalArgumentException("Illegal fragment position");
            }
        }

        @Override
        public int getItemCount () {
            return 2;
        }

    }
}
