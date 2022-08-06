package com.udacity.download

/**
 * ENUM class responsible of track download status
 *
 * @Param: status, string: The current status of the download
 *
 * [AVAILABLE OPTIONS]: [SUCCESSFUL, FAILED, UNKNOWN]
 * */
enum class DownloadStatus(val status: String) {
    SUCCESSFUL("Successful"),
    FAILED("Failed"),
    UNKNOWN("Unknown")
}