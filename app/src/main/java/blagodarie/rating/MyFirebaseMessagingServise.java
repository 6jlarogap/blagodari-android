package blagodarie.rating;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import blagodarie.rating.ui.user.UserActivity;

public class MyFirebaseMessagingServise
        extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingServise.class.getSimpleName();

    private static int id = 0;

    @Override
    public void onNewToken (@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived (RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if (remoteMessage.getNotification().getBody() != null) {
                try {
                    final JSONObject json = new JSONObject(remoteMessage.getNotification().getBody());
                    String firstName = json.getString("first_name");
                    String lastName = json.getString("last_name");
                    String photo = json.getString("photo");
                    int operationTypeId = json.getInt("operation_type_id");
                    OperationType operationType = OperationType.getById(operationTypeId);
                    String comment = json.getString("comment");

                    String title = null;
                    String channelId = null;
                    String channelName = null;
                    String channelDescription = null;
                    if (operationType != null) {
                        switch (operationType) {
                            case THANKS:
                                title = getApplicationContext().getString(R.string.notification_title_thanks_pattern, firstName, lastName);
                                channelId = getApplicationContext().getString(R.string.notification_chanel_id_thanks);
                                channelName = getApplicationContext().getString(R.string.notification_chanel_name_thanks);
                                channelDescription = getApplicationContext().getString(R.string.notification_chanel_description_thanks);
                                break;
                            case MISTRUST:
                                title = getApplicationContext().getString(R.string.notification_title_mistrus_pattern, firstName, lastName);
                                channelId = getApplicationContext().getString(R.string.notification_chanel_id_mistrust);
                                channelName = getApplicationContext().getString(R.string.notification_chanel_name_mistrust);
                                channelDescription = getApplicationContext().getString(R.string.notification_chanel_description_mistrust);
                                break;
                            case MISTRUST_CANCEL:
                                title = getApplicationContext().getString(R.string.notification_title_cancel_mistrust_pattern, firstName, lastName);
                                channelId = getApplicationContext().getString(R.string.notification_chanel_id_cancel_mistrust);
                                channelName = getApplicationContext().getString(R.string.notification_chanel_name_cancel_mistrust);
                                channelDescription = getApplicationContext().getString(R.string.notification_chanel_description_cancel_mistrust);
                                break;
                            default:
                                title = "";
                        }
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(comment)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                        channel.setDescription(channelDescription);
                        // Register the channel with the system; you can't change the importance
                        // or other notification behaviors after this
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
                    //notificationManager.notify(1, builder.build());
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(() -> Picasso.get().load(photo)
                            .resize(250, 250)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded (Bitmap bitmap, Picasso.LoadedFrom from) {
                                    builder.setLargeIcon(bitmap);
                                    notificationManager.notify(id++, builder.build());
                                }

                                @Override
                                public void onBitmapFailed (Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad (Drawable placeHolderDrawable) {

                                }
                            }));
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
