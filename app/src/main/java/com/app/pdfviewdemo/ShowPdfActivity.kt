package com.app.pdfviewdemo

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danjdt.pdfviewer.PdfViewer
import com.danjdt.pdfviewer.interfaces.OnErrorListener
import com.danjdt.pdfviewer.interfaces.OnPageChangedListener
import com.danjdt.pdfviewer.utils.PdfPageQuality
import com.danjdt.pdfviewer.view.PdfViewerRecyclerView
import java.io.IOException

class ShowPdfActivity : AppCompatActivity(), OnPageChangedListener, OnErrorListener {
    private var download_file_url =
        "https://researchtorevenue.files.wordpress.com/2015/04/1r41ai10801601_fong.pdf"
    private lateinit var tvCounter: TextView
    private lateinit var rootView: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_pdf)
        tvCounter = findViewById(R.id.tvCounter)
        rootView = findViewById(R.id.rootView)

        PdfViewer.Builder(rootView)
            .view(PdfViewerRecyclerView(this))
            .setMaxZoom(3f)
            .setZoomEnabled(true)
            .quality(PdfPageQuality.QUALITY_1080)
            .setOnErrorListener(this)
            .setOnPageChangedListener(this)
            .build()
            .load(download_file_url)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun downloadFile(url: String) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Download")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Download.pdf")
        downloadManager.enqueue(request)

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.download) onDownload()
        return super.onOptionsItemSelected(item)
    }

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                Toast.makeText(
                    context,
                    "File is Downloaded Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                context.unregisterReceiver(this)
            }
        }
    }

    override fun onAttachViewError(e: Exception) {
        e.printStackTrace()
    }

    override fun onFileLoadError(e: Exception) {
        Log.e("error", "onFileLoadError: ${e.printStackTrace()}")
    }

    override fun onPdfRendererError(e: IOException) {
        e.printStackTrace()
    }

    override fun onPageChanged(page: Int, total: Int) {
        tvCounter.text = getString(R.string.pdf_page_counter, page, total)
    }

    private fun onDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadFile(download_file_url)
        } else {
            requestPermission.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                downloadFile(download_file_url)
            } else {

            }
        }

}