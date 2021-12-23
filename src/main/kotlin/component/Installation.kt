package component

import APP_TV_LAUNCHER_CUSTOM
import DownloadState
import Release
import RequestState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dadb.Dadb
import downloadFile
import getReleases
import install2
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.nio.file.Files

@Composable
fun Installation(
  dadb: Dadb,
  onInstalled: (Dadb) -> Unit,
  modifier: Modifier = Modifier
) {
  val downloadUrl = remember { mutableStateOf<HttpUrl?>(null) }
  val downloadedFile = remember { mutableStateOf<File?>(null) }
  val installationState = dadb.produceAppInstallationState(APP_TV_LAUNCHER_CUSTOM)

  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      painter = painterResource("icons/logo_placeholder.png"),
      contentDescription = "Logo",
      modifier = Modifier.size(200.dp)
    )
    Spacer(
      modifier = Modifier.height(46.dp)
    )
    when (installationState.value) {
      is AppInstallationState.Retrieving -> {
        CircularProgressIndicator()
      }
      is AppInstallationState.Installed -> {
        Text(
          text = "App is already installed, continue to next step."
        )
        Spacer(
          modifier = Modifier.height(8.dp)
        )
        Button(
          onClick = { onInstalled(dadb) }
        ) {
          Text(
            text = "Next"
          )
        }
      }
      else -> {
        Text(
          text = "App not installed, download and install to continue."
        )
        Spacer(
          modifier = Modifier.height(46.dp)
        )
        if (downloadedFile.value == null) {
          downloadUrl.value?.let { url ->
            DownloadStatus(
              url = url,
              onDownloaded = { downloadedFile.value = it }
            )
          }
          if (downloadUrl.value == null) {
            StartDownload(
              onClick = { downloadUrl.value = it }
            )
          }
        }
        downloadedFile.value?.let {
          InstallApp(
            dadb = dadb,
            file = it,
            onInstalled = { onInstalled(dadb) }
          )
        }
      }
    }
  }
}

@Composable
private fun StartDownload(
  onClick: (HttpUrl) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val releaseState = remember { mutableStateOf<RequestState<Release>>(RequestState.Initial()) }
  LaunchedEffect(Unit) {
    getReleases().collect { releaseState.value = it }
  }

  when (releaseState.value) {
    is RequestState.Failure -> {
      Text(
        text = "Could not fetch data",
        color = MaterialTheme.colors.error,
        modifier = Modifier
      )
      Button(
        onClick = { getReleases().onEach { releaseState.value = it }.launchIn(scope) }
      ) {
        Text(
          text = "Try again",
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
    is RequestState.Success -> {
      val response = (releaseState.value as RequestState.Success<Release>).response
      Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Release ${response?.version}",
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
          onClick = { response?.url?.toHttpUrl()?.let { onClick(it) } }
        ) {
          Icon(
            painter = painterResource("icons/get_app_black_24dp.svg"),
            contentDescription = "Download",
          )
        }
      }
    }
    is RequestState.Requested,
    is RequestState.Initial -> {
      CircularProgressIndicator(
        modifier = modifier.size(40.dp)
      )
    }
  }
}

@Composable
private fun DownloadStatus(
  url: HttpUrl,
  onDownloaded: (File) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val downloadFile = remember {
    Files.createTempFile(null, ".apk").toFile().also { it.deleteOnExit() }
  }
  val downloadState = remember { mutableStateOf<DownloadState>(DownloadState.Initial) }
  val downloadAction = downloadFile(url, downloadFile)
    .conflate()
    .onEach { downloadState.value = it }
    .catch { emit(DownloadState.Failure(it)) }
    .onCompletion {
      if (downloadState.value is DownloadState.Success) onDownloaded(downloadFile)
    }
    .flowOn(IO)

  LaunchedEffect(Unit) {
    downloadAction.collect()
  }

  val progress = when (val state = downloadState.value) {
    is DownloadState.Progress -> state.bytesRead.toFloat() / state.contentLength.toFloat()
    DownloadState.Success,
    is DownloadState.Failure -> 1.0f
    DownloadState.Initial,
    DownloadState.Requested -> 0.0f
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Downloading",
      modifier = Modifier.padding(bottom = 4.dp)
    )
    LinearProgressIndicator(
      progress = progress,
      color = if (downloadState.value is DownloadState.Failure)
        MaterialTheme.colors.error
      else
        MaterialTheme.colors.primary
    )
  }
  if (downloadState.value is DownloadState.Failure) {
    Text(
      text = "Could not download this release",
      color = MaterialTheme.colors.error,
      modifier = Modifier
    )
    Button(
      onClick = {
        downloadAction.launchIn(scope)
      }
    ) {
      Text(
        text = "Try again",
        modifier = Modifier.padding(top = 4.dp)
      )
    }
  }
}

@Composable
fun InstallApp(
  dadb: Dadb,
  file: File,
  onInstalled: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val installingState = rememberActionState<Boolean>()
  val canInstall = installingState.value is ActionState.Initial
    || installingState.value is ActionState.Failed

  val install = {
    installingState.value = ActionState.Executing()
    if (dadb.install2(file)) {
      onInstalled()
    } else {
      installingState.value = ActionState.Failed()
    }
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box {
      Button(
        enabled = canInstall,
        onClick = {
          scope.launch(IO) { install() }
        },
        modifier = modifier
      ) {
        Text(
          text = "Install",
        )
      }
      if (installingState.value is ActionState.Executing) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center).size(20.dp)
        )
      }
    }
    if (installingState.value is ActionState.Failed) {
      Text(
        text = "Error - Could not install",
        color = MaterialTheme.colors.error
      )
    }
  }
}
