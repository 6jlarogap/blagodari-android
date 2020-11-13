package blagodarie.rating.auth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "FirebaseNotification"

internal fun subscribeOnFirebaseNotifications(userId: String) {
    FirebaseMessaging.getInstance().subscribeToTopic("user_$userId")
            .addOnCompleteListener { task: Task<Void?> ->
                var msg = "subscribed"
                if (!task.isSuccessful) {
                    msg = "subscribe failed"
                }
                Log.d(TAG, msg)
            }
}

internal fun unSubscribeOnFirebaseNotifications(userId: String) {
    FirebaseMessaging.getInstance().unsubscribeFromTopic("user_$userId")
            .addOnCompleteListener { task: Task<Void?> ->
                var msg = "unsubscribed"
                if (!task.isSuccessful) {
                    msg = "unsubscribe failed"
                }
                Log.d(TAG, msg)
            }
}