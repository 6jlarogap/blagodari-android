package blagodarie.rating.ui.profile;

import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import blagodarie.rating.R;
import blagodarie.rating.databinding.MainActivityBinding;
import blagodarie.rating.databinding.ProfileActivityBinding;
import blagodarie.rating.server.ServerApiResponse;
import blagodarie.rating.server.ServerConnector;
import blagodarie.rating.ui.main.MainViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class ProfileActivity
        extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        final Uri data = getIntent().getData();
        if (data != null) {
            final String profileId = data.getQueryParameter("id");
            if (profileId != null) {
                Observable.
                        fromCallable(() -> ServerConnector.sendRequestAndGetResponse("getprofileinfo?uuid=" + profileId)).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(
                                this::extractDataFromServerApiResponse,
                                throwable -> Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }
        } else {
            finish();
        }

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getProfileUrl().set(data.getQueryParameter("id"));
        ProfileActivityBinding activityBinding = DataBindingUtil.setContentView(this, R.layout.profile_activity);
        activityBinding.setViewModel(viewModel);
        AccountManager accountManager = AccountManager.get(this);
    }

    private void extractDataFromServerApiResponse(ServerApiResponse serverApiResponse){
        if (serverApiResponse.getCode() == 200) {
            if (serverApiResponse.getBody() != null) {
                final String responseBody = serverApiResponse.getBody();
                try {
                    final JSONObject userJSON = new JSONObject(responseBody);
                    final String userIdString = userJSON.getString("user_uuid");
                    final UUID userId = UUID.fromString(userIdString);
                    final String first_name = userJSON.getString("first_name");
                    final String middleName = userJSON.getString("middle_name");
                    final String lastName = userJSON.getString("last_name");
                    final String authToken = userJSON.getString("token");
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(this, getString(blagodarie.rating.auth.R.string.err_msg_incorrect_user_id), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
