package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import androidx.fragment.app.activityViewModels
import com.example.myapplication.ExerciciosViewModel


class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var txtTrack: TextView
    private lateinit var txtArtist: TextView
    private lateinit var imgAlbum: ImageView
    private val viewModel: ExerciciosViewModel by activityViewModels()

    companion object {
        private const val CLIENT_ID = "8e63a9c9aeae49829d445dd8e33482d5"
        private const val REDIRECT_URI = "com.example.myapplication://callback"
        private const val REQUEST_CODE = 1337
    }

    private var spotifyAppRemote: SpotifyAppRemote? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.listaExercicios)

        val series = 4
        val repeticoes = "8-10"
        val descanso = "60s"

        val btnPrev = view.findViewById<Button>(R.id.btnPrev)
        val btnPause = view.findViewById<Button>(R.id.btnPause)
        val btnNext = view.findViewById<Button>(R.id.btnNext)

        if (viewModel.listaExercicios.value.isNullOrEmpty()) {

            val lista = mutableListOf<Exercicio>()

            lista.add(
                Exercicio(
                    "Supino reto",
                    "$series séries",
                    "$repeticoes repetições",
                    "$descanso descanso",
                    R.drawable.supino
                )
            )

            lista.add(
                Exercicio(
                    "Supino inclinado",
                    "$series séries",
                    "$repeticoes repetições",
                    "$descanso descanso",
                    R.drawable.supino_inclinado
                )
            )

            lista.add(
                Exercicio(
                    "Cross Over",
                    "4 séries",
                    "10 repetições",
                    "$descanso descanso",
                    R.drawable.cross_over
                )
            )

            lista.add(
                Exercicio(
                    "Tríceps máquina",
                    "4 séries",
                    "8 repetições",
                    "$descanso descanso",
                    R.drawable.triceps_maquina
                )
            )

            viewModel.listaExercicios.value = lista
        }

        viewModel.listaExercicios.postValue(viewModel.listaExercicios.value)

        txtTrack = view.findViewById(R.id.txtTrack)
        txtArtist = view.findViewById(R.id.txtArtist)
        imgAlbum = view.findViewById(R.id.imgAlbum)

        connectSpotify()

        btnPrev.setOnClickListener {
            spotifyAppRemote?.playerApi?.skipPrevious()
        }

        btnPause.setOnClickListener {
            spotifyAppRemote?.playerApi?.pause()
        }

        btnNext.setOnClickListener {
            spotifyAppRemote?.playerApi?.skipNext()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ExercicioAdapter(mutableListOf())
        recycler.adapter = adapter

        viewModel.listaExercicios.observe(viewLifecycleOwner) { lista ->
            adapter.atualizarLista(lista)
        }
    }

    private fun connectSpotify() {

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            requireActivity(), connectionParams,
            object : Connector.ConnectionListener {

                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("SPOTIFY", "Conectado!")

                    spotifyAppRemote?.playerApi?.play("spotify:playlist:37i9dQZF1DX70RN3TfWWJh")

                    spotifyAppRemote?.playerApi
                        ?.subscribeToPlayerState()
                        ?.setEventCallback { playerState ->

                            val track = playerState.track

                            if (track != null) {

                                txtTrack.text = track.name
                                txtArtist.text = track.artist.name

                                spotifyAppRemote?.imagesApi
                                    ?.getImage(track.imageUri)
                                    ?.setResultCallback { bitmap ->
                                        imgAlbum.setImageBitmap(bitmap)
                                    }
                            }
                        }
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("SPOTIFY", "Erro", throwable)
                }
            })
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }
}