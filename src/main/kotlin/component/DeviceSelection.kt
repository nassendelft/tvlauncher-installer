package component

import DadbProduct
import ScanResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.unit.dp
import connectToAdbDevice
import dadb.Dadb
import getProductInfo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withTimeoutOrNull
import scanDevicesOnNetwork
import kotlin.math.min

@Composable
fun DeviceSelection(
  onSelection: (Dadb) -> Unit,
  modifier: Modifier = Modifier
) {
  val showManual = remember { mutableStateOf(false) }
  val selected = remember { mutableStateOf<Dadb?>(null) }
  Column(
    modifier = modifier.fillMaxSize()
  ) {
    Text(
      text = "Connecting your device",
      style = typography.h5,
    )
    Spacer(
      modifier = Modifier.height(8.dp)
    )
    Text(
      text = "Select a device where you want to install your launcher.\n" +
        "The selected device should match with what was shown in the last step.",
      style = typography.subtitle1,
    )
    Spacer(
      modifier = Modifier.height(12.dp)
    )
    if (selected.value == null) {
      if (showManual.value) {
        ManualEntry(
          modifier = Modifier.padding(bottom = 4.dp),
          onConnect = { selected.value = it }
        )
        Text(
          text = "Select device",
          style = typography.body1.copy(textDecoration = Underline),
          modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            .align(Alignment.End)
            .clickable {
              showManual.value = false
              selected.value = null
            }
        )
      } else {
        DeviceSearch(
          onConnect = { selected.value = it }
        )
        Text(
          text = "Input manually",
          style = typography.body1.copy(textDecoration = Underline),
          modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            .align(Alignment.End)
            .clickable {
              showManual.value = true
              selected.value = null
            }
        )
      }
    }
    selected.value?.let {
      SelectedDevice(
        dadb = it,
        onSelected = { selection ->
          if (selection == null) selected.value = null
          else onSelection(selection)
        }
      )
    }
  }
}

@Composable
private fun SelectedDevice(
  dadb: Dadb,
  modifier: Modifier = Modifier,
  onSelected: (Dadb?) -> Unit
) {
  val product = produceState<DadbProduct?>(null) { value = dadb.getProductInfo() }

  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      painter = painterResource("icons/tv_black_24dp.svg"),
      contentDescription = "Selected device",
      modifier = Modifier.size(100.dp)
    )
    Text(
      text = product.value?.model ?: "",
      style = typography.h5,
      modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
    )
    Button(
      onClick = { onSelected(dadb) }
    ) {
      Text(
        text = "Select this device"
      )
    }
    Text(
      text = "Select a different device",
      style = typography.body1.copy(textDecoration = Underline),
      modifier = Modifier.padding(8.dp).clickable { onSelected(null) }
    )
  }
}

