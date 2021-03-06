package com.example.internet_speed_testing

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError

class InternetSpeedBuilder(var activity: Activity) {

    private var countTestSpeed = 0
    private var LIMIT = 3
    lateinit var downloadUrl: String
    lateinit var uploadUrl: String
    lateinit var javaListener: OnEventInternetSpeedListener
    lateinit var onDownloadProgressListener: ()->Unit
    lateinit var onUploadProgressListener: ()->Unit
    lateinit var onTotalProgressListener: ()->Unit
    lateinit var speedDownloadTask: SpeedDownloadTestTask;
    lateinit var speedUploadTestTask: SpeedUploadTestTask;
    private lateinit var progressModel: ProgressionModel

    fun start(downloadUrl: String,uploadUrl: String, limitCount: Int) {
        this.downloadUrl = downloadUrl
        this.uploadUrl = uploadUrl
        this.LIMIT = limitCount
        startTestDownload()
    }

    fun stop() {
        speedDownloadTask.cancelDownloadTask()
        speedUploadTestTask.cancelUploadTask()
    }

    fun setOnEventInternetSpeedListener(javaListener: OnEventInternetSpeedListener) {
        this.javaListener = javaListener
    }

    fun setOnEventInternetSpeedListener(onDownloadProgress: ()->Unit, onUploadProgress: ()->Unit, onTotalProgress: ()->Unit) {
        this.onDownloadProgressListener = onDownloadProgress
        this.onUploadProgressListener = onUploadProgress
        this.onTotalProgressListener = onTotalProgress
    }


    private fun startTestDownload() {
        progressModel = ProgressionModel()
        speedDownloadTask = SpeedDownloadTestTask()
        speedUploadTestTask = SpeedUploadTestTask()
        speedDownloadTask.execute()
    }

    private fun startTestUpload() {
        speedUploadTestTask.execute()

    }

    interface OnEventInternetSpeedListener {
        fun onDownloadProgress(count: Int, progressModel: ProgressionModel)
        fun onUploadProgress(count: Int, progressModel: ProgressionModel)
        fun onTotalProgress(count: Int, progressModel: ProgressionModel)
    }

    inner class SpeedDownloadTestTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }
        val speedTestSocket = SpeedTestSocket()

        override fun doInBackground(vararg params: Void): String? {


            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {

                override fun onCompletion(report: SpeedTestReport) {
                    // called when download/upload is finished
                    Log.v("speedtest Download" + countTestSpeed, "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Download" + countTestSpeed, "[COMPLETED] rate in bit/s   : " + report.transferRateBit)

                    /*downloadProgressModel.progressTotal = 50f
                    downloadProgressModel.progressDownload = 100f
                    downloadProgressModel.downloadSpeed = report.transferRateBit

                    totalProgressModel.progressTotal = 50f
                    totalProgressModel.progressDownload = 100f
                    totalProgressModel.downloadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onDownloadProgress(countTestSpeed, downloadProgressModel)
                        javaListener.onTotalProgress(countTestSpeed, totalProgressModel)

                    }*/

                    progressModel.progressTotal = 50f
                    progressModel.progressUpload= 100f
                    progressModel.downloadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onDownloadProgress(countTestSpeed, progressModel)
                        javaListener.onTotalProgress(countTestSpeed, progressModel)

                    }

                    startTestUpload()

                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    // called when a download/upload error occur
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    // called to notify download/upload progress
                    Log.v("speedtest Download" + countTestSpeed, "[PROGRESS] progress : $percent%")
                    Log.v("speedtest Download" + countTestSpeed, "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Download" + countTestSpeed, "[PROGRESS] rate in bit/s   : " + report.transferRateBit)

                    progressModel.progressTotal = percent / 2
                    progressModel.progressDownload = percent
                    progressModel.downloadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onDownloadProgress(countTestSpeed, progressModel)
                       // javaListener.onTotalProgress(countTestSpeed, progressModel)
                    }

                }
            })

            speedTestSocket.startDownload(downloadUrl)

            return null
        }

        fun cancelDownloadTask() {
            //this.cancel(true);
            speedTestSocket.clearListeners()
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class SpeedUploadTestTask : AsyncTask<Void, Void, Void>() {
        val speedTestSocket = SpeedTestSocket()

        override fun doInBackground(vararg params: Void): Void? {


            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {

                override fun onCompletion(report: SpeedTestReport) {
                    // called when download/upload is finished
                    Log.v("speedtest Upload" + countTestSpeed, "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Upload" + countTestSpeed, "[COMPLETED] rate in bit/s   : " + report.transferRateBit)

                    progressModel.progressTotal = 100f
                    progressModel.progressUpload= 100f
                    progressModel.uploadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onUploadProgress(countTestSpeed, progressModel)
                        javaListener.onTotalProgress(countTestSpeed, progressModel)
                    }


                    countTestSpeed++
                    if (countTestSpeed < LIMIT) {
                        startTestDownload()
                    }
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    // called when a download/upload error occur
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    // called to notify download/upload progress
                    Log.v("speedtest Upload" + countTestSpeed, "[PROGRESS] progress : $percent%")
                    Log.v("speedtest Upload" + countTestSpeed, "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Upload" + countTestSpeed, "[PROGRESS] rate in bit/s   : " + report.transferRateBit)

                    progressModel.progressTotal = percent / 2 + 50
                    progressModel.progressUpload = percent
                    progressModel.uploadSpeed= report.transferRateBit

                    activity.runOnUiThread {

                        if (countTestSpeed < LIMIT) {
                            javaListener.onUploadProgress(countTestSpeed, progressModel)
                            //javaListener.onTotalProgress(countTestSpeed, progressModel)

                        }
                    }

                }
            })

           // val fileName = SpeedTestUtils.generateFileName() + ".txt"
            Log.v("speedtest", "uploadUrl : $uploadUrl")
            //speedTestSocket.startUpload("ftp://speedtest.tele2.net/upload/$fileName", 1000000)
            speedTestSocket.startUpload(uploadUrl, 1000000)
            //speedTestSocket.startDownload(url)
            return null
        }

        fun cancelUploadTask() {
           //this.cancel(true);
            speedTestSocket.clearListeners()

        }
    }

}