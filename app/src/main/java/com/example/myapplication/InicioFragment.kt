package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.produto.CadastroProdutoActivity
import com.example.myapplication.repository.UsuarioRepository
import com.example.myapplication.ui.exercicio.CadastroExercicioActivity
import com.example.myapplication.ui.treino.CadastroTreinoActivity
import com.example.myapplication.ui.usuario.CadastroUsuarioActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val CLIENT_ID = "8e63a9c9aeae49829d445dd8e33482d5"
    private val REDIRECT_URI = "com.example.myapplication://callback"

    private lateinit var cardSpotify: CardView
    private lateinit var ivAlbumArt: ImageView
    private lateinit var tvTrackName: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var progressTrack: ProgressBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvSpotifyOpen: TextView

    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private var trackDurationMs: Long = 0L
    private var trackPositionMs: Long = 0L

    private val progressUpdater = object : Runnable {
        override fun run() {
            if (isPlaying && trackDurationMs > 0) {
                trackPositionMs += 1000
                val progress = ((trackPositionMs.toFloat() / trackDurationMs) * 100).toInt()
                progressTrack.progress = progress.coerceIn(0, 100)
                tvCurrentTime.text = formatMillis(trackPositionMs)
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        carregarNomeUsuario(view)
        setupQuickAccessButtons(view)
        setupSpotifyControls()
        setupAdminView(view)
    }

    override fun onStart() {
        super.onStart()
        connectSpotify()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(progressUpdater)
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        spotifyAppRemote = null
    }

    private fun bindViews(view: View) {
        cardSpotify    = view.findViewById(R.id.cardSpotify)
        ivAlbumArt     = view.findViewById(R.id.ivAlbumArt)
        tvTrackName    = view.findViewById(R.id.tvTrackName)
        tvArtistName   = view.findViewById(R.id.tvArtistName)
        btnPlayPause   = view.findViewById(R.id.btnPlayPause)
        btnPrevious    = view.findViewById(R.id.btnPrevious)
        btnNext        = view.findViewById(R.id.btnNext)
        progressTrack  = view.findViewById(R.id.progressTrack)
        tvCurrentTime  = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime    = view.findViewById(R.id.tvTotalTime)
        tvSpotifyOpen  = view.findViewById(R.id.tvSpotifyOpen)
    }

    private fun connectSpotify() {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            requireContext(),
            connectionParams,
            object : Connector.ConnectionListener {

                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    subscribeToPlayerState()
                    handler.post(progressUpdater)
                }

                override fun onFailure(throwable: Throwable) {
                    showSpotifyDisconnected()
                }
            }
        )
    }

    private fun subscribeToPlayerState() {
        spotifyAppRemote?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback { playerState: PlayerState ->
                requireActivity().runOnUiThread {
                    updatePlayerUI(playerState)
                }
            }
    }

    private fun updatePlayerUI(playerState: PlayerState) {
        val track: Track? = playerState.track ?: return

        isPlaying       = !playerState.isPaused
        trackDurationMs = track!!.duration
        trackPositionMs = playerState.playbackPosition

        tvTrackName.text  = track.name
        tvArtistName.text = track.artist.name

        val progress = if (trackDurationMs > 0)
            ((trackPositionMs.toFloat() / trackDurationMs) * 100).toInt()
        else 0
        progressTrack.progress = progress.coerceIn(0, 100)
        tvCurrentTime.text = formatMillis(trackPositionMs)
        tvTotalTime.text   = formatMillis(trackDurationMs)

        track.imageUri?.let { uri ->
            spotifyAppRemote?.imagesApi
                ?.getImage(uri)
                ?.setResultCallback { bitmap ->
                    requireActivity().runOnUiThread {
                        ivAlbumArt.setImageBitmap(bitmap)
                    }
                }
        }
    }

    private fun showSpotifyDisconnected() {
        tvTrackName.text  = "Spotify não conectado"
        tvArtistName.text = "Abra o app para conectar"
        btnPlayPause.isEnabled = false
        btnPrevious.isEnabled  = false
        btnNext.isEnabled      = false
    }

    private fun setupSpotifyControls() {
        btnPlayPause.setOnClickListener {
            val remote = spotifyAppRemote ?: return@setOnClickListener
            if (isPlaying) {
                remote.playerApi.pause()
            } else {
                remote.playerApi.resume()
            }
        }

        btnPrevious.setOnClickListener {
            spotifyAppRemote?.playerApi?.skipPrevious()
        }

        btnNext.setOnClickListener {
            spotifyAppRemote?.playerApi?.skipNext()
        }

        val openSpotify = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music"))
                try {
                    startActivity(storeIntent)
                } catch (e2: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(requireContext(), "Spotify não está instalado", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvSpotifyOpen.setOnClickListener { openSpotify() }
        cardSpotify.setOnClickListener { openSpotify() }
    }

    private fun carregarNomeUsuario(view: View) {
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        lifecycleScope.launch {
            val result = UsuarioRepository().buscarUsuarioAtual()
            if (result is com.example.myapplication.util.Resource.Success) {
                val primeiroNome = result.data.nome.trim().split(" ").firstOrNull() ?: result.data.nome
                tvUserName.text = "Olá, $primeiroNome!"
            }
        }
    }

    private fun setupAdminView(view: View) {
        val sectionAdmin   = view.findViewById<View>(R.id.sectionAdminInicio)
        val labelTreino    = view.findViewById<View>(R.id.labelTreinoHoje)
        val cardTreino     = view.findViewById<View>(R.id.cardTreinoHoje)
        val labelMusica    = view.findViewById<View>(R.id.labelMusica)

        lifecycleScope.launch {
            val isAdmin = UsuarioRepository().usuarioAtualEhAdmin()
            if (isAdmin) {
                sectionAdmin.visibility = View.VISIBLE
            } else {
                labelTreino.visibility = View.VISIBLE
                cardTreino.visibility  = View.VISIBLE
                labelMusica.visibility = View.VISIBLE
                cardSpotify.visibility = View.VISIBLE
            }
        }

        view.findViewById<View>(R.id.adminBtnProduto).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroProdutoActivity::class.java))
        }
        view.findViewById<View>(R.id.adminBtnExercicio).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroExercicioActivity::class.java))
        }
        view.findViewById<View>(R.id.adminBtnTreino).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroTreinoActivity::class.java))
        }
        view.findViewById<View>(R.id.adminBtnUsuario).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroUsuarioActivity::class.java))
        }
    }

    private fun setupQuickAccessButtons(view: View) {
        view.findViewById<View>(R.id.btnHistorico).setOnClickListener {
            Toast.makeText(requireContext(), "Histórico", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnEvolucao).setOnClickListener {
            Toast.makeText(requireContext(), "Evolução", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnDesafios).setOnClickListener {
            Toast.makeText(requireContext(), "Desafios", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnEstatisticas).setOnClickListener {
            Toast.makeText(requireContext(), "Estatísticas", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnContinuar).setOnClickListener {
            Toast.makeText(requireContext(), "Continuar treino", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatMillis(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }
}
