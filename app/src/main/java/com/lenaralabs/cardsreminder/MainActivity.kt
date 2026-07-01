package com.lenaralabs.cardsreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.lenaralabs.cardsreminder.app.RootContent
import com.lenaralabs.cardsreminder.ui.theme.CardsreminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardsreminderTheme {
                RootContent(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
