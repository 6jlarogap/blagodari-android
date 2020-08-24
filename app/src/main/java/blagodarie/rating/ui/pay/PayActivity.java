package blagodarie.rating.ui.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yandex.money.api.authorization.AuthorizationData;
import com.yandex.money.api.authorization.AuthorizationParameters;
import com.yandex.money.api.methods.InstanceId;
import com.yandex.money.api.methods.Token;
import com.yandex.money.api.methods.payment.BaseRequestPayment;
import com.yandex.money.api.methods.payment.RequestExternalPayment;
import com.yandex.money.api.methods.payment.params.P2pTransferParams;
import com.yandex.money.api.model.Scope;
import com.yandex.money.api.net.ApiRequest;
import com.yandex.money.api.net.AuthorizationCodeResponse;
import com.yandex.money.api.net.clients.ApiClient;
import com.yandex.money.api.net.clients.DefaultApiClient;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import blagodarie.rating.R;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PayActivity
        extends AppCompatActivity {

    private static final String TAG = PayActivity.class.getSimpleName();

    @Override
    protected void onCreate (@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_activity);
        startAuthorization();
    }

    public void startAuthorization () {
        Completable.fromAction(() -> {
            String clientId = "019433286201A8E0BE421CD1274019B3B7DD84A1864D67B09493FEFA028772B3";
            ApiClient client = new DefaultApiClient.Builder()
                    .setClientId(clientId)
                    .create();

            ApiRequest<InstanceId> request1 = new InstanceId.Request(clientId);
            InstanceId instanceId = client.execute(request1);

            P2pTransferParams params = new P2pTransferParams.
                    Builder("4100115682133636").
                    setAmount(new BigDecimal("100")).
                    setComment("hello man").
                    create();

            ApiRequest<RequestExternalPayment> request2 = RequestExternalPayment.Request.newInstance(instanceId.instanceId, params);
            RequestExternalPayment requestExternalPayment = client.execute(request2);


            Log.d(TAG, requestExternalPayment.toString());
        }).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(
                        () -> {
                            Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
                        },
                        throwable -> {
                            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    public static Intent createSelfIntent (
            @NonNull final Context context
    ) {
        Log.d(TAG, "createSelfIntent");
        return new Intent(context, PayActivity.class);
    }

}