@Composable
private fun DeviceSearch(
  onConnect: (Dadb?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val connectError = remember { mutableStateOf(false) }
  val selected = remember { mutableStateOf<ScanResult?>(null) }
  val expanded = remember { mutableStateOf(false) }
  val scanning = remember { mutableStateOf(true) }
  val state = remember { mutableStateOf<DeviceState>(DeviceState.Initial) }
  val results = remember { mutableStateListOf<ScanResult>() }

  LaunchedEffect(Unit) {
    scanDevicesOnNetwork()
      .filter { it.couldConnect }
      .onCompletion { scanning.value = false }
      .collect { results.add(it) }
  }

  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Card {
      Column(
        modifier = Modifier.padding(8.dp)
      ) {
        SelectRow(
          onClick = { expanded.value = !expanded.value },
          selected = selected.value,
          expanded = expanded.value
        )
        if (expanded.value) {
          Box(
            modifier = Modifier.padding(top = 16.dp)
          ) {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              items(results) {
                DeviceScanRow(
                  scanResult = it,
                  onClick = { device ->
                    expanded.value = false
                    if (selected.value != device) {
                      selected.value = device
                      onConnect(null)
                    }
                  }
                )
              }
              if (scanning.value) {
                item {
                  Column {
                    CircularProgressIndicator(
                      modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                      text = "Scanning..."
                    )
                  }
                }
              }
            }
            if (results.isEmpty() && !scanning.value) {
              Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "No devices found"
                )
              }
            }
          }
        }
        if (!expanded.value && selected.value != null) {
          Box(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
          ) {
            Button(
              enabled = state.value !is DeviceState.Connecting,
              onClick = {
                connectError.value = false
                state.value = DeviceState.Connecting
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Connect"
              )
            }
            if (state.value is DeviceState.Connecting) {
              ConnectDevice(
                scanResult = selected.value!!,
                onConnect = { dadb ->
                  if (dadb != null) {
                    onConnect(dadb)
                  } else {
                    connectError.value = true
                  }
                  state.value = DeviceState.Initial
                },
                modifier = Modifier.align(Alignment.Center)
              )
            }
          }
          if (connectError.value) {
            Text(
              text = "Failed to connect",
              color = colors.error,
              modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ConnectDevice(
  scanResult: ScanResult,
  onConnect: (Dadb?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val finished = remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val dadb = withTimeoutOrNull(5 * 60 * 1000) {
      connectToAdbDevice(scanResult.address, scanResult.port)
    }
    finished.value = true
    onConnect(dadb)
  }

  CircularProgressIndicator(
    modifier = modifier
      .size(15.dp, 15.dp)
      .then(
        if (finished.value) Modifier.alpha(0f) else Modifier
      )
  )
}

@Composable
fun ManualEntry(
  modifier: Modifier = Modifier,
  onConnect: (Dadb) -> Unit
) {
  val inputIp = remember { mutableStateOf("") }
  val inputPort = remember { mutableStateOf("5555") }

  val ipError = remember { mutableStateOf<String?>(null) }
  val portError = remember { mutableStateOf<String?>(null) }

  val state = remember { mutableStateOf<DeviceState>(DeviceState.Initial) }
  val connectError = remember { mutableStateOf(false) }

  val ipValidation: (String) -> String? = {
    if (it.isBlank())
      "Cannot be blank"
    else if (it.count { char -> char == '.' } != 3)
      "Must contain 3 dots ('.')"
    else if (it.first() == '.')
      "Cannot start with '.'"
    else if (it.last() == '.')
      "Cannot end with '.'"
    else if (it.split('.')
        .fold(false) { r, section -> r || section.isEmpty() || section.length > 3 }
    )
      "Each segment must hava min 1 and max 3 numbers"
    else null
  }

  val portValidation: (String) -> String? = {
    if (it.isBlank()) "Cannot be blank"
    else if (inputPort.value.toInt() > 65535) "Invalid range (< 65535)"
    else null
  }
  Card(
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(8.dp)
    ) {
      Row {
        Column {
          Text(
            text = "IPv4"
          )
          TextField(
            value = inputIp.value,
            onValueChange = {
              inputIp.value = it.replace("[^0-9.]".toRegex(), "")
                .let { s -> s.substring(0, min(s.length, 15)) }
            },
            singleLine = true,
            placeholder = { Text("i.e. 192.168.1.10") },
            isError = ipError.value != null,
          )
          Text(
            text = ipError.value ?: "",
            color = colors.error
          )
        }
        Spacer(
          modifier = Modifier.width(20.dp)
        )
        Column(
          modifier = Modifier.width(100.dp)
        ) {
          Text(
            text = "Port"
          )
          TextField(
            value = inputPort.value,
            onValueChange = {
              inputPort.value = it.replace("[^0-9]".toRegex(), "")
                .let { s -> s.substring(0, min(s.length, 5)) }
            },
            singleLine = true,
            isError = portError.value != null,
          )
          Text(
            text = portError.value ?: "",
            color = colors.error
          )
        }
        Spacer(
          modifier = Modifier.width(20.dp)
        )
        Column(
          modifier = Modifier.align(Alignment.CenterVertically),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Button(
            enabled = state.value != DeviceState.Connecting,
            onClick = {
              ipError.value = ipValidation(inputIp.value)
              portError.value = portValidation(inputPort.value)

              if (ipError.value == null && portError.value == null) {
                connectError.value = false
                state.value = DeviceState.Connecting
              }
            },
          ) {
            Text(
              text = "Connect"
            )
          }
          if (connectError.value) {
            Text(
              text = "Failed to connect",
              color = colors.error,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
        if (state.value == DeviceState.Connecting) {
          ConnectDevice(
            scanResult = ScanResult(true, inputIp.value, inputPort.value.toInt()),
            onConnect = { dadb ->
              if (dadb != null) {
                onConnect(dadb)
              } else {
                connectError.value = true
              }
              state.value = DeviceState.Initial
            },
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun DeviceScanRow(
  scanResult: ScanResult,
  onClick: (ScanResult) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    elevation = 2.dp,
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick(scanResult) }
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
    ) {
      Text(
        text = "${scanResult.address}:${scanResult.port}",
      )
    }
  }
}

@Composable
private fun SelectRow(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: ScanResult? = null,
  expanded: Boolean = false,
) {
  Box(
    modifier = modifier
  ) {
    if (selected != null) {
      DeviceScanRow(
        scanResult = selected,
        onClick = { onClick() }
      )
    } else {
      Card(
        elevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onClick() }
      ) {
        Row(
          modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
        ) {
          Text(
            text = "Select device",
          )
        }
      }
    }
    Icon(
      imageVector = Icons.Filled.ArrowDropDown,
      contentDescription = "Open/close",
      modifier = Modifier.align(Alignment.CenterEnd)
        .padding(end = 20.dp)
        .then(if (expanded) Modifier.rotate(180f) else Modifier)
    )
  }
}

private sealed class DeviceState {
  object Initial : DeviceState()
  object Connecting : DeviceState()
}
