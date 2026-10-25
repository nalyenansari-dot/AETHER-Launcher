package com.aether.launcher

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val pageColor = Color.rgb(8, 12, 12)
    private val panelColor = Color.rgb(17, 25, 23)
    private val emerald = Color.rgb(74, 255, 160)
    private val secondaryText = Color.rgb(153, 173, 164)

    private val prefs by lazy {
        getSharedPreferences("aether_settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = pageColor
        window.navigationBarColor = pageColor
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

        page.addView(card, matchWrap())
        card.addView(textView("YOUR NEXT WORLD", 11f, emerald))
        card.addView(textView("Adventure awaits.", 25f, Color.WHITE).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(8), 0, dp(5))
        })
        card.addView(textView(
            "Your Minecraft Java launcher dashboard.",
            14f,
            secondaryText
        ))

        val play = button("▶   PLAY", emerald, pageColor)
        card.addView(play, LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(22)
        })
        play.setOnClickListener { showPlay() }

        page.addView(sectionTitle("YOUR PROFILE"))
        val nameInput = EditText(this).apply {
            hint = "Enter player name"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 15f
            isSingleLine = true
            setPadding(dp(14), 0, dp(14), 0)
            background = roundedBackground(panelColor, 14, Color.rgb(43, 58, 51))
            setText(prefs.getString("player_name", ""))
        }
        page.addView(nameInput, LinearLayout.LayoutParams(-1, dp(54)))

        val save = button("SAVE PLAYER NAME", Color.rgb(30, 43, 37), emerald)
        page.addView(save, LinearLayout.LayoutParams(-1, dp(48)).apply {
            topMargin = dp(9)
        })
        save.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (!name.matches(Regex("[A-Za-z0-9_]{3,16}"))) {
                Toast.makeText(
                    this,
                    "Use 3–16 letters, numbers or underscores.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                prefs.edit().putString("player_name", name).apply()
                Toast.makeText(this, "Player profile saved!", Toast.LENGTH_SHORT).show()
            }
        }

        page.addView(sectionTitle("QUICK ACCESS"))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        page.addView(row)

        row.addView(
            quickTile("VERSIONS", "Game versions") { showVersions() },
            LinearLayout.LayoutParams(0, dp(105), 1f).apply {
                marginEnd = dp(6)
            }
        )

        row.addView(
            quickTile("SETTINGS", "Launcher options") { showSettings() },
            LinearLayout.LayoutParams(0, dp(105), 1f).apply {
                marginStart = dp(6)
            }
        )

        page.addView(textView("AETHER • BUILD 0.2.0", 10f, secondaryText).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(30), 0, 0)
        })

        setContentView(scroll)
    }

    private fun showPlay() {
        val version = prefs.getString("selected_version", "1.20.1") ?: "1.20.1"
        val name = prefs.getString("player_name", "").orEmpty()

        val message = if (name.isBlank()) {
            "First save your player name in the profile section."
        } else {
            "Profile: $name\nSelected version: $version\n\nMinecraft engine is not installed yet. This build cannot launch the game."
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("AETHER • Play")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showVersions() {
        val versions = arrayOf("1.8.9", "1.12.2", "1.16.5", "1.20.1", "1.21.1")
        val current = prefs.getString("selected_version", "1.20.1")
        val checked = versions.indexOf(current).let { if (it < 0) 3 else it }

        android.app.AlertDialog.Builder(this)
            .setTitle("Select Minecraft version")
            .setSingleChoiceItems(versions, checked) { dialog, which ->
                prefs.edit().putString("selected_version", versions[which]).apply()
                Toast.makeText(
                    this,
                    "Selected version: ${versions[which]}",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showSettings() {
        val options = arrayOf(
            "Dark emerald theme",
            "Clear saved player profile"
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("AETHER Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(
                        this,
                        "Dark emerald theme is already active.",
                        Toast.LENGTH_SHORT
                    ).show()

                    1 -> android.app.AlertDialog.Builder(this)
                        .setTitle("Clear profile?")
                        .setMessage("This removes the saved player name.")
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("CLEAR") { _, _ ->
                            prefs.edit().remove("player_name").apply()
                            Toast.makeText(
                                this,
                                "Profile cleared. Return to Home to refresh it.",
                                Toast.LENGTH_LONG
                            ).show()
                            showHome()
                        }
                        .show()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun sectionTitle(value: String) = textView(
        value, 12f, emerald
    ).apply {
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
        background = roundedBackground(panelColor, 16, Color.rgb(34, 49, 42))
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

    private fun roundedBackground(fill: Int, radius: Int, stroke: Int) =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }

    private fun matchWrap() =
        LinearLayout.LayoutParams(-1, -2)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
