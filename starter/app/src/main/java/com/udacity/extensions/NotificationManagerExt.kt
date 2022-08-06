package com.udacity.extensions

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.udacity.R
import com.udacity.download.DownloadStatus
import com.udacity.ui.detail.DetailActivity
import com.udacity.ui.detail.DetailActivity.Companion.bundleExtrasOf

private val DOWNLOAD_COMPLETED_ID = 1
private val NOTIFICATION_REQUEST_CODE = 1
private val DOWLOAD_DETAIL_ACTION = "com.udacity.action.DOWNLOAD_DETAIL"

/**
 * NotificationExt
 *
 * Extension responsible to create and send the download notification.
 *
 * @param: String, file name
 * @param: String, download status
 * @param: Context
 *
 * */
fun NotificationManager.createAndPostDownloadFileNotification(
    fileName: String,
    downloadStatus: DownloadStatus,
    context: Context
) {
    /**
    * Pending intent to open details activity when download finish
    * */
    val contentIntent = Intent(DOWLOAD_DETAIL_ACTION).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(bundleExtrasOf(fileName, downloadStatus))
    }

    /**
    * Defining pending intent as a content for the notification actions
    * */
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_REQUEST_CODE,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    /**
    * Notification action to check the downloaded file and it's status
    * @param Icon, for the action
    * @param String resource for the action
    * @param PendingIntent for the wanted action
    * */

    val checkDownloadAction = NotificationCompat.Action.Builder(
        IconCompat.createWithBitmap(getBitmapFromVectorDrawable(context, R.drawable.ic_more)),
        context.getString(R.string.notification_button),
        contentPendingIntent
    ).build()

    /**
    * Creating the notification "scope" state and build with the id [@DOWNLOAD_COMPLETED_ID]
    * */
    NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
        .apply {
            setSmallIcon(R.drawable.ic_assistant_black_24dp)
            setContentTitle(context.getString(R.string.notification_title))
            setContentText(context.getString(R.string.notification_description))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(contentPendingIntent)
            setAutoCancel(true)
            addAction(checkDownloadAction)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }.run {
            notify(DOWNLOAD_COMPLETED_ID, this.build())
        }
}

/**
* Creating the notification channel
* */
@SuppressLint("NewApi")
fun NotificationManager.createDownloadStatusNotificationChannel(context: Context) {
    Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.run {
        NotificationChannel(
            context.getString(R.string.notification_channel_id),
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(true)
        }.run {
            createNotificationChannel(this)
        }
    }
}