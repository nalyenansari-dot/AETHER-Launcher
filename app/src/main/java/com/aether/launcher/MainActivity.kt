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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = pageColor
        window.navigationBarColor = pageColor
        showHome()
    }

    private fun showHome() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(pageColor)
            isFillViewport = true
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
        }
        scrollView.addView(page)

        page.addView(textView("AETHER", 34f, emerald).apply {
            letterSpacing = 0.22f
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        })
        page.addView(textView("JAVA EDITION  •  ANDROID LAUNCHER", 11f, secondaryText).apply {
            letterSpacing = 0.12f
            setPadding(0, dp(3), 0, dp(24))
        })

        val heroCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
            background = roundedBackground(panelColor, 22, Color.rgb(34, 64, 49))
        }
        page.addView(heroCard, LinearLayout.LayoutParams(-1, -2))
        heroCard.addView(textView("YOUR NEXT WORLD", 11f, emerald).apply {
            letterSpacing = 0.14f
        })
        heroCard.addView(textView("Adventure awaits.", 25f, Color.WHITE).apply {
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setPadding(0, dp(7), 0, dp(5))
        })
        heroCard.addView(textView("A fresh home for your Minecraft Java journey.", 14f, secondaryText))

        val playButton = button("▶   PLAY", emerald, pageColor)
        heroCard.addView(
            playButton,
            LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(22) }
        )
        playButton.setOnClickListener {
            Toast.makeText(
                this,
                "Minecraft launch is not available in this starter build yet.",
                Toast.LENGTH_LONG
            ).show()
        }

        page.addView(textView("YOUR SETUP", 12f, emerald).apply {
            letterSpacing = 0.14f
            setPadding(0, dp(27), 0, dp(12))
        })

        val playerName = EditText(this).apply {
            hint = "Player name (local profile)"
            setHintTextColor(Color.rgb(105, 126, 116))
            setTextColor(Color.WHITE)
            textSize = 15f
            isSingleLine = true
            setPadding(dp(16), 0, dp(16), 0)
            background = roundedBackground(panelColor, 14, Color.rgb(43, 58, 51))
            setText(getPreferences(MODE_PRIVATE).getString(KEY_PLAYER_NAME, ""))
        }
        page.addView(playerName, LinearLayout.LayoutParams(-1, dp(54)))

        val saveButton = button("SAVE PLAYER NAME", Color.rgb(30, 43, 37), emerald)
        page.addView(
            saveButton,
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(9) }
        )
        saveButton.setOnClickListener {
            val name = playerName.text.toString().trim()
            getPreferences(MODE_PRIVATE).edit().putString(KEY_PLAYER_NAME, name).apply()
            Toast.makeText(this, "Player name saved on this device.", Toast.LENGTH_SHORT).show()
        }

        page.addView(textView("QUICK ACCESS", 12f, emerald).apply {
            letterSpacing = 0.14f
            setPadding(0, dp(27), 0, dp(12))
        })

        val quickAccessRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        page.addView(quickAccessRow)
        quickAccessRow.addView(
            quickTile("VERSIONS", "Choose a version", "The version manager is not implemented yet."),
            LinearLayout.LayoutParams(0, dp(104), 1f).apply { marginEnd = dp(6) }
        )
        quickAccessRow.addView(
            quickTile("SETTINGS", "Tune your setup", "Launcher settings are not implemented yet."),
            LinearLayout.LayoutParams(0, dp(104), 1f).apply { marginStart = dp(6) }
        )

        page.addView(textView("AETHER  •  BUILD 0.1.0", 10f, secondaryText).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(30), 0, 0)
            letterSpacing = 0.12f
        })

        setContentView(scrollView)
    }

    private fun quickTile(title: String, subtitle: String, message: String) =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(15), dp(10), dp(10))
            background = roundedBackground(panelColor, 16, Color.rgb(34, 49, 42))
            addView(textView(title, 12f, emerald).apply {
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(textView(subtitle, 14f, Color.WHITE).apply {
                setPadding(0, dp(9), 0, dp(4))
            })
            isClickable = true
            isFocusable = true
            setOnClickListener {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

    private fun textView(value: String, sizeSp: Float, color: Int) =
        TextView(this).apply {
            text = value
            textSize = sizeSp
            setTextColor(color)
        }

    private fun button(value: String, fill: Int, textColor: Int) =
        TextView(this).apply {
            text = value
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(textColor)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = 0.08f
            background = roundedBackground(fill, 14, fill)
            isClickable = true
            isFocusable = true
        }

    private fun roundedBackground(fill: Int, radiusDp: Int, strokeColor: Int) =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radiusDp).toFloat()
            setStroke(dp(1), strokeColor)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val KEY_PLAYER_NAME = "player_name"
    }
}
