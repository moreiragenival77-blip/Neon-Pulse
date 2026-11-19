package com.genival.neonpulse

import android.content.ComponentName
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : ComponentActivity() {
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private lateinit var status: TextView
    private lateinit var title: TextView
    private lateinit var seek: SeekBar
    private lateinit var play: Button
    private val cyan = Color.rgb(0, 229, 255)
    private val pink = Color.rgb(255, 40, 215)
    private val white = Color.rgb(244, 247, 255)
    private val muted = Color.rgb(155, 164, 195)

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { runCatching { contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
            controller?.setMediaItems(uris.map { MediaItem.fromUri(it) })
            controller?.prepare()
            controller?.play()
            status.text = "${uris.size} música(s) adicionada(s)"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 8, 20)
        window.navigationBarColor = Color.rgb(8, 8, 20)
        buildScreen()
        connectPlayer()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 22, 24, 24)
            setBackgroundColor(Color.rgb(8, 8, 20))
        }
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.ic_neon_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Ícone Neon Pulse"
        }
        root.addView(logo, LinearLayout.LayoutParams(150.dp(), 150.dp()).apply { bottomMargin = 12.dp() })
        val heading = TextView(this).apply {
            text = "NEON PULSE"
            textSize = 25f
            setTextColor(cyan)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.16f
        }
        root.addView(heading)
        status = TextView(this).apply {
            text = "Seu som. Sua energia."
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, 8.dp(), 0, 18.dp())
        }
        root.addView(status)
        title = TextView(this).apply {
            text = "Nenhuma música selecionada"
            textSize = 17f
            setTextColor(white)
            gravity = Gravity.CENTER
            maxLines = 2
            setPadding(8.dp(), 18.dp(), 8.dp(), 12.dp())
        }
        root.addView(title)
        seek = SeekBar(this).apply { progressTintList = android.content.res.ColorStateList.valueOf(cyan); thumbTintList = android.content.res.ColorStateList.valueOf(pink) }
        root.addView(seek, LinearLayout.LayoutParams(-1, 42.dp()))
        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
        val previous = button("⏮", cyan) { controller?.seekToPreviousMediaItem() }
        play = button("▶", pink) { togglePlayback() }
        val next = button("⏭", cyan) { controller?.seekToNextMediaItem() }
        controls.addView(previous); controls.addView(play); controls.addView(next)
        root.addView(controls)
        val add = button("＋  ADICIONAR MÚSICAS", cyan) { picker.launch(arrayOf("audio/*")) }
        root.addView(add, LinearLayout.LayoutParams(-1, 54.dp()).apply { topMargin = 24.dp() })
        val creator = TextView(this).apply {
            text = "desenvolvido por Genival"
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, 22.dp(), 0, 0)
        }
        root.addView(creator)
        setContentView(root)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) controller?.let { if (it.duration > 0) it.seekTo(it.duration * progress / 1000L) }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
    }

    private fun connectPlayer() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            runCatching {
                controller = controllerFuture?.get()
                controller?.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        title.text = mediaItem?.mediaMetadata?.title ?: mediaItem?.localConfiguration?.uri?.lastPathSegment ?: "Tocando música"
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        play.text = if (isPlaying) "Ⅱ" else "▶"
                    }
                })
                refreshProgress()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun refreshProgress() {
        val p = controller ?: return
        if (p.duration > 0) seek.progress = ((p.currentPosition * 1000L) / p.duration).toInt().coerceIn(0, 1000)
        status.text = if (p.isPlaying) "REPRODUZINDO" else "PRONTO PARA TOCAR"
        seek.postDelayed({ refreshProgress() }, 700)
    }

    private fun togglePlayback() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    private fun button(label: String, color: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 15f
            setTextColor(color)
            isAllCaps = false
            setOnClickListener { action() }
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(19, 23, 43))
            val lp = LinearLayout.LayoutParams(if (label.contains("ADICIONAR")) -1 else 64.dp(), 54.dp())
            lp.setMargins(5.dp(), 5.dp(), 5.dp(), 5.dp())
            layoutParams = lp
        }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        controller?.release()
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        super.onDestroy()
    }
}
