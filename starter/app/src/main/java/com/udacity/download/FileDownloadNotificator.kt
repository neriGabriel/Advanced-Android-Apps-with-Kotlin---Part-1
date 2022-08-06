package com.udacity.download

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.udacity.R
import com.udacity.extensions.createAndPostDownloadFileNotification
import com.udacity.extensions.createDownloadStatusNotificationChannel
import com.udacity.extensions.getNotificationManager

/**
 * FileDownloadNotificator
 *
 * LifeCycleAware component which has the responsibility of notify other components that
 * a download was finished and post notification.
 * */
class FileDownloadNotificator(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {

    /**
     * notifyDownloadStatus
     *
     * Responsible of validate the current lifecycle state and post download notification.
     * */
    fun notifyDownloadStatus(
        fileName: String,
        downloadStatus: DownloadStatus
    ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Toast.makeText(
                context,
                context.getString(R.string.notification_download_completed),
                Toast.LENGTH_SHORT
            ).show()
        }
        with(context.applicationContext) {
            getNotificationManager().run {
                createDownloadStatusNotificationChannel(applicationContext)
                createAndPostDownloadFileNotification(
                    fileName,
                    downloadStatus,
                    applicationContext
                )
            }
        }
    }

    /**
     * registerObserver
     *
     * Responsible of register lifecycle observer on onCreate state.
     * */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun registerObserver() = lifecycle.addObserver(this)

    /**
     * unregisterObserver
     *
     * Responsible of unregister lifecycle observer on onDestroy state.
     * */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterObserver() = lifecycle.removeObserver(this)
}