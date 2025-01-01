package com.fireblocks.sdkdemo.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.ui.theme.background
import com.fireblocks.sdkdemo.ui.theme.primary_blue
import com.google.accompanist.systemuicontroller.rememberSystemUiController


/**
 * Created by Fireblocks Ltd. on 22/11/2022.
 * @see <a href="https://developer.android.com/jetpack/compose/designsystems/custom#replacing-systems">Replacing Material systems</a>
 *
 */

@Immutable
data class Typography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val b1: TextStyle,
    val b2: TextStyle,
    val b3: TextStyle,
    val b4: TextStyle,
    val bigText: TextStyle,
)

val LocalTypography = staticCompositionLocalOf {
    Typography(
        h1 = TextStyle.Default,
        h2 = TextStyle.Default,
        h3 = TextStyle.Default,
        h4 = TextStyle.Default,
        b1 = TextStyle.Default,
        b2 = TextStyle.Default,
        b3 = TextStyle.Default,
        b4 = TextStyle.Default,
        bigText = TextStyle.Default,
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = primary_blue,
    secondary = Color.White,
    tertiary = Color.White,
//    background = Color.Black,
    background = background,
)

@Composable
fun FireblocksNCWDemoTheme(content: @Composable () -> Unit){
    val fontFamily = FontFamily(
        Font(R.font.figtree_regular, FontWeight.Normal),
        Font(R.font.figtree_bold, FontWeight.Bold)
    )

    val h1 = TextStyle(
        fontSize = 32.sp,
        lineHeight = 35.2.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(600),
        color = Color(0xFFFFFFFF),
    )
    val h2 = TextStyle(
        fontSize = 24.sp,
        lineHeight = 26.4.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(600),
        color = Color(0xFFFFFFFF),
    )
    val h3 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 22.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(600),
        color = Color(0xFFFFFFFF),
    )
    val h4 =  TextStyle(
        fontSize = 16.sp,
        lineHeight = 17.6.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(600),
        color = Color(0xFFFFFFFF),
    )
    val b1 = TextStyle(
        fontSize = 16.sp,
        lineHeight = 17.6.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(400),
        color = Color(0xFFFFFFFF),
    )
    val b2 = TextStyle(
        fontSize = 14.sp,
        lineHeight = 15.4.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(400),
        color = Color(0xFFFFFFFF),
    )
    val b3 = TextStyle(
        fontSize = 12.sp,
        lineHeight = 13.2.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(500),
        color = Color(0xFFFFFFFF),
    )
    val b4 = TextStyle(
        fontSize = 10.sp,
        lineHeight = 11.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(400),
        color = Color(0xFFFFFFFF),
    )
    val bigText = TextStyle(
        fontSize = 46.sp,
        lineHeight = 50.6.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight(500),
        color = Color(0xFFFFFFFF),
    )

    val fireblocksTypography = Typography(
        h1 = h1,
        h2 = h2,
        h3 = h3,
        h4 = h4,
        b1 = b1,
        b2 = b2,
        b3 = b3,
        b4 = b4,
        bigText = bigText,
    )
    CompositionLocalProvider(
        LocalTypography provides fireblocksTypography,
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }


    // Remember a SystemUiController
    val systemUiController = rememberSystemUiController()
//    val useDarkIcons = !isSystemInDarkTheme()
    val useDarkIcons = false

    DisposableEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )

        // setStatusBarColor() and setNavigationBarColor() also exist

        onDispose {}
    }
}

// Use with eg. ReplacementTheme.typography.body
object FireblocksNCWDemoTheme {
    val typography: Typography
        @Composable
        get() = LocalTypography.current
}



