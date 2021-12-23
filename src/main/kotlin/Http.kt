import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

private val httpClient = OkHttpClient()

fun <T> httpRequest(
  url: HttpUrl,
  headers: Headers = Headers.headersOf(),
  parser: (String?) -> T,
  okHttpClient: OkHttpClient = httpClient
) = channelFlow<RequestState<T>> {
  send(RequestState.Requested())

  val request = Request.Builder()
    .headers(headers)
    .url(url)
    .build()

  okHttpClient.newCall(request).execute().use { response ->
    if (!response.isSuccessful) trySend(RequestState.Failure(IOException("Unexpected code $response")))
    else trySend(RequestState.Success(parser(response.body?.string())))
  }
}
  .catch { emit(RequestState.Failure(it)) }
  .flowOn(IO)

fun downloadFile(
  url: HttpUrl,
  destination: File,
  okHttpClient: OkHttpClient = httpClient
) = okHttpClient.downloadFile(url, destination)

private fun OkHttpClient.downloadFile(
  url: HttpUrl,
  destination: File
) = channelFlow {
  send(DownloadState.Requested)

  val progressListener = object : ProgressInterceptor.Progress {
    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
      trySendBlocking(DownloadState.Progress(bytesRead, contentLength, done))
    }

  }
  val client = newBuilder()
    .addNetworkInterceptor(DownloadInterceptor(destination))
    .addNetworkInterceptor(ProgressInterceptor(progressListener))
    .build()

  val request = Request.Builder()
    .url(url)
    .build()

  client.newCall(request).execute().use { response ->
    if (!response.isSuccessful) send(DownloadState.Failure(IOException("Unexpected code $response")))
    else send(DownloadState.Success)
  }
}

sealed class RequestState<T> {
  class Initial<T> : RequestState<T>()
  class Requested<T> : RequestState<T>()
  class Success<T>(val response: T?) : RequestState<T>()
  class Failure<T>(val exception: Throwable) : RequestState<T>()
}

sealed class DownloadState {
  object Initial : DownloadState()
  object Requested : DownloadState()
  object Success : DownloadState()
  class Failure(val exception: Throwable) : DownloadState()
  data class Progress(
    val bytesRead: Long,
    val contentLength: Long,
    val done: Boolean
  ) : DownloadState()
}
