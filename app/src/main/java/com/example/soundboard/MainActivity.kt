package com.example.soundboard

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private data class Song(val title: String, val artist: String, val resourceId: Int, val genre: String)

    private val songs = listOf(
        Song("Rain & Thunder", "Nature Sounds", R.raw.rain_thunder, "Nature"),
        Song("Cafe Ambience", "Ambient Collection", R.raw.cafe_ambience, "Ambient"),
        Song("Deep Space", "Ambient Collection", R.raw.deep_space, "Ambient"),
        Song("Forest Birds", "Nature Sounds", R.raw.forest_birds, "Nature"),
        Song("Neon Pulse", "Echo Originals", R.raw.edm_pulse, "EDM"),
        Song("Midnight Groove", "Echo Originals", R.raw.hiphop_groove, "Hip-Hop"),
        Song("Lo-fi Evening", "Echo Originals", R.raw.lofi_evening, "Lo-fi")
    )
    private var currentIndex = 0
    private var player: MediaPlayer? = null
    private var isPaused = false
    private var selectedGenre = "All"
    private val genres = listOf("All", "EDM", "Hip-Hop", "Lo-fi")

    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var statusText: TextView
    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.currentTitle)
        artistText = findViewById(R.id.currentArtist)
        statusText = findViewById(R.id.statusText)
        playButton = findViewById(R.id.playButton)

        findViewById<Button>(R.id.songRain).setOnClickListener { playSong(0) }
        findViewById<Button>(R.id.songCafe).setOnClickListener { playSong(1) }
        findViewById<Button>(R.id.songSpace).setOnClickListener { playSong(2) }
        findViewById<Button>(R.id.songForest).setOnClickListener { playSong(3) }
        findViewById<Button>(R.id.songEdm).setOnClickListener { playSong(4) }
        findViewById<Button>(R.id.songHipHop).setOnClickListener { playSong(5) }
        findViewById<Button>(R.id.songLofi).setOnClickListener { playSong(6) }
        playButton.setOnClickListener { togglePlayback() }
        findViewById<Button>(R.id.nextButton).setOnClickListener { playSong((currentIndex + 1) % songs.size) }
        findViewById<Button>(R.id.previousButton).setOnClickListener { playSong((currentIndex - 1 + songs.size) % songs.size) }
        findViewById<Button>(R.id.stopButton).setOnClickListener { stopPlayback() }
        setupGenreTabs()
        setupSearch()
        updateTrackInfo()
    }

    private fun setupGenreTabs() {
        val container = findViewById<android.widget.LinearLayout>(R.id.genreTabs)
        for (genre in genres) {
            val tab = Button(this).apply {
                text = genre
                isAllCaps = false
                textSize = 13f
                setPadding(18, 0, 18, 0)
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    if (genre == selectedGenre) android.graphics.Color.rgb(183, 243, 107)
                    else android.graphics.Color.rgb(38, 49, 38)
                )
                setTextColor(if (genre == selectedGenre) android.graphics.Color.rgb(16, 21, 16) else android.graphics.Color.WHITE)
                minHeight = 42
                minimumHeight = 42
                stateListAnimator = null
            }
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 8
            tab.layoutParams = params
            tab.setOnClickListener {
                selectedGenre = genre
                for (i in 0 until container.childCount) {
                    val child = container.getChildAt(i) as Button
                    val active = child.text.toString() == selectedGenre
                    child.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        if (active) android.graphics.Color.rgb(183, 243, 107)
                        else android.graphics.Color.rgb(38, 49, 38)
                    )
                    child.setTextColor(if (active) android.graphics.Color.rgb(16, 21, 16) else android.graphics.Color.WHITE)
                }
                filterSongs()
            }
            container.addView(tab)
        }
    }

    private fun setupSearch() {
        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterSongs() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterSongs() {
        val query = findViewById<EditText>(R.id.searchInput).text.toString().trim().lowercase()
        val songButtons = listOf(
            Triple(findViewById<Button>(R.id.songRain), songs[0], "rain thunder nature sounds"),
            Triple(findViewById<Button>(R.id.songCafe), songs[1], "cafe ambience ambient collection"),
            Triple(findViewById<Button>(R.id.songSpace), songs[2], "deep space ambient collection"),
            Triple(findViewById<Button>(R.id.songForest), songs[3], "forest birds nature sounds"),
            Triple(findViewById<Button>(R.id.songEdm), songs[4], "neon pulse edm electronic echo originals"),
            Triple(findViewById<Button>(R.id.songHipHop), songs[5], "midnight groove hip hop hip-hop echo originals"),
            Triple(findViewById<Button>(R.id.songLofi), songs[6], "lo-fi lofi evening chill echo originals")
        )
        var visibleCount = 0
        for ((button, song, searchableText) in songButtons) {
            val genreMatches = selectedGenre == "All" || song.genre.equals(selectedGenre, ignoreCase = true)
            val queryMatches = searchableText.contains(query) || song.genre.lowercase().contains(query)
            val matches = genreMatches && queryMatches
            button.visibility = if (matches) View.VISIBLE else View.GONE
            if (matches) visibleCount++
        }
        val noResultsText = findViewById<TextView>(R.id.noResultsText)
        noResultsText.visibility = if (visibleCount == 0) View.VISIBLE else View.GONE
        noResultsText.text = "No tracks found. Try another search or genre."
    }

    private fun playSong(index: Int) {
        currentIndex = index
        releasePlayer()
        val song = songs[currentIndex]
        player = MediaPlayer.create(this, song.resourceId)
        if (player == null) {
            Toast.makeText(this, "Couldn't load this audio file", Toast.LENGTH_SHORT).show()
            return
        }
        player?.isLooping = true
        player?.start()
        isPaused = false
        playButton.text = "Ⅱ  Pause"
        statusText.text = "NOW PLAYING"
        updateTrackInfo()
    }

    private fun togglePlayback() {
        val activePlayer = player
        if (activePlayer == null) {
            playSong(currentIndex)
        } else if (activePlayer.isPlaying) {
            activePlayer.pause()
            isPaused = true
            playButton.text = "▶  Resume"
            statusText.text = "PAUSED"
        } else if (isPaused) {
            activePlayer.start()
            isPaused = false
            playButton.text = "Ⅱ  Pause"
            statusText.text = "NOW PLAYING"
        } else {
            playSong(currentIndex)
        }
    }

    private fun updateTrackInfo() {
        val song = songs[currentIndex]
        titleText.text = song.title
        artistText.text = song.artist
    }

    private fun stopPlayback() {
        releasePlayer()
        isPaused = false
        playButton.text = "▶  Play"
        statusText.text = "READY TO PLAY"
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
