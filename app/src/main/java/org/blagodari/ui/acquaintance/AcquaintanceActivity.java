package org.blagodari.ui.acquaintance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.BlagodariApp;
import org.blagodari.R;
import org.blagodari.ui.GoogleSignInListener;
import org.blagodari.ui.newcontacts.NewContactsActivity;

public class AcquaintanceActivity
        extends AppCompatActivity
        implements AcquaintanceNavigator {

    private static final String EXTRA_USER_ID = "com.vsdrozd.blagodarie.ui.acquaintance.USER_ID";
    private Long userId;
    private NavController navController;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acquaintance_activity);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Diagnostic.i();
        if (requestCode == GoogleSignInListener.ACTIVITY_REQUEST_CODE_GOGGLE_SIGN_IN) {
            if (GoogleSignInListener.getInstance().handleSignInResult(data, userId, ((BlagodariApp) getApplication()).getRepository())) {
                toContacts();
            }
        }
    }

    @Override
    public void toGreeting () {
        navController.navigate(R.id.action_agreementFragment_to_greetingFragment2);
    }

    @Override
    public void fromGreetingToAgreement () {
        navController.navigate(R.id.action_greetingFragment_to_agreementFragment);
    }

    @Override
    public void fromEnterToAgreement () {
        navController.navigate(R.id.action_enterFragment_to_agreementFragment2);
    }

    @Override
    public void toEnter () {
        navController.navigate(R.id.action_agreementFragment_to_enterFragment);
    }

    @Override
    public void toContacts () {
        Bundle bundle = new Bundle();
        //bundle.putLong(ContactsActivity.EXTRA_USER_ID, userId);
        //navController.navigate(R.id.action_enterFragment_to_contactsActivity, bundle);
        bundle.putLong(NewContactsActivity.EXTRA_USER_ID, userId);
        navController.navigate(R.id.action_enterFragment_to_newContactsActivity, bundle);
        finish();
    }

    public static Intent createIntent (
            @NonNull final Context context,
            @NonNull final Long userId
    ){
        final Intent intent = new Intent(context, AcquaintanceActivity.class);
        intent.putExtra(AcquaintanceActivity.EXTRA_USER_ID, userId);
        return intent;
    }
}
