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

        val prefs = getSharedPreferences(
            "aether_settings",
            MODE_PRIVATE
        )

        selectedVersion = prefs.getString(
            "selected_version",
            "1.21.4"
        ) ?: "1.21.4"

        playerName = prefs.getString(
            "player_name",
            "Player"
        ) ?: "Player"

        buildLauncher()
    }

    private fun buildLauncher() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(bg)
        }

        setContentView(root)

        // SIDEBAR

        val sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)

            setPadding(
                dp(14),
                dp(18),
                dp(14),
                dp(12)
            )
        }

        root.addView(
            sidebar,
            LinearLayout.LayoutParams(dp(210), -1)
        )

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

                addView(
                    text("AETHER", 26f, green, true)
                )

                addView(
                    text("Minecraft Launcher", 11f, muted)
                )
            },
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        sidebar.addView(
            brand,
            LinearLayout.LayoutParams(-1, dp(70))
        )

        sidebar.addView(
            View(this).apply {
                setBackgroundColor(
                    Color.parseColor("#0d3432")
                )
            },
            LinearLayout.LayoutParams(-1, dp(1))
        )

        gap(sidebar, 20)

        sidebar.addView(
            sideButton("⌂   HOME") {
                buildLauncher()
            }
        )

        sidebar.addView(
            sideButton("▤   VERSIONS") {
                showVersions()
            }
        )

        sidebar.addView(
            sideButton("⚙   SETTINGS") {
                showSettings()
            }
        )

        sidebar.addView(
            View(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        sidebar.addView(
            text("JAVA EDITION", 10f, muted, true).apply {
                gravity = Gravity.CENTER
            }
        )

        sidebar.addView(
            text("AETHER v1.0", 12f, green, true).apply {
                gravity = Gravity.CENTER
            }
        )

        // CENTER CONTENT

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(14)
            )
        }

        root.addView(
            center,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(
            View(this),
            LinearLayout.LayoutParams(0, 1, 1f)
        )

        top.addView(
            text("●  AETHER READY", 10f, green, true)
        )

        center.addView(
            top,
            LinearLayout.LayoutParams(-1, dp(26))
        )

        // HERO SECTION

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(22),
                dp(18),
                dp(22),
                dp(18)
            )

            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0a2b2a"))
                cornerRadius = dp(18).toFloat()

                setStroke(
                    dp(1),
                    Color.parseColor("#1d5d50")
                )
            }
        }

        center.addView(
            hero,
            LinearLayout.LayoutParams(-1, dp(280))
        )

        val heroText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        heroText.addView(
            text("AETHER LAUNCHER", 13f, green, true)
        )

        gap(heroText, 8)

        heroText.addView(
            text("YOUR WORLD.", 32f, white, true)
        )

        heroText.addView(
            text("YOUR ADVENTURE.", 30f, green, true)
        )

        gap(heroText, 8)

        heroText.addView(
            text(
                "Explore. Build. Survive.\nMinecraft Java Edition.",
                14f,
                muted
            )
        )

        gap(heroText, 14)

        heroText.addView(
            actionButton(
                "▶  PLAY",
                green,
                Color.BLACK,
                20f
            ) {
                showPlayInfo()
            },
            LinearLayout.LayoutParams(dp(220), dp(54))
        )

        gap(heroText, 10)

        val versionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        versionRow.addView(
            text("Selected Version: ", 12f, muted)
        )

        versionLabel = text(
            selectedVersion,
            13f,
            green,
            true
        )

        versionLabel.setOnClickListener {
            showVersions()
        }

        versionRow.addView(versionLabel)

        heroText.addView(versionRow)

        hero.addView(
            heroText,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        // MINECRAFT DISPLAY PANEL

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
                    text(
                        "MINECRAFT\nJAVA EDITION",
                        18f,
                        white,
                        true
                    ).apply {
                        gravity = Gravity.CENTER

                        setShadowLayer(
                            8f,
                            0f,
                            0f,
                            Color.argb(100, 0, 0, 0)
                        )
                    }
                )
            },
            LinearLayout.LayoutParams(dp(220), -1)
        )

        // BOTTOM PANELS

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        center.addView(
            bottom,
            LinearLayout.LayoutParams(-1, dp(150))
        )

        val news = card()

        news.addView(
            text("📰  Latest News", 16f, green, true)
        )

        gap(news, 8)

        news.addView(
            text(
                "Minecraft $selectedVersion",
                14f,
                white,
                true
            )
        )

        news.addView(
            text(
                "Official client download and version selection.",
                12f,
                muted
            )
        )

        bottom.addView(
            news,
            LinearLayout.LayoutParams(0, -1, 1f).apply {
                rightMargin = dp(10)
            }
        )

        val actions = card()

        actions.addView(
            text("⚡  Quick Actions", 16f, green, true)
        )

        gap(actions, 8)

        actions.addView(
            smallAction("⬇  Download Client") {
                downloadClient()
            }
        )

        gap(actions, 6)

        actions.addView(
            smallAction("▤  View All Versions") {
                showVersions()
            }
        )

        bottom.addView(
            actions,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        // PLAYER PROFILE

        val profile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
            )

            setBackgroundColor(panel)
        }

        root.addView(
            profile,
            LinearLayout.LayoutParams(dp(220), -1)
        )

        profile.addView(
            text("PLAYER PROFILE", 12f, green, true).apply {
                gravity = Gravity.CENTER
            }
        )

        gap(profile, 12)

        avatar = text(
            playerName.take(1).uppercase(),
            34f,
            green,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = roundedBg(green, 45)
        }

        profile.addView(
            LinearLayout(this).apply {
                gravity = Gravity.CENTER

                addView(
                    avatar,
                    LinearLayout.LayoutParams(dp(86), dp(86))
                )
            }
        )

        gap(profile, 8)

        nameLabel = text(
            playerName,
            19f,
            white,
            true
        ).apply {
            gravity = Gravity.CENTER
        }

        profile.addView(nameLabel)

        gap(profile, 4)

        // FIXED CONTEXT: View(this@MainActivity)

        profile.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                val statusDot = View(this@MainActivity).apply {
                    setBackgroundColor(green)

                    layoutParams = LinearLayout.LayoutParams(
                        dp(9),
                        dp(9)
                    ).apply {
                        rightMargin = dp(8)
                    }
                }

                addView(statusDot)

                addView(
                    text("Offline Profile", 11f, muted)
                )
            }
        )

        gap(profile, 16)

        profile.addView(
            smallAction("✎  Edit Name") {
                showSettings()
            }
        )

        gap(profile, 14)

        profile.addView(divider())

        gap(profile, 14)

        profile.addView(
            text("SELECTED VERSION", 10f, muted, true).apply {
                gravity = Gravity.CENTER
            }
        )

        profile.addView(
            text(selectedVersion, 28f, green, true).apply {
                gravity = Gravity.CENTER
            }
        )

        profile.addView(
            text(
                "Minecraft Java Edition",
                11f,
                muted
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        gap(profile, 14)

        profile.addView(
            smallAction("⇄  Change Version") {
                showVersions()
            }
        )

        gap(profile, 14)

        profile.addView(divider())

        gap(profile, 14)

        profile.addView(
            text("QUICK STATS", 10f, muted, true).apply {
                gravity = Gravity.CENTER
            }
        )

        gap(profile, 8)

        profile.addView(
            text("▣  Java Edition", 12f, white, true)
        )

        gap(profile, 6)

        profile.addView(
            text("◉  AETHER v1.0", 12f, white, true)
        )

        profile.addView(
            View(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        profile.addView(
            text(
                "Built for real explorers.",
                10f,
                muted
            ).apply {
                gravity = Gravity.CENTER
            }
        )
    }

    // PLAY INFORMATION

    private fun showPlayInfo() {
        AlertDialog.Builder(this)
            .setTitle("AETHER PLAY")
            .setMessage(
                "Selected version: $selectedVersion\n\n" +
                    "The launcher interface is ready, but actual " +
                    "Minecraft launching is not implemented yet.\n\n" +
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

    // LOAD MINECRAFT VERSIONS

    private fun showVersions() {
        Toast.makeText(
            this,
            "Loading Minecraft versions...",
            Toast.LENGTH_SHORT
        ).show()

        executor.execute {
            try {
                val data = downloadText(MANIFEST_URL)

                val array = JSONObject(data)
                    .getJSONArray("versions")

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

                            getSharedPreferences(
                                "aether_settings",
                                MODE_PRIVATE
                            ).edit()
                                .putString(
                                    "selected_version",
                                    selectedVersion
                                )
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

            } catch (_: Exception) {
                handler.post {
                    if (!isFinishing) {
                        message(
                            "Unable to load versions. Check your internet."
                        )
                    }
                }
            }
        }
    }

    // PLAYER SETTINGS

    private fun showSettings() {

        val input = EditText(this).apply {
            setText(playerName)
            hint = "Enter player name"
            setSingleLine(true)
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(20),
                dp(8),
                dp(20),
                0
            )

            addView(
                input,
                LinearLayout.LayoutParams(-1, dp(52))
            )
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

                    getSharedPreferences(
                        "aether_settings",
                        MODE_PRIVATE
                    ).edit()
                        .putString("player_name", playerName)
                        .apply()

                    buildLauncher()
                    message("Player name saved.")
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    // DOWNLOAD CLIENT

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

            var temp: File? = null
            var failure: String? = null
            var success = false

            try {

                val manifest = JSONObject(
                    do
