package org.blagodari.ui.contactdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.BlagodariApp;
import org.blagodari.databinding.ContactDetailFragmentBinding;
import org.blagodari.db.scheme.Like;
import org.blagodari.ui.OnLikeCancelListener;

import org.jetbrains.annotations.NotNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.blagodari.ui.contactdetail.ContactDetailActivity.EXTRA_CONTACT_ID;
import static org.blagodari.ui.contactdetail.ContactDetailActivity.EXTRA_USER_ID;

public final class ContactDetailFragment
        extends Fragment
        implements OnLikeCancelListener {

    private ContactDetailViewModel mContactDetailViewModel;
    private ContactDetailFragmentBinding mContactDetailFragmentBinding;

    @Override
    public void onCreate (@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Diagnostic.i();
        setRetainInstance(true);
    }

    @NotNull
    @Override
    public View onCreateView (
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState
    ) {
        Diagnostic.i();
        this.mContactDetailFragmentBinding = ContactDetailFragmentBinding.inflate(inflater, container, false);
        return mContactDetailFragmentBinding.getRoot();
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

    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Diagnostic.i();
        initViewModel();
        this.mContactDetailFragmentBinding.setViewModel(this.mContactDetailViewModel);
        setupPhonesAdapter();
        setupEmailsAdapter();
    }

    private void setupPhonesAdapter () {
        KeyzAdapter keyzAdapter = new KeyzAdapter();
        mContactDetailFragmentBinding.rvPhones.setAdapter(keyzAdapter);
        this.mContactDetailViewModel.getPhones().observe(getViewLifecycleOwner(), keyzAdapter::setData);
    }

    private void setupEmailsAdapter () {
        KeyzAdapter keyzAdapter = new KeyzAdapter();
        mContactDetailFragmentBinding.rvEmails.setAdapter(keyzAdapter);
        this.mContactDetailViewModel.getEmails().observe(getViewLifecycleOwner(), keyzAdapter::setData);
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
