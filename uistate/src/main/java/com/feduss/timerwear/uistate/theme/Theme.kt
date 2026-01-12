package com.feduss.timerwear.uistate.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import com.feduss.timerwear.uistate.extension.DarkGrayNew
import com.feduss.timerwear.uistate.extension.InactiveTimer
import com.feduss.timerwear.uistate.extension.Indigo500
import com.feduss.timerwear.uistate.extension.Orange
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Purple500
import com.feduss.timerwear.uistate.extension.Purple700
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.extension.Teal200

private val LightColorScheme = lightColorScheme(
    // --- Colori principali ---
    primary = Color.Purple500,                  // Colore principale per elementi importanti (bottoni, link attivi)
    onPrimary = White,                    // Testo/icone su 'primary'
    primaryContainer = Color.Purple700,         // Contenitore meno prominente basato su 'primary'
    onPrimaryContainer = White,    // Testo/icone su 'primaryContainer'

    // --- Colori secondari ---
    secondary = Color.Indigo500,                // Colore per elementi importanti ma secondari (es. filtri)
    onSecondary = White,                  // Testo/icone su 'secondary'
    secondaryContainer = Color.Teal200,// Un contenitore chiaro basato sull'indaco
    onSecondaryContainer = White,// Testo/icone su 'secondaryContainer'

    // --- Sfondi e Superfici ---
    background = White,                   // Sfondo principale dell'app
    onBackground = Black,                 // Testo/icone su 'background'
    surface = Color.LightGray,                      // Sfondo per componenti come Card, Sheet
    onSurface = Black,                    // Testo/icone su 'surface'
    surfaceVariant = DarkGray,   // Variante di superficie per distinguere elementi (es. contorno TextField)
    onSurfaceVariant = White,       // Testo/icone su 'surfaceVariant'

    // --- Altri colori ---
    error = Color.Red,                // Colore per stati di errore
    onError = White,                      // Testo/icone su 'error'
)

private val DarkColorScheme = darkColorScheme(
    // --- Colori principali ---
    primary = Color.Purple500,                  // Colore principale, più luminoso per il tema scuro
    onPrimary = White,                // Testo/icone su 'primary'
    primaryContainer = Color.Purple700,         // Contenitore meno prominente basato su 'primary'
    onPrimaryContainer = White,    // Testo/icone su 'primaryContainer'

    // --- Colori secondari ---
    secondary = Color.Indigo500,                  // Il Teal è un classico secondario per temi scuri basati sul viola
    onSecondary = White,                  // Testo/icone su 'secondary'
    secondaryContainer = Color.Teal200,// Un contenitore scuro basato sul teal
    onSecondaryContainer = White,// Testo/icone su 'secondaryContainer'

    // --- Sfondi e Superfici ---
    background = Black,                   // Sfondo principale dell'app
    onBackground = White,                 // Testo/icone su 'background'
    surface = Color.DarkGrayNew,                      // Sfondo per componenti come Card, Sheet
    onSurface = White,                    // Testo/icone su 'surface'
    surfaceVariant = DarkGray,   // Variante di superficie per distinguere elementi (es. contorno TextField)
    onSurfaceVariant = White,       // Testo/icone su 'surfaceVariant'

    // --- Altri colori ---
    error = Color.Red,                // Il rosso per gli errori funziona bene anche qui
    onError = White,                      // Testo/icone su 'error'
)


@Composable
fun CustomMobileAppTheme( // Rinomina questo con il nome del tuo tema
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabilitato di default per usare sempre la tua palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
