package component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jthemedetecor.OsThemeDetector

fun startApplication() = application {
  val detector = OsThemeDetector.getDetector()
  val darkTheme = remember { mutableStateOf(detector.isDark) }
  detector.registerListener { isDark -> darkTheme.value = isDark }

  val state = rememberWindowState(
    width = 900.dp,
    height = 635.dp,
    position = WindowPosition.Aligned(Alignment.Center),
  )

  Window(
    onCloseRequest = ::exitApplication,
    title = "TvLauncher installer",
    state = state,
    resizable = false
  ) {
    MaterialTheme(
      colors = if (darkTheme.value) Theme.appDarkColors else Theme.appLightColors,
      typography = Theme.appTypography()
    ) {
      Scaffold(
        modifier = Modifier.fillMaxSize()
      ) {
        Setup(
          onFinished = { exitApplication() },
          modifier = Modifier.background(
            brush = if (darkTheme.value) Theme.appBackgroundDark() else Theme.appBackgroundLight()
          )
        )
      }
    }
  }
}
