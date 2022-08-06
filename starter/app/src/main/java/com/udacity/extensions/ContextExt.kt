package com.udacity.extensions

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat


/**
 * ContextExt
 *
 * Extension responsible to return NotificationManager (SystemService manager).
 * */
fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(
    this,
    NotificationManager::class.java
) as NotificationManager

/**
 * ContextExt
 *
 * Extension responsible to return DownloadManager (SystemService manager).
 * */
fun Context.getDownloadManager(): DownloadManager = ContextCompat.getSystemService(
    this,
    DownloadManager::class.java
) as DownloadManager