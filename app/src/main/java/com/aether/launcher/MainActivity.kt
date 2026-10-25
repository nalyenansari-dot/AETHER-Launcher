package com.aether.launcher

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import android.os.StrictMode
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val pageColor = Color.rgb(8, 12, 12)
    private val panelColor = Color.rgb(17, 25, 23)
    private val emerald = Color.rgb(74, 255, 160)
    private val secondaryText = Color.rgb(153, 173, 164)

    private val prefs by lazy {
        getSharedPreferences("aether_settings", MODE_PRIVATE)
    }

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = pageColor
        window.navigationBarColor = pageColor

        // Temporary development setting: network work runs off the UI thread below.
        showHome()
    }

    private fun showHome() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            setBackgroundColor(pageColor)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(page)
        }

        page.addView(textView("AETHER", 34f, emerald).apply {
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            letterSpacing = 0.18f
        })

        page.addView(textView("JAVA EDITION • ANDROID LAUNCHER", 11f, secondaryText).apply {
            setPadding(0, dp(4), 0, dp(25))
        })

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = roundedBackground(panelColor, 22, Color.rgb(34, 64, 49))
        }

        page.addView(card, LinearLayout.LayoutParams(-1, -2))
        card.addView(textView("YOUR NEXT WORLD", 11f, emerald))
        card.addView(textView("Adventure awaits.", 25f, Color.WHITE).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(8), 0, dp(5))
        })
        card.addView(textView(
            "Discover Minecraft Java versions.",
            14f,
            secondaryText
        ))

        val play = button("▶   PLAY", emerald, pageColor)
        card.addView(play, LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(22)
        })
        play.setOnClickListener { showPlayInfo() }

        page.addView(sectionTitle("PLAYER PROFILE"))

        val playerName = EditText(this).apply {
            hint = "Player name (3–16 characters)"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            isSingleLine = true
            setPadding(dp(14), 0, dp(14), 0)
            background = roundedBackground(panelColor, 14, Color.rgb(43, 58, 51))
            setText(prefs.getString("player_name", ""))
        }

        page.addView(playerName, LinearLayout.LayoutParams(-1, dp(54)))

        val save = button("SAVE PLAYER NAME", Color.rgb(30, 43, 37), emerald)
        page.addView(save, LinearLayout.LayoutParams(-1, dp(48)).apply {
            topMargin = dp(9)
        })

        save.setOnClickListener {
            val name = playerName.text.toString().trim()

            if (!name.matches(Regex("[A-Za-z0-9_]{3,16}"))) {
                Toast.makeText(
                    this,
                    "Use 3–16 letters, numbers or underscores.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                prefs.edit().putString("player_name", name).apply()
                Toast.makeText(
                    this,
                    "Player name saved!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        page.addView(sectionTitle("QUICK ACCESS"))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        page.addView(row)

        row.addView(
            quickTile("VERSIONS", "Browse real versions") {
                showVersions()
            },
            LinearLayout.LayoutParams(0, dp(100), 1f).apply {
                marginEnd = dp(6)
            }
        )

        row.addView(
            quickTile("SETTINGS", "Launcher options") {
                showSettings()
            },
            LinearLayout.LayoutParams(0, dp(100), 1f).apply {
                marginStart = dp(6)
            }
        )

        page.addView(textView("AETHER • BUILD 0.2.0", 10f, secondaryText).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(30), 0, 0)
        })

        setContentView(scroll)
    }

    private fun showVersions() {
        val loading = android.app.AlertDialog.Builder(this)
            .setTitle("AETHER Versions")
            .setMessage("Loading Minecraft versions...")
            .setNegativeButton("CANCEL", null)
            .create()

        loading.show()

        executor.execute {
            try {
                val connection = URL(
                    "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
                ).openConnection() as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"

                val jsonText = connection.inputStream.bufferedReader().use {
                    it.readText()
                }
                connection.disconnect()

                val root = JSONObject(jsonText)
                val latestRelease = root.getJSONObject("latest")
                    .getString("release")
                val versions = root.getJSONArray("versions")

                val ids = ArrayList<String>()
                for (i in 0 until versions.length()) {
                    val item = versions.getJSONObject(i)
                    if (item.getString("type") == "release") {
                        ids.add(item.getString("id"))
                    }
                }

                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread

                    loading.dismiss()

                    if (ids.isEmpty()) {
                        showError("No release versions were found.")
                        return@runOnUiThread
                    }

                    val selected = prefs.getString(
                        "selected_version",
                        latestRelease
                    )

                    val checked = ids.indexOf(selected).let {
                        if (it < 0) 0 else it
                    }

                    android.app.AlertDialog.Builder(this)
                        .setTitle("Minecraft Releases")
                        .setSingleChoiceItems(
                            ids.toTypedArray(),
                            checked
                        ) { dialog, which ->
                            val version = ids[which]
                            prefs.edit()
                                .putString("selected_version", version)
                                .apply()

                            Toast.makeText(
                                this,
                                "Selected version: $version",
                                Toast.LENGTH_SHORT
                            ).show()

                            dialog.dismiss()
                        }
                        .setNeutralButton("LATEST: $latestRelease", null)
                        .setNegativeButton("CLOSE", null)
                        .show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    loading.dismiss()
                    showError(
                        "Could not load versions. Check your internet connection and try again."
                    )
                }
            }
        }
    }

    private fun showPlayInfo() {
        val name = prefs.getString("player_name", "").orEmpty()
        val version = prefs.getString("selected_version", "Latest release")
            ?: "Latest release"

        val message = if (name.isBlank()) {
            "Save your player name first."
        } else {
            "Player: $name\nSelected version: $version\n\nThe version list works, but the Minecraft downloader, Android-compatible Java runtime and game-launching engine are not implemented yet."
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("AETHER • Play")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSettings() {
        android.app.AlertDialog.Builder(this)
            .setTitle("AETHER Settings")
            .setItems(
                arrayOf("Dark emerald theme", "Clear saved player name")
            ) { _, which ->
                when (which) {
                    0 -> Toast.makeText(
                        this,
                        "Dark emerald theme is active.",
                        Toast.LENGTH_SHORT
                    ).show()

                    1 -> {
                        prefs.edit().remove("player_name").apply()
                        Toast.makeText(
                            this,
                            "Saved player name cleared.",
                            Toast.LENGTH_SHORT
                        ).show()
                        showHome()
                    }
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showError(message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("AETHER")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun sectionTitle(value: String) =
        textView(value, 12f, emerald).apply {
            letterSpacing = 0.12f
            setPadding(0, dp(26), 0, dp(12))
        }

    private fun quickTile(
        title: String,
        subtitle: String,
        action: () -> Unit
    ) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(13), dp(12), dp(10), dp(10))
        background = roundedBackground(
            panelColor, 16, Color.rgb(34, 49, 42)
        )
        isClickable = true
        isFocusable = true

        addView(textView(title, 12f, emerald).apply {
            typeface = Typeface.DEFAULT_BOLD
        })
        addView(textView(subtitle, 13f, Color.WHITE).apply {
            setPadding(0, dp(9), 0, 0)
        })

        setOnClickListener { action() }
    }

    private fun textView(value: String, size: Float, color: Int) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
        }

    private fun button(value: String, fill: Int, textColor: Int) =
        TextView(this).apply {
            text = value
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
            background = roundedBackground(fill, 14, fill)
            isClickable = true
            isFocusable = true
        }

    private fun roundedBackground(
        fill: Int,
        radius: Int,
        stroke: Int
    ) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), stroke)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
