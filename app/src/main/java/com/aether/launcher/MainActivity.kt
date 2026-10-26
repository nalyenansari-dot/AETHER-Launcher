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

    companion object {
        private const val MANIFEST_URL =
            "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"
    }

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

    @Volatile
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
        }

        brand.addView(
            TextView(this).apply {
                text = "A"
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(green)
                gravity = Gravity.CENTER
                background = roundedBg(green, 12)
            },
            LinearLayout.LayoutParams(dp(38), dp(38))
        )

        brand.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
                addView(text("AETHER", 26f, green, true))
                addView(text("Minecraft Launcher", 11f, muted))
            },
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        sidebar.addView(brand, LinearLayout.LayoutParams(-1, dp(70)))

        sidebar.addView(
            View(this).apply { setBackgroundColor(Color.parseColor("#0d3432")) },
            LinearLayout.LayoutParams(-1, dp(1))
        )

        gap(sidebar, 20)

        sidebar.addView(sideButton("⌂   HOME") { buildLauncher() })
        sidebar.addView(sideButton("▤   VERSIONS") { showVersions() })
        sidebar.addView(sideButton("⚙   SETTINGS") { showSettings() })

        sidebar.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        sidebar.addView(text("JAVA EDITION", 10f, muted, true).apply { gravity = Gravity.CENTER })
        sidebar.addView(text("AETHER v1.0", 12f, green, true).apply { gravity = Gravity.CENTER })

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(14))
        }

        root.addView(center, LinearLayout.LayoutParams(0, -1, 1f))

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        top.addView(text("●  AETHER READY", 10f, green, true))
        center.addView(top, LinearLayout.LayoutParams(-1, dp(26)))

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
        }

        heroText.addView(text("AETHER LAUNCHER", 13f, green, true))
        gap(heroText, 8)
        heroText.addView(text("YOUR WORLD.", 32f, white, true))
        heroText.addView(text("YOUR ADVENTURE.", 30f, green, true))
        gap(heroText, 8)
        heroText.addView(text("Explore. Build. Survive.\nMinecraft Java Edition.", 14f, muted))
        gap(heroText, 14)

        heroText.addView(
            actionButton("▶  PLAY", green, Color.BLACK, 20f) { showPlayInfo() },
            LinearLayout.LayoutParams(dp(220), dp(54))
        )

        gap(heroText, 10)

        val versionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        versionRow.addView(text("Selected Version: ", 12f, muted))
        versionLabel = text(selectedVersion, 13f, green, true)
        versionLabel.setOnClickListener { showVersions() }
        versionRow.addView(versionLabel)
        heroText.addView(versionRow)

        hero.addView(heroText, LinearLayout.LayoutParams(0, -1, 1f))

        hero.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.parseColor("#efc9ae"),
                        Color.parseColor("#a5d7d7"),
                        Color.parseColor("#143e38")
                    )
                ).apply {
                    cornerRadius = dp(18).toFloat()
                }
                addView(
                    text("MINECRAFT\nJAVA EDITION", 18f, white, true).apply {
                        gravity = Gravity.CENTER
                        setShadowLayer(8f, 0f, 0f, Color.argb(100, 0, 0, 0))
                    }
                )
            },
            LinearLayout.LayoutParams(dp(220), -1)
        )

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        center.addView(bottom, LinearLayout.LayoutParams(-1, dp(150)))

        val news = card()
        news.addView(text("📰  Latest News", 16f, green, true))
        gap(news, 8)
        news.addView(text("Minecraft $selectedVersion", 14f, white, true))
        news.addView(text("Official client download and version selection.", 12f, muted))
        bottom.addView(news, LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = dp(10) })

        val actions = card()
        actions.addView(text("⚡  Quick Actions", 16f, green, true))
        gap(actions, 8)
        actions.addView(smallAction("⬇  Download Client") { downloadClient() })
        gap(actions, 6)
        actions.addView(smallAction("▤  View All Versions") { showVersions() })
        bottom.addView(actions, LinearLayout.LayoutParams(0, -1, 1f))

        val profile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(panel)
        }

        root.addView(profile, LinearLayout.LayoutParams(dp(220), -1))
        profile.addView(text("PLAYER PROFILE", 12f, green, true).apply { gravity = Gravity.CENTER })
        gap(profile, 12)

        avatar = text(playerName.take(1).uppercase(), 34f, green, true).apply {
            gravity = Gravity.CENTER
            background = roundedBg(green, 45)
        }

        profile.addView(
            LinearLayout(this).apply {
                gravity = Gravity.CENTER
                addView(avatar, LinearLayout.LayoutParams(dp(86), dp(86)))
            }
        )

        gap(profile, 8)

        nameLabel = text(playerName, 19f, white, true).apply { gravity = Gravity.CENTER }
        profile.addView(nameLabel)
        gap(profile, 4)

        profile.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                val statusDot = View(this@MainActivity).apply {
                    setBackgroundColor(green)
                    layoutParams = LinearLayout.LayoutParams(dp(9), dp(9)).apply {
                        rightMargin = dp(8)
                    }
                }

                addView(statusDot)
                addView(text("Offline Profile", 11f, muted))
            }
        )

        gap(profile, 16)
        profile.addView(smallAction("✎  Edit Name") { showSettings() })
        gap(profile, 14)
        profile.addView(divider())
        gap(profile, 14)
        profile.addView(text("SELECTED VERSION", 10f, muted, true).apply { gravity = Gravity.CENTER })
        profile.addView(text(selectedVersion, 28f, green, true).apply { gravity = Gravity.CENTER })
        profile.addView(text("Minecraft Java Edition", 11f, muted).apply { gravity = Gravity.CENTER })
        gap(profile, 14)
        profile.addView(smallAction("⇄  Change Version") { showVersions() })
        gap(profile, 14)
        profile.addView(divider())
        gap(profile, 14)
        profile.addView(text("QUICK STATS", 10f, muted, true).apply { gravity = Gravity.CENTER })
        gap(profile, 8)
        profile.addView(text("▣  Java Edition", 12f, white, true))
        gap(profile, 6)
        profile.addView(text("◉  AETHER v1.0", 12f, white, true))
        profile.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        profile.addView(text("Built for real explorers.", 10f, muted).apply { gravity = Gravity.CENTER })
    }

    private fun showPlayInfo() {
        AlertDialog.Builder(this)
            .setTitle("AETHER PLAY")
            .setMessage(
                "Selected version: $selectedVersion\n\n" +
                    "The launcher interface is ready, but actual Minecraft launching is not implemented yet.\n\n" +
                    "You can download the official client JAR. A complete game installation, Java runtime, libraries, assets and launch engine are still required."
            )
            .setPositiveButton("DOWNLOAD CLIENT") { _, _ -> downloadClient() }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showVersions() {
        Toast.makeText(this, "Loading Minecraft versions...", Toast.LENGTH_SHORT).show()

        executor.execute {
            try {
                val data = downloadText(MANIFEST_URL)
                val array = JSONObject(data).getJSONArray("versions")
                val releases = mutableListOf<String>()

                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    if (item.optString("type") == "release") {
                        releases.add(item.getString("id"))
                    }
                }

                handler.post {
                    if (isFinishing || releases.isEmpty()) {
                        return@post
                    }

                    val choices = releases.take(100).toTypedArray()

                    AlertDialog.Builder(this)
                        .setTitle("Select Minecraft Version")
                        .setItems(choices) { _, index ->
                            selectedVersion = choices[index]
                            getSharedPreferences("aether_settings", MODE_PRIVATE)
                                .edit()
                                .putString("selected_version", selectedVersion)
                                .apply()
                            buildLauncher()
                            Toast.makeText(this, "Selected $selectedVersion", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
            } catch (_: Exception) {
                handler.post {
                    if (!isFinishing) {
                        message("Unable to load versions. Check your internet.")
                    }
                }
            }
        }
    }

    private fun showSettings() {
        val input = EditText(this).apply {
            setText(playerName)
            hint = "Enter player name"
            setSingleLine(true)
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(input, LinearLayout.LayoutParams(-1, dp(52)))
        }

        AlertDialog.Builder(this)
            .setTitle("Player Settings")
            .setMessage("Choose an offline profile name.")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->
                val value = input.text.toString().trim()

                if (value.isBlank()) {
                    message("Player name cannot be empty.")
                } else {
                    playerName = value.take(16)
                    getSharedPreferences("aether_settings", MODE_PRIVATE)
                        .edit()
                        .putString("player_name", playerName)
                        .apply()
                    buildLauncher()
                    message("Player name saved.")
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
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
            var temp: File? = null
            var failure: String? = null
            var success = false

            try {
                val manifest = JSONObject(downloadText(MANIFEST_URL))
                val versions = manifest.getJSONArray("versions")
                var selectedUrl: String? = null

                for (i in 0 until versions.length()) {
                    val item = versions.getJSONObject(i)
                    if (item.optString("id") == version) {
                        selectedUrl = item.optString("url")
                        break
                    }
                }

                if (selectedUrl == null) {
                    throw IllegalStateException("Version metadata not found.")
                }

                val versionJson = JSONObject(downloadText(selectedUrl))
                val clientUrl = versionJson.getJSONObject("downloads")
                    .getJSONObject("client")
                    .getString("url")

                val fileName = "minecraft-$version.jar"
                temp = File(getExternalFilesDir(null), fileName)

                val connection = URL(clientUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("Download failed: ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    FileOutputStream(temp).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                    }
                }

                success = true
                dialog.setMessage("Download complete")
            } catch (e: Exception) {
                failure = e.message ?: "Download failed"
            } finally {
                handler.post {
                    dialog.dismiss()
                    if (success) {
                        message("Downloaded client for $version")
                    } else {
                        message(failure ?: "Download failed")
                    }
                    downloading = false
                    temp?.delete()
                }
            }
        }
    }

    private fun message(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    private fun sideButton(label: String, action: () -> Unit): TextView {
        return text(label, 12f, white, true).apply {
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setOnClickListener { action() }
        }
    }

    private fun smallAction(label: String, action: () -> Unit): TextView {
        return text(label, 11f, white, true).apply {
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setOnClickListener { action() }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#102d2d"))
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.parseColor("#1e5f56"))
            }
        }
    }

    private fun actionButton(label: String, color: Int, textColor: Int, size: Float, action: () -> Unit): TextView {
        return text(label, size, textColor, true).apply {
            setBackgroundColor(color)
            gravity = Gravity.CENTER
            setOnClickListener { action() }
            background = roundedBg(color, 20)
        }
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.parseColor("#0c2c2c"))
            background = roundedBg(Color.parseColor("#0c2c2c"), 16)
        }
    }

    private fun divider(): View {
        return View(this).apply {
            setBackgroundColor(Color.parseColor("#1d5d50"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(1))
        }
    }

    private fun gap(parent: LinearLayout, size: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(-1, dp(size)))
    }

    private fun roundedBg(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun downloadText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        connection.connect()

        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        return stream.bufferedReader().use { it.readText() }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
