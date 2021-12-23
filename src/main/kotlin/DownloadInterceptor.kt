import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File


class DownloadInterceptor(private val file: File) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalResponse = chain.proceed(chain.request())

    return originalResponse.body?.let {
      it.byteStream().copyTo(file.outputStream())

      originalResponse.newBuilder()
        .body(byteArrayOf().toResponseBody(it.contentType()))
        .build()
    } ?: return originalResponse
  }
}
