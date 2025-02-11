package org.sayandev.download

interface DownloadService {
    companion object {
        fun simple(): SimpleDownloadService {
            return SimpleDownloadService()
        }

        fun async(threadCount: Int): AsyncDownloadService {
            return AsyncDownloadService(threadCount)
        }
    }
}