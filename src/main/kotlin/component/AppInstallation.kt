package component

import androidx.compose.runtime.*
import dadb.Dadb
import isAppEnabled
import isAppInstalled
import java.io.IOException

@Composable
fun Dadb.produceAppInstallationState(
  packageName: String
): State<AppInstallationState> {
  val result = remember { mutableStateOf<AppInstallationState>(AppInstallationState.Retrieving) }
  LaunchedEffect(Unit) {
    result.value = try {
      if (!isAppInstalled(packageName)) {
        AppInstallationState.NotFound
      } else {
        val enabled = isAppEnabled(packageName)
        AppInstallationState.Installed(enabled)
      }
    } catch (exception: IOException) {
      AppInstallationState.Failed(exception)
    }
  }
  return result
}

sealed class AppInstallationState {
  object Retrieving : AppInstallationState()
  object NotFound : AppInstallationState()
  class Installed(val enabled: Boolean) : AppInstallationState()
  class Failed(val exception: IOException) : AppInstallationState()
}
