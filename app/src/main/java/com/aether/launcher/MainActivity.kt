package com.aether.launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val bg = Color.rgb(7, 11, 9)
    private val panel = Color.rgb(17, 25, 21)
    private val green = Color.rgb(0, 255, 145)
    private val white = Color.WHITE
    private val muted = Color.rgb(145, 165, 154)

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var root: LinearLayout
    private lateinit var versionLabel: TextView
    private lateinit var nameLabel: TextView
    private lateinit var avatar: TextView

    private var selectedVersion = "1.21.4"
    private var playerName = "Player"
    private var downloading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val prefs = getSharedPreferences("aether_settings", MODE_PRIVATE)
        selectedVersion = prefs.getString("selected_version", "1.21.4") ?: "1.21.4"
        playerName = prefs.getString("player_name", "Player") ?: "Player"

        buildLauncher()
    }

    private fun buildLauncher() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(bg)
        }

        setContentView(root)

        // LEFT SIDEBAR

        val sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(18), dp(12), dp(12))
            setBackgroundColor(panel)
        }

        root.addView(
            sidebar,
            LinearLayout.LayoutParams(dp(155), -1)
        )

        val logo = text("AETHER", 23f, green, true)
        logo.gravity = Gravity.CENTER

        sidebar.addView(logo, LinearLayout.LayoutParams(-1, dp(52)))

        sidebar.addView(sidebarButton("⌂   HOME") {
            buildLauncher()
        })

        sidebar.addView(sidebarButton("▤   VERSIONS") {
            showVersions()
        })

        sidebar.addView(sidebarButton("⚙   SETTINGS") {
            showSettings()
        })

        val sideSpacer = View(this)
        sidebar.addView(
            sideSpacer,
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        sidebar.addView(
            text("JAVA EDITION", 10f, muted, true).apply {
                gravity = Gravity.CENTER
            }
        )

        sidebar.addView(
            text("AETHER v1.0", 11f, green, true).apply {
                gravity = Gravity.CENTER
            }
        )

        // CENTER PANEL

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(15), dp(18), dp(12))
        }

        root.addView(
            center,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        topBar.addView(
            text("GAME LAUNCHER", 15f, white, true),
            LinearLayout.LayoutParams(0, dp(35), 1f)
        )

        topBar.addView(
            text("● AETHER READY", 10f, green, true)
        )

        center.addView(topBar)

        addGap(center, 10)

        // HERO CARD

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(14), dp(18), dp(14))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(14, 40, 28),
                    Color.rgb(7, 15, 11),
                    Color.rgb(16, 32, 23)
                )
            ).apply {
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), Color.rgb(28, 80, 52))
            }
        }

        center.addView(
            hero,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        hero.addView(text("AETHER LAUNCHER", 12f, green, true))

        addGap(hero, 8)

        hero.addView(text("YOUR WORLD.", 29f, white, true))
        hero.addView(text("YOUR ADVENTURE.", 29f, green, true))

        addGap(hero, 5)

        hero.addView(
            text(
                "Explore. Build. Survive.\nMinecraft Java Edition.",
                13f,
                muted,
                false
            )
        )

        addGap(hero, 12)

        val playButton = Button(this).apply {
            text = "▶   PLAY MINECRAFT"
            textSize = 15f
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            background = rounded(green, 12)

            setOnClickListener {
                showPlayInfo()
            }
        }

        hero.addView(
            playButton,
            LinearLayout.LayoutParams(dp(220), dp(49))
        )

        addGap(hero, 8)

        val versionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        versionRow.addView(
            text("Selected Version: ", 12f, muted, false)
        )

        versionLabel = text(selectedVersion, 13f, green, true)
        versionLabel.setOnClickListener { showVersions() }
        versionRow.addView(versionLabel)

        val change = text("   ▼", 12f, green, true)
        change.setOnClickListener { showVersions() }
        versionRow.addView(change)

        hero.addView(versionRow)

        // BOTTOM CARDS

        addGap(center, 10)

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        center.addView(
            bottomRow,
            LinearLayout.LayoutParams(-1, dp(105))
        )

        val newsCard = miniCard(
            "LATEST NEWS",
            "Minecraft $selectedVersion\nSelect a version to download its client."
        )

        bottomRow.addView(
            newsCard,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        val spacer = View(this)
        bottomRow.addView(
            spacer,
            LinearLayout.LayoutParams(dp(10), 1)
        )

        val actionsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(8))
            background = rounded(panel, 13)
        }

        bottomRow.addView(
            actionsCard,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        actionsCard.addView(
            text("QUICK ACTIONS", 12f, green, true)
        )

        addGap(actionsCard, 5)

        actionsCard.addView(
            smallAction("⬇  Download Client") {
                downloadClient()
            }
        )

        actionsCard.addView(
            smallAction("▤  View Versions") {
                showVersions()
            }
        )

        // RIGHT PROFILE PANEL

        val profile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(22), dp(15), dp(14))
            setBackgroundColor(panel)
        }

        root.addView(
            profile,
            LinearLayout.LayoutParams(dp(180), -1)
        )

        profile.addView(
            text("PLAYER PROFILE", 12f, green, true)
        )

        addGap(profile, 15)

        avatar = text(
            playerName.take(1).uppercase(),
            34f,
            green,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(29, 52, 40), 45)
        }

        profile.addView(
            avatar,
            LinearLayout.LayoutParams(dp(68), dp(68))
        )

        addGap(profile, 8)

        nameLabel = text(playerName, 18f, white, true)
        profile.addView(nameLabel)

        addGap(profile, 3)

        profile.addView(
            text("● Offline Profile", 11f, muted, false)
        )

        addGap(profile, 10)

        profile.addView(
            smallAction("Edit Player Name") {
                showSettings()
            }
        )

        addGap(profile, 12)
        profile.addView(divider())
        addGap(profile, 12)

        profile.addView(
            text("SELECTED VERSION", 10f, muted, true)
        )

        addGap(profile, 6)

        val profileVersion = text(selectedVersion, 20f, green, true)
        profile.addView(profileVersion)

        addGap(profile, 8)

        profile.addView(
            smallAction("Change Version") {
                showVersions()
            }
        )

        addGap(profile, 12)
        profile.addView(divider())
        addGap(profile, 12)

        profile.addView(
            text("LAUNCHER INFO", 10f, green, true)
        )

        addGap(profile, 7)

        profile.addView(
            text("AETHER v1.0", 12f, white, true)
        )

        profile.addView(
            text("Minecraft Java Edition", 10f, muted, false)
        )

        val profileSpacer = View(this)

        profile.addView(
            profileSpacer,
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        profile.addView(
            text("BUILT FOR EXPLORERS", 9f, green, true)
        )
    }

    private fun showPlayInfo() {
        AlertDialog.Builder(this)
            .setTitle("AETHER PLAY")
            .setMessage(
                "Selected version: $selectedVersion\n\n" +
                "The horizontal launcher interface is ready, " +
                "but actual Minecraft launching is not implemented yet.\n\n" +
                "You can download the official client JAR. " +
                "A complete game installation, Java runtime, " +
                "libraries, assets and launch engine are still required."
            )
            .setPositiveButton("DOWNLOAD CLIENT") { _, _ ->
                downloadClient()
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showVersions() {
        Toast.makeText(
            this,
            "Loading Minecraft versions...",
            Toast.LENGTH_SHORT
        ).show()

        executor.execute {
            try {
                val connection = URL(
                    "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
                ).openConnection() as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val data = connection.inputStream.bufferedReader().use {
                    it.readText()
                }

                connection.disconnect()

                val array = JSONObject(data).getJSONArray("versions")
                val releases = mutableListOf<String>()

                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)

                    if (item.optString("type") == "release") {
                        releases.add(item.getString("id"))
                    }
                }

                handler.post {
                    if (isFinishing) return@post

                    val choices = releases.take(100).toTypedArray()

                    AlertDialog.Builder(this)
                        .setTitle("Select Minecraft Version")
                        .setItems(choices) { _, index ->
                            selectedVersion = choices[index]

                            getSharedPreferences(
                                "aether_settings",
                                MODE_PRIVATE
                            ).edit()
                                .putString("selected_version", selectedVersion)
                                .apply()

                            buildLauncher()

                            Toast.makeText(
                                this,
                                "Selected $selectedVersion",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
            } catch (e: Exception) {
                handler.post {
                    if (!isFinishing) {
                        message("Unable to load versions. Check your internet.")
                    }
                }
            }
        }
    }

    private fun downloadClient() {
        if (downloading) {
            message("A download is already running.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Download Minecraft $selectedVersion?")
            .setMessage(
                "Download the official Minecraft client JAR " +
                "into AETHER's private storage?"
            )
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("DOWNLOAD") { _, _ ->
                beginDownload(selectedVersion)
            }
            .show()
    }

    private fun beginDownload(version: String) {
        if (downloading) return

        downloading = true

        val dialog = ProgressDialog(this).apply {
            setTitle("AETHER DOWNLOAD")
            setMessage("Preparing download...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            progress = 0
            setCancelable(false)
            show()
        }

        executor.execute {
            var temporary: File? = null

            try {
                val manifestConnection = URL(
                    "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
                ).openConnection() as HttpURLConnection

                manifestConnection.connectTimeout = 15000
                manifestConnection.readTimeout = 15000

                val manifestText =
                    manifestConnection.inputStream.bufferedReader().use {
                        it.readText()
                    }

                manifestConnection.disconnect()

                val manifest = JSONObject(manifestText)
                val versions = manifest.getJSONArray("versions")
                var versionUrl: String? = null

                for (i in 0 until versions.length()) {
                    val item = versions.getJSONObject(i)

                    if (item.getString("id") == version) {
                        versionUrl = item.getString("url")
                        break
                    }
                }

                if (versionUrl == null) {
                    throw Exception("Minecraft version not found.")
                }

                val detailsConnection = URL(versionUrl)
                    .openConnection() as HttpURLConnection

                detailsConnection.connectTimeout = 15000
                detailsConnection.readTimeout = 15000

                val detailsText =
                    detailsConnection.inputStream.bufferedReader().use {
                        it.readText()
                    }

                detailsConnection.disconnect()

                val details = JSONObject(detailsText)
                val client = details
                    .getJSONObject("downloads")
                    .getJSONObject("client")

                val downloadUrl = client.getString("url")
                val expectedSha1 = client.optString("sha1", "")

                val directory = File(
                    filesDir,
                    "minecraft/versions/$version"
                )

                if (!directory.exists() && !directory.mkdirs()) {
                    throw Exception("Could not create download folder.")
                }

                val destination = File(directory, "$version.jar")
                temporary = File(directory, "$version.jar.part")

                val connection = URL(downloadUrl)
                    .openConnection() as HttpURLConnection

                connection.connectTimeout = 20000
                connection.readTimeout = 30000
                connection.connect()

                val total = connection.contentLengthLong
                var downloaded = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(temporary).use { output ->
                        val buffer = ByteArray(8192)
                        var count: Int

                        while (
                            input.read(buffer).also { count = it } != -1
                        ) {
                            output.write(buffer, 0, count)
                            downloaded += count

                            if (total > 0) {
                                val percent =
                                    (downloaded * 100 / total)
                                        .toInt()
                                        .coerceIn(0, 100)

                                handler.post {
                                    if (!isFinishing) {
                                        dialog.progress = percent
                                        dialog.setMessage(
                                            "Downloading $version..."
                                        )
                                    }
                                }
                            }
                        }

                        output.flush()
                    }
                }

                connection.disconnect()

                if (!temporary.exists() || temporary.length() == 0L) {
                    throw Exception("Downloaded file is empty.")
                }

                if (expectedSha1.isNotBlank()) {
                    handler.post {
                        dialog.setMessage("Verifying download...")
                    }

                    val actualSha1 = calculateSha1(temporary)

                    if (!actualSha1.equals(expectedSha1, true)) {
                        throw Exception("SHA-1 verification failed.")
                    }
                }

                if (destination.exists() && !destination.delete()) {
                    throw Exception("Could not replace old client file.")
                }

                if (!temporary.renameTo(destination)) {
                    temporary.copyTo(destination, overwrite = true)
                    temporary.delete()
                }

                handler.post {
                    if (!isFinishing) {
                        dialog.dismiss()

                        message(
                            "Minecraft $version client downloaded successfully!\n\n" +
                            "Saved in AETHER private storage.\n\n" +
                            "This is only the client JAR, not a complete playable installation."
                        )
                    }

                    downloading = false
                }

            } catch (e: Exception) {
                temporary?.delete()

                handler.post {
                    if (!isFinishing) {
                        dialog.dismiss()
                        message(
                            "Download failed:\n${e.message ?: "Unknown error"}"
                        )
               
