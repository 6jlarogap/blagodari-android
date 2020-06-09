package org.blagodari.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.BlagodariApp;
import org.blagodari.DataRepository;
import org.blagodari.db.scheme.User;
import org.blagodari.ui.acquaintance.AcquaintanceActivity;
import org.blagodari.ui.contacts.ContactsActivity;
import org.blagodari.ui.newcontacts.NewContactsActivity;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Активити отображается сразу после запуска приложения. Инициализирует идентификатор пользователя.
 * Если пользователя не существует, то создает его.
 *
 * @author sergeGabrus
 */
public final class SplashActivity
        extends AppCompatActivity {

    /**
     * Хранилище БД.
     */
    private DataRepository mDataRepository;

    /**
     * Контейнер объектов Disposable.
     */
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    /**
     * Вызывается перед первым созданием Activity.
     *
     * @param savedInstanceState Данные после предыдущего выключения.
     */
    @Override
    protected void onCreate (final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDataRepository = ((BlagodariApp)getApplication()).getRepository();
        initUser();
    }

    /**
     * Вызывается перед тем, как Activity будет уничтожено.
     */
    @Override
    protected void onDestroy () {
        super.onDestroy();
        this.mDisposables.dispose();
    }

    /**
     * Инициализирует пользователя, после чего запускает проверку авторизации.
     */
    private void initUser () {
        Diagnostic.i();
        Disposable initDisposable = mDataRepository.
                getOrCreateUser(1L).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(
                        this::checkAuthorized,
                        Diagnostic::e
                );
        this.mDisposables.add(initDisposable);
    }

    /**
     * Проверяет авторизован ли пользователь. Если пользователь авторизован, запускает
     * {@link ContactsActivity}, если нет - {@link AcquaintanceActivity}.
     *
     * @param user Пользователь.
     */
    private void checkAuthorized (@NonNull final User user) {
        Diagnostic.i();
        Disposable checkAuthorizedDisposable = mDataRepository.
                isAuthorizedUser(user.getId()).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(
                        isAuthorized -> {
                            if (isAuthorized) {
                                startContactsActivity(user.getId());
                            } else {
                                startAcquaintanceActivity(user.getId());
                            }
                        },
                        Diagnostic::e
                );

        mDisposables.add(checkAuthorizedDisposable);
    }

    /**
     * Запускает {@link ContactsActivity}, передавая ему идентификатор пользователя, и завершает
     * текущее Activity.
     *
     * @param userId Идентификатор пользователя.
     */
    private void startContactsActivity (@NonNull final Long userId) {
        Diagnostic.i();
        //final Intent intent = ContactsActivity.createIntent(this, userId);
        final Intent intent = NewContactsActivity.createIntent(this, userId);
        startActivity(intent);
        finish();
    }

    /**
     * Запускает {@link AcquaintanceActivity}, передавая ему идентификатор пользователя, и завершает
     * текущее Activity.
     *
     * @param userId Идентификатор пользователя.
     */
    private void startAcquaintanceActivity (@NonNull final Long userId) {
        Diagnostic.i();
        final Intent intent = AcquaintanceActivity.createIntent(this, userId);
        startActivity(intent);
        finish();
    }
}
