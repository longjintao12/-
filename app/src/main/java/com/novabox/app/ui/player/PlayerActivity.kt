package com.novabox.app.ui.player

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.GridLayoutManager
import com.novabox.app.R
import com.novabox.app.data.db.AppDbHelper
import com.novabox.app.data.model.PlayUrl
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.data.repo.VodRepo
import com.novabox.app.databinding.ActivityPlayerBinding
import com.novabox.app.net.ApiParser
import com.novabox.app.util.Prefs
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var b: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var sourceId = ""
    private var vodId = ""
    private var vodName = ""
    private var playUrl = ""
    private var playIndex = 0
    private var episodes = listOf<PlayUrl>()
    private var currentLine = 0
    private var detail: com.novabox.app.data.model.VodDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        b = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(b.root)

        sourceId = intent.getStringExtra("sourceId") ?: ""
        vodId = intent.getStringExtra("vodId") ?: ""
        vodName = intent.getStringExtra("name") ?: ""
        playUrl = intent.getStringExtra("playUrl") ?: ""
        playIndex = intent.getIntExtra("playIndex", 0)

        b.vodTitle.text = vodName
        b.episodes.layoutManager = GridLayoutManager(this, 5)

        setupPlayer()
        loadDetail()

        b.btnSpeed.setOnClickListener { showSpeedDialog() }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { p ->
                b.playerView.player = p
                p.setMediaItem(MediaItem.fromUri(playUrl))
                p.prepare()
                p.playWhenReady = true
                p.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED && Prefs.autoPlay) {
                            playNext()
                        }
                    }
                })
            }

        b.playerView.setOnTouchListener { _, event -> handleGesture(event) }
    }

    private fun handleGesture(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            b.panel.visibility = if (b.panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        return false
    }

    private fun loadDetail() {
        lifecycleScope.launch {
            try {
                val src = SourceRepo.load().find { it.id == sourceId }
                if (src != null) {
                    detail = VodRepo.fetchDetail(src, vodId)
                    detail?.let { d ->
                        b.tabLine.removeAllTabs()
                        for (line in d.vodPlayFrom) {
                            b.tabLine.addTab(b.tabLine.newTab().setText(line))
                        }
                        if (d.vodPlayUrl.isNotEmpty()) {
                            episodes = ApiParser.parseEpisodes(d.vodPlayUrl.getOrElse(currentLine) { "" })
                            updateEpisodeList()
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun updateEpisodeList() {
        b.episodes.adapter = com.novabox.app.ui.detail.DetailActivity.EpisodeAdapter { ep ->
            playUrl = ep.url
            playIndex = episodes.indexOf(ep)
            player?.setMediaItem(MediaItem.fromUri(ep.url))
            player?.prepare()
            player?.playWhenReady = true
            saveHistory()
        }
        (b.episodes.adapter as? androidx.recyclerview.widget.ListAdapter<*, *>)?.let {
            @Suppress("UNCHECKED_CAST")
            (it as androidx.recyclerview.widget.ListAdapter<PlayUrl, *>).submitList(episodes)
        }
    }

    private fun playNext() {
        if (playIndex + 1 < episodes.size) {
            playIndex++
            playUrl = episodes[playIndex].url
            player?.setMediaItem(MediaItem.fromUri(playUrl))
            player?.prepare()
            player?.playWhenReady = true
            saveHistory()
        }
    }

    private fun saveHistory() {
        val db = AppDbHelper.get(this)
        db.addHistory(vodId, sourceId, vodName, "",
            detail?.vodPlayFrom?.getOrElse(currentLine) { "" } ?: "", playIndex, 0)
    }

    private fun showSpeedDialog() {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val items = speeds.map { "${it}x" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("播放速度")
            .setItems(items) { _, i ->
                player?.setPlaybackSpeed(speeds[i])
                b.btnSpeed.text = "${speeds[i]}x"
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        saveHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
