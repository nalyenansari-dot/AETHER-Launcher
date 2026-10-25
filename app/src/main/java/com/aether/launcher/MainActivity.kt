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

    private val bg = Color.parseColor("#041d1b")
    private val panel = Color.parseColor("#061f1f")
    private val green = Color.parseColor("#4AFFA0")
    private val white = Color.WHITE
    private val muted = Color.parseColor("#91A59A")

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

        val sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)
            setPadding(dp(14), dp(18), dp(14), dp(12))
        }
        root.addView(sidebar, LinearLayout.LayoutParams(dp(210), -1))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(12))
        }

        val logoBox = TextView(this).apply {
            text = "A"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
            gravity = Gravity.CENTER
            background = roundedBg(green, 12)
            layoutParams = LinearLayout.LayoutParams(dp(38), dp(38))
        }
        brand.addView(logoBox)
        brand.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
                addView(text("AETHER", 26f, green, true))
                addView(text("Minecraft Launcher", 11f, muted, false))
            },
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        sidebar.addView(brand, LinearLayout.LayoutParams(-1, dp(70)))

        sidebar.addView(
            View(this).apply { setBackgroundColor(Color.parseColor("#0d3432")) },
            LinearLayout.LayoutParams(-1, dp(1))
        )
        addGap(sidebar, 20)

        sidebar.addView(sidebarButton("⌂   HOME") { buildLauncher() })
        sidebar.addView(sidebarButton("▤   VERSIONS") { showVersions() })
        sidebar.addView(sidebarButton("⚙   SETTINGS") { showSettings() })

        val spac = View(this)
        sidebar.addView(spac, LinearLayout.LayoutParams(1, 0, 1f))

        sidebar.addView(text("JAVA EDITION", 10f, muted, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        })
        sidebar.addView(text("AETHER v1.0", 12f, green, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(6))
        })

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(14))
        }
        root.addView(center, LinearLayout.LayoutParams(0, -1, 1f))

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        topBar.addView(text("", 1f, white, false), LinearLayout.LayoutParams(0, 0, 1f))

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val dot = View(this).apply {
            setBackgroundColor(green)
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply { rightMargin = dp(8) }
        }
        status.addView(dot)
        status.addView(text("AETHER READY", 10f, green, true))
        topBar.addView(status)
        center.addView(topBar, LinearLayout.LayoutParams(-1, dp(26)))

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0a2b2a"))
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), Color.parseColor("#1d5d50"))
            }
        }
        center.addView(hero, LinearLayout.LayoutParams(-1, dp(280)))

        val heroText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, dp(16), 0)
        }
        heroText.addView(text("AETHER LAUNCHER", 13f, green, true))
        addGap(heroText, 10)
        heroText.addView(text("YOUR WORLD.", 36f, white, true))
        heroText.addView(text("YOUR ADVENTURE.", 36f, green, true))
        addGap(heroText, 10)
        heroText.addView(text("Explore. Build. Survive.\nMinecraft Java Edition.", 14f, muted, false))
        addGap(heroText, 16)

        val playButton = Button(this).apply {
            text = "▶  PLAY"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setTextColor(Color.BLACK)
            background = rounded(green, 14)
            setOnClickListener { showPlayInfo() }
        }
        heroText.addView(playButton, LinearLayout.LayoutParams(dp(220), dp(54)))
        addGap(heroText, 12)

        val versionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        versionRow.addView(text("Selected Version: ", 12f, muted, false))
        versionLabel = text(selectedVersion, 13f, green, true)
        versionLabel.setOnClickListener { showVersions() }
        versionRow.addView(versionLabel)
        versionRow.addView(text("  ▼", 12f, green, true))
        heroText.addView(versionRow)

        hero.addView(heroText, LinearLayout.LayoutParams(0, -1, 1f))

        val scene = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#efc9ae"),
                    Color.parseColor("#a5d7d7"),
                    Color.parseColor("#143e38")
                )
            ).apply { cornerRadius = dp(18).toFloat() }
        }

        val sceneBanner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            setPadding(dp(12), 0, dp(12), dp(8))
        }
        val mcText = text("MINECRAFT\nJAVA EDITION", 18f, white, true)
        mcText.gravity = Gravity.CENTER
        mcText.setShadowLayer(8f, 0f, 0f, Color.argb(100, 0, 0, 0))
        sceneBanner.addView(mcText)
        scene.addView(sceneBanner, LinearLayout.LayoutParams(-1, -1))
        hero.addView(scene, LinearLayout.LayoutParams(dp(260), -1))

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        center.addView(bottomRow, LinearLayout.LayoutParams(-1, dp(150)))

        val news = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(panel, 15)
        }
        val newsHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        newsHeader.addView(text("📰", 14f, green, true))
        newsHeader.addView(text(" Latest News", 16f, green, true))
        val viewAll = text("View All →", 11f, muted, false)
        viewAll.gravity = Gravity.END
        newsHeader.addView(viewAll, LinearLayout.LayoutParams(0, -2, 1f))
        news.addView(newsHeader)

        addGap(news, 10)

        val thumb = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#190d2a"))
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        thumb.addView(text("Minecraft 1.21.4", 14f, white, true))
        thumb.addView(text("The latest release brings new features, fixes and improvements to your world.", 12f, muted, false))
        thumb.addView(text("Apr 05, 2025", 10f, green, true))
        news.addView(thumb, LinearLayout.LayoutParams(-1, 0, 1f))

        bottomRow.addView(news, LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = dp(12) })

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(panel, 15)
        }
        val actionHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        actionHeader.addView(text("⚡", 15f, green, true))
        actionHeader.addView(text(" Quick Actions", 16f, green, true))
        actions.addView(actionHeader)

        addGap(actions, 10)
        actions.addView(smallAction("⬇  Download Client") { downloadClient() })
        actions.addView(smallAction("▤  View All Versions") { showVersions() })
        bottomRow.addView(actions, LinearLayout.LayoutParams(0, -1, 1f))

        val profile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(panel)
        }
        root.addView(profile, LinearLayout.LayoutParams(dp(220), -1))

        profile.addView(text("PLAYER PROFILE", 12f, green, true).apply { gravity = Gravity.CENTER })
        addGap(profile, 14)

        avatar = text(playerName.take(1).uppercase(), 34f, green, true).apply {
            gravity = Gravity.CENTER
            background = roundedBg(green, 45)
            layoutParams = LinearLayout.LayoutParams(dp(86), dp(86))
        }
        val avatarWrap = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        avatarWrap.addView(avatar)
        profile.addView(avatarWrap)
        addGap(profile, 8)

        nameLabel = text(playerName, 19f, white, true)
        profile.addView(nameLabel)
        addGap(profile, 4)

        profile.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val dot = View(this).apply {
                    setBackgroundColor(green)
                    layoutParams = LinearLayout.LayoutParams(dp(9), dp(9)).apply { rightMargin = dp(8) }
                }
                addView(dot)
                addView(text("Offline Profile", 11f, muted, false))
            }
        )

        addGap(profile, 18)
        profile.addView(smallAction("✎  Edit Name") { showSettings() })

        addGap(profile, 16)
        profile.addView(divider())
        addGap(profile, 16)

        profile.addView(text("SELECTED VERSION", 10f, muted, true).apply { gravity = Gravity.CENTER })
        addGap(profile, 6)
        profile.addView(text(selectedVersion, 30f, green, true).apply { gravity = Gravity.CENTER })
        profile.addView(text("Latest Release", 11f, muted, false).apply { gravity = Gravity.CENTER })

        addGap(profile, 18)
        profile.addView(smallAction("⇄  Change Version") { showVersions() })

        addGap(profile, 18)
        profile.addView(divider())
        addGap(profile, 18)

        profile.addView(text("QUICK STATS", 10f, muted, true).apply { gravity = Gravity.CENTER })
        addGap(profile, 10)

        val stat1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        stat1.addView(text("▣", 18f, green, true))
        stat1.addView(text(" Java Edition", 12f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        profile.addView(stat1)

        addGap(profile, 8)

        val stat2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        stat2.addView(text("◉", 18f, green, true))
        stat2.addView(text(" AETHER v1.0", 12f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        profile.addView(stat2)

        val profileSpacer = View(this)
        profile.addView(profileSpacer, LinearLayout.LayoutParams(1, 0, 1f))
        profile.addView(text("Built for real explorers.", 10f, muted, false).apply { gravity = Gravity.CENTER })
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
            .setPositiveButton("DOWNLOAD CLIENT") { _, _ -> downloadClient() }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showVersions() {
        Toast.makeText(this, "Loading Minecraft versions...", Toast.LENGTH_SHORT).show()
        executor.execute {
            try {
                val connection = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val data = connection.inputStream.bufferedReader().use { it.readText() }
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
                            getSharedPreferences("aether_settings", MODE_PRIVATE)
                                .edit().putString("selected_version", selectedVersion).apply()
                            buildLauncher()
                            Toast.makeText(this, "Selected $selectedVersion", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
            } catch (e: Exception) {
                handler.post {
                    if (!isFinishing) message("Unable to load versions. Check your internet.")
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
            .setMessage("Download the official Minecraft client JAR into AETHER's private storage?")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("DOWNLOAD") { _, _ -> beginDownload(selectedVersion) }
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
                val manifestConnection = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").openConnection() as HttpURLConnection
                manifestConnection.connectTimeout = 15000
                manifestConnection.readTimeout = 15000
                val manifestText = manifestConnection.inputStream.bufferedReader().use { it.readText() }
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
                if (versionUrl == null) throw Exception("Minecraft version not found.")

                val detailsConnection = URL(versionUrl).openConnection() as HttpURLConnection
                detailsConnection.connectTimeout = 15000
                detailsConnection.readTimeout = 15000
                val detailsText = detailsConnection.inputStream.bufferedReader().use { it.readText() }
                detailsConnection.disconnect()

                val details = JSONObject(detailsText)
                val client = details.getJSONObject("downloads").getJSONObject("client")
                val downloadUrl = client.getString("url")
                val expectedSha1 = client.optString("sha1", "")

                val directory = File(filesDir, "minecraft/versions/$version")
                if (!directory.exists() && !directory.mkdirs()) {
                    throw Exception("Could not create download folder.")
                }

                val destination = File(directory, "$version.jar")
                temporary = File(directory, "$version.jar.part")

                val connection = URL(downloadUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 20000
                connection.readTimeout = 30000
                connection.connect()

                val total = connection.contentLengthLong
                var downloaded = 0L
                connection.inputStream.use { input ->
                    FileOutputStream(temporary).use { output ->
                        val buffer = ByteArray(8192)
                        var count: Int
                        while (input.read(buffer).also { count = it } != -1) {
                            output.write(buffer, 0, count)
                            downloaded += count
                            if (total > 0) {
                                val percent = (downloaded * 100 / total).toInt().coerceIn(0, 100)
                                handler.post {
                                    if (!isFinishing) {
                                        dialog.progress = percent
                                        dialog.setMessage("Downloading $version...")
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
                    handler.post { dialog.setMessage("Verifying download...") }
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
                        message("Download failed:\n${e.message ?: "Unknown error"}")
                    }
                    downloading = false
                }
            }
        }
    }

    private fun calculateSha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            var count: Int
            while (input.read(buffer).also { count = it } != -1) {
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private fun showSettings() {
        val input = EditText(this).apply {
            setText(playerName)
            hint = "Enter player name"
            setSingleLine(true)
            setTextColor(white)
            setHintTextColor(muted)
        }

        AlertDialog.Builder(this)
            .setTitle("Player Settings")
            .setMessage("Choose your offline profile name.")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SAVE") { _, _ ->
                val entered = input.text.toString().trim()
                if (entered.isEmpty()) {
                    message("Enter a player name.")
                } else {
                    playerName = entered.take(16)
                    getSharedPreferences("aether_settings", MODE_PRIVATE)
                        .edit().putString("player_name", playerName).apply()
                    buildLauncher()
                }
            }
            .show()
    }

    private fun sidebarButton(title: String, action: () -> Unit): TextView {
        return text(title, 20f, white, true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
            background = rounded(Color.parseColor("#0b2d2c"), 12)
            layoutParams = LinearLayout.LayoutParams(-1, dp(58)).apply { bottomMargin = dp(12) }
            setOnClickListener { action() }
        }
    }

    private fun smallAction(title: String, action: () -> Unit): TextView {
        return text(title, 15f, white, true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(Color.parseColor("#102f2c"), 12)
            layoutParams = LinearLayout.LayoutParams(-1, dp(44)).apply { bottomMargin = dp(8) }
            setOnClickListener { action() }
        }
    }

    private fun divider(): View {
        return View(this).apply {
            setBackgroundColor(Color.parseColor("#19493e"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(1))
        }
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun addGap(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.parseColor("#224f46"))
        }
    }

    private fun roundedBg(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun message(value: String) {
        if (isFinishing) return
        AlertDialog.Builder(this)
            .setTitle("AETHER")
            .setMessage(value)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
