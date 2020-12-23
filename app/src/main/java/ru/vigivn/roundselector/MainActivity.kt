package ru.vigivn.roundselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class Item(private val drawable: Int = R.drawable.ic_android) : IRoundSelectorItem {
    override fun getLabel(): String = "label"

    override fun getDrawable(): Int = drawable
}

class MainActivity : AppCompatActivity() {
    private val items = listOf<Item>(
        Item(),
        Item(R.drawable.ic_launcher_foreground),
        Item(),
        Item(R.drawable.ic_launcher_foreground)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        round_selector.items = items
        round_selector.currIndex = 1
    }
}
