package ml.adamsprogs.bimba.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.IntentFilter
import android.widget.TextView
import ml.adamsprogs.bimba.*


class NoDbActivity : AppCompatActivity(), NetworkStateReceiver.OnConnectivityChangeListener, MessageReceiver.OnTimetableDownloadListener {
    private val networkStateReceiver = NetworkStateReceiver()
    private val timetableDownloadReceiver = MessageReceiver()
    private var serviceRunning = false
    private var askedForNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nodb)
        var filter = IntentFilter(TimetableDownloader.ACTION_DOWNLOADED)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(timetableDownloadReceiver, filter)
        timetableDownloadReceiver.addOnTimetableDownloadListener(this)

        if (!NetworkStateReceiver.isNetworkAvailable(this)) {
            askedForNetwork = true
            (findViewById(R.id.noDbCaption) as TextView).text = getString(R.string.no_db_connect)
            filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            registerReceiver(networkStateReceiver, filter)
            networkStateReceiver.addOnConnectivityChangeListener(this)
        } else
            downloadTimetable()
    }

    override fun onResume() {
        super.onResume()
        var filter = IntentFilter(TimetableDownloader.ACTION_DOWNLOADED)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(timetableDownloadReceiver, filter)
        if (!NetworkStateReceiver.isNetworkAvailable(this)) {
            askedForNetwork = true
            (findViewById(R.id.noDbCaption) as TextView).text = getString(R.string.no_db_connect)
            filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            registerReceiver(networkStateReceiver, filter)
            networkStateReceiver.addOnConnectivityChangeListener(this)
        } else if (!serviceRunning)
            downloadTimetable()
    }

    private fun downloadTimetable() {
        (findViewById(R.id.noDbCaption) as TextView).text = getString(R.string.no_db_downloading)
        serviceRunning = true
        intent = Intent(this, TimetableDownloader::class.java)
        intent.putExtra(TimetableDownloader.EXTRA_FORCE, true)
        startService(intent)
    }

    override fun onConnectivityChange(connected: Boolean) {
        if (connected && !serviceRunning)
            downloadTimetable()
        /*if (!connected)
            serviceRunning = false*/
    }

    override fun onTimetableDownload(result: String?) {
        when (result) {
            TimetableDownloader.RESULT_DOWNLOADED -> {
                timetableDownloadReceiver.removeOnTimetableDownloadListener(this)
                networkStateReceiver.removeOnConnectivityChangeListener(this)
                startActivity(Intent(this, DashActivity::class.java))
                finish()
            }
            else -> (findViewById(R.id.noDbCaption) as TextView).text = getString(R.string.error_try_later)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(timetableDownloadReceiver)
        if (askedForNetwork)
            unregisterReceiver(networkStateReceiver)
    }
}
