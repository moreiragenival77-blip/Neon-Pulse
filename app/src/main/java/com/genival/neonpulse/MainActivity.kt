package com.genival.neonpulse

import android.content.ComponentName
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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
    private lateinit var songsContainer: LinearLayout
    private val selectedUris = mutableListOf<Uri>()
    private val cyan = Color.rgb(0, 229, 255)
    private val pink = Color.rgb(255, 40, 215)
    private val white = Color.rgb(244, 247, 255)
    private val muted = Color.rgb(155, 164, 195)

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                runCatching { contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                if (!selectedUris.contains(uri)) selectedUris.add(uri)
            }
            renderSongList()
            val player = controller
            if (player != null) {
                player.setMediaItems(selectedUris.map { MediaItem.fromUri(it) })
                player.prepare()
                player.play()
                status.text = "${selectedUris.size} música(s) na lista"
            } else {
                status.text = "Músicas adicionadas. Conectando ao player..."
            }
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
            setPadding(24, 22, 24, 16)
            setBackgroundColor(Color.rgb(8, 8, 20))
        }
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.ic_neon_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Ícone Neon Pulse"
        }
        root.addView(logo, LinearLayout.LayoutParams(112.dp(), 112.dp()).apply { bottomMargin = 8.dp() })
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
            setPadding(0, 8.dp(), 0, 12.dp())
        }
        root.addView(status)
        title = TextView(this).apply {
            text = "Nenhuma música selecionada"
            textSize = 16f
            setTextColor(white)
            gravity = Gravity.CENTER
            maxLines = 2
            setPadding(8.dp(), 8.dp(), 8.dp(), 4.dp())
        }
        root.addView(title)
        seek = SeekBar(this).apply {
            progressTintList = android.content.res.ColorStateList.valueOf(cyan)
            thumbTintList = android.content.res.ColorStateList.valueOf(pink)
        }
        root.addView(seek, LinearLayout.LayoutParams(-1, 36.dp()))
        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
        val previous = button("⏮", cyan) { controller?.seekToPreviousMediaItem() }
        play = button("▶", pink) { togglePlayback() }
        val next = button("⏭", cyan) { controller?.seekToNextMediaItem() }
        controls.addView(previous)
        controls.addView(play)
        controls.addView(next)
        root.addView(controls)

        val add = button("＋  ADICIONAR MÚSICAS", cyan) { picker.launch(arrayOf("audio/*")) }
        root.addView(add, LinearLayout.LayoutParams(-1, 50.dp()).apply { topMargin = 8.dp(); bottomMargin = 8.dp() })

        val listHeading = TextView(this).apply {
            text = "MINHAS MÚSICAS"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setPadding(4.dp(), 6.dp(), 4.dp(), 6.dp())
        }
        root.addView(listHeading)
        songsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 4.dp())
        }
        val songsScroll = ScrollView(this).apply {
            isFillViewport = false
            addView(songsContainer)
        }
        root.addView(songsScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val creator = TextView(this).apply {
            text = "desenvolvido por Genival"
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, 8.dp(), 0, 0)
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
        renderSongList()
    }

    private fun renderSongList() {
        if (!::songsContainer.isInitialized) return
        songsContainer.removeAllViews()
        if (selectedUris.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Toque em “Adicionar músicas” para montar sua lista."
                textSize = 13f
                setTextColor(muted)
                setPadding(8.dp(), 12.dp(), 8.dp(), 12.dp())
            }
            songsContainer.addView(empty)
            return
        }
        selectedUris.forEachIndexed { index, uri ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(10.dp(), 4.dp(), 6.dp(), 4.dp())
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(18, 22, 42))
                    cornerRadius = 10.dp().toFloat()
                }
            }
            val name = TextView(this).apply {
                text = displayName(uri)
                textSize = 13f
                setTextColor(white)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(4.dp(), 8.dp(), 4.dp(), 8.dp())
            }
            row.addView(name, LinearLayout.LayoutParams(0, -2, 1f))
            val playTrack = TextView(this).apply {
                text = "▶"
                textSize = 18f
                setTextColor(cyan)
                gravity = Gravity.CENTER
                setPadding(14.dp(), 8.dp(), 10.dp(), 8.dp())
                setOnClickListener {
                    controller?.let { player ->
                        player.seekTo(index, 0L)
                        player.play()
                    } ?: run { status.text = "Aguarde a conexão do player." }
                }
            }
            row.addView(playTrack)
            row.setOnClickListener {
                controller?.let { player ->
                    player.seekTo(index, 0L)
                    player.play()
                }
            }
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = 6.dp()
            songsContainer.addView(row, params)
        }
    }

    private fun displayName(uri: Uri): String {
        var name: String? = null
        val cursor: Cursor? = runCatching { contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null) }.getOrNull()
        cursor?.use {
            if (it.moveToFirst()) {
                val column = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (column >= 0) name = it.getString(column)
            }
        }
        return name ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Faixa de áudio"
    }

    private fun connectPlayer() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            runCatching {
                controller = controllerFuture?.get()
                controller?.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        title.text = mediaItem?.mediaMetadata?.title ?: mediaItem?.localConfiguration?.uri?.let { displayName(it) } ?: "Tocando música"
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        play.text = if (isPlaying) "Ⅱ" else "▶"
                    }
                })
                if (selectedUris.isNotEmpty() && controller?.mediaItemCount == 0) {
                    controller?.setMediaItems(selectedUris.map { MediaItem.fromUri(it) })
                    controller?.prepare()
                }
                refreshProgress()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun refreshProgress() {
        val p = controller ?: return
        if (p.duration > 0) seek.progress = ((p.currentPosition * 1000L) / p.duration).toInt().coerceIn(0, 1000)
        status.text = if (p.isPlaying) "REPRODUZINDO" else if (selectedUris.isNotEmpty()) "${selectedUris.size} música(s) na lista" else "PRONTO PARA TOCAR"
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
            val lp = LinearLayout.LayoutParams(if (label.contains("ADICIONAR")) -1 else 64.dp(), 50.dp())
            lp.setMargins(5.dp(), 4.dp(), 5.dp(), 4.dp())
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
