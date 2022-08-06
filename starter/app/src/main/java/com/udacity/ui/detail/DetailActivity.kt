package com.udacity.ui.detail

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.udacity.BuildConfig
import com.udacity.R
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding
import com.udacity.download.DownloadStatus
import kotlinx.android.synthetic.main.activity_detail.*
import timber.log.Timber

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val fileName by lazy {
        intent?.extras?.getString(EXTRA_FILE_NAME, unknownText)
    }

    private val downloadStatus by lazy {
        intent?.extras?.getString(EXTRA_DOWNLOAD_STATUS, unknownText)
    }

    private val unknownText by lazy {
        getString(R.string.unknown_file)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)

        binding.apply {
            setSupportActionBar(toolbar)
            lifecycleOwner = this@DetailActivity
            detailContent.initializeView()
        }
    }

    private fun ContentDetailBinding.initializeView() {
        fileNameText.text = fileName
        downloadStatusText.text = downloadStatus
        okButton.setOnClickListener { finish() }
        changeViewForDownloadStatus()
    }

    private fun ContentDetailBinding.changeViewForDownloadStatus() {
        when (downloadStatusText.text) {
            DownloadStatus.SUCCESSFUL.status -> {
                changeDownloadStatusImageTo(R.drawable.ic_check)
                changeDownloadStatusColorTo(R.color.colorPrimaryDark)
            }
            DownloadStatus.FAILED.status -> {
                changeDownloadStatusImageTo(R.drawable.ic_error)
                changeDownloadStatusColorTo(R.color.design_default_color_primary)
            }
        }
    }

    private fun ContentDetailBinding.changeDownloadStatusImageTo(@DrawableRes imageRes: Int) {
        downloadStatusImage.setImageResource(imageRes)
    }

    private fun ContentDetailBinding.changeDownloadStatusColorTo(@ColorRes colorRes: Int) {
        applicationContext.getColor(colorRes)
            .also { color ->
                downloadStatusImage.imageTintList = ColorStateList.valueOf(color)
                downloadStatusText.setTextColor(color)
            }
    }

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"

        fun bundleExtrasOf(
            fileName: String,
            downloadStatus: DownloadStatus
        ) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.status
        )
    }
}
