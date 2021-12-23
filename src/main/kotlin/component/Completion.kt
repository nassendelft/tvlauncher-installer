package component

import APP_TV_LAUNCHER_DEFAULT
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dadb.Dadb
import disableApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Completion(
  dadb: Dadb,
  onCompletion: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val disableActionState = rememberActionState<Unit>()
  val defaultTvLauncherState = dadb.produceAppInstallationState(APP_TV_LAUNCHER_DEFAULT)
  val isDefaultEnabled = !((defaultTvLauncherState.value as? AppInstallationState.Installed)?.enabled == false
    || disableActionState.value !is ActionState.Successful)

  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Filled.CheckCircle,
      contentDescription = "Install successful",
      modifier = Modifier.size(120.dp)
    )
    Spacer(
      modifier = Modifier.height(64.dp)
    )
    Text(
      text = "Successfully installed custom launcher",
      style = MaterialTheme.typography.h5
    )
    Spacer(
      modifier = Modifier.height(80.dp)
    )
    Text(
      text = "The only thing left now is to disable the default launcher"
    )
    Spacer(
      modifier = Modifier.height(8.dp)
    )

    if (defaultTvLauncherState.value is AppInstallationState.Retrieving) {
      CircularProgressIndicator()
    } else {
      Row {
        Text(
          text = "Default launcher:"
        )
        Spacer(
          modifier = Modifier.width(8.dp)
        )
        Text(
          text = if (isDefaultEnabled) "Enabled" else "Disabled",
          color = if (isDefaultEnabled) MaterialTheme.colors.error else Color.Green
        )
      }
      if (isDefaultEnabled) {
        Spacer(
          modifier = Modifier.height(16.dp)
        )
        Button(
          onClick = {
            scope.launch(Dispatchers.IO) {
              disableActionState.value = ActionState.Executing()
              disableActionState.value = if (dadb.disableApp(APP_TV_LAUNCHER_DEFAULT)) {
                ActionState.Successful(Unit)
              } else {
                ActionState.Failed()
              }
            }
          },
        ) {
          Text(
            text = "Disable"
          )
        }
        if (disableActionState.value is ActionState.Failed) {
          Spacer(
            modifier = Modifier.height(4.dp)
          )
          Text(
            text = "Failed disabling default launcher.",
            color = MaterialTheme.colors.error
          )
        }
      } else {
        Spacer(
          modifier = Modifier.height(24.dp)
        )
        Text(
          text = "All Done!",
          style = MaterialTheme.typography.h5
        )
        Spacer(
          modifier = Modifier.height(8.dp)
        )
        Button(
          onClick = { onCompletion() }
        ) {
          Text(
            text = "Close this window"
          )
        }
      }
    }
  }
}
