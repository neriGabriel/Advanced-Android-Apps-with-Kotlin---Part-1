package com.udacity.ui.main

import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.udacity.R
import com.udacity.databinding.ActivityMainBinding
import com.udacity.download.FileDownloadNotificator
import com.udacity.download.DownloadStatus
import com.udacity.extensions.getDownloadManager
import com.udacity.load.ButtonState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadFileName = ""
    private var downloadID: Long = 0L
    private var downloadContentObserver: ContentObserver? = null
    private var fileDownloadNotificator: FileDownloadNotificator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.also {
            setSupportActionBar(toolbar)
            registerReceiver(onDownloadCompletedReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        onLoadingButtonClicked()
    }

    private fun onLoadingButtonClicked() {
        with(main_content) {
            custom_button.setOnClickListener {
                when (download_option_radio_group.checkedRadioButtonId) {
                    View.NO_ID ->
                        Toast.makeText(
                            this@MainActivity,
                            "Please any option to download",
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        downloadFileName =
                            findViewById<RadioButton>(download_option_radio_group.checkedRadioButtonId)
                                .text.toString()
                        requestDownload()
                    }
                }
            }
        }
    }

    private val onDownloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadStatus = getDownloadManager().queryStatus(it)
                unregisterDownloadContentObserver()
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notifyDownloadStatus(downloadFileName, downloadStatus)
                }
            }
        }
    }

    private fun getDownloadNotificator(): FileDownloadNotificator = when (fileDownloadNotificator) {
        null -> FileDownloadNotificator(this, lifecycle).also { fileDownloadNotificator = it }
        else -> fileDownloadNotificator!!
    }

    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(COLUMN_STATUS))) {
                        STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }

    private fun requestDownload() {
        with(getDownloadManager()) {
            downloadID.takeIf { it != 0L }?.run {
                val downloadsCancelled = remove(downloadID)
                unregisterDownloadContentObserver()
                downloadID = 0L
                Timber.d("Number of downloads cancelled: $downloadsCancelled")
            }

            val request = Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadID = enqueue(request)

            createAndRegisterDownloadContentObserver()
        }
    }

    private fun DownloadManager.createAndRegisterDownloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { queryCurrentProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                DOWNLOAD_DIRECTORY.toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    private fun DownloadManager.queryCurrentProgress() {
        query(Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    val id = getInt(getColumnIndex(COLUMN_ID))
                    when (getInt(getColumnIndex(COLUMN_STATUS))) {
                        STATUS_FAILED -> {
                            Timber.d("Download $id: failed")
                            main_content.custom_button.updateButtonState(ButtonState.Completed)
                        }
                        STATUS_PAUSED -> {
                            Timber.d("Download $id: paused")
                        }
                        STATUS_PENDING -> {
                            Timber.d("Download $id: pending")
                        }
                        STATUS_RUNNING -> {
                            Timber.d("Download $id: running")
                            main_content.custom_button.updateButtonState(ButtonState.Loading)
                        }
                        STATUS_SUCCESSFUL -> {
                            Timber.d("Download $id: successful")
                            main_content.custom_button.updateButtonState(ButtonState.Completed)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadCompletedReceiver)
        unregisterDownloadContentObserver()
        fileDownloadNotificator = null
    }

    private fun unregisterDownloadContentObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val DOWNLOAD_DIRECTORY = "content://downloads/my_downloads"
    }

}
