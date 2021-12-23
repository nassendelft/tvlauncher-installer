import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*


class ProgressInterceptor(private val progressListener: Progress) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalResponse = chain.proceed(chain.request())
    return originalResponse.newBuilder()
      .body(originalResponse.body?.let { ProgressResponseBody(it, progressListener) })
      .build()
  }

  private class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: Progress
  ) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType() = responseBody.contentType()

    override fun contentLength() = responseBody.contentLength()

    override fun source() = bufferedSource
      ?: source(responseBody.source()).buffer().also { bufferedSource = it }

    private fun source(source: Source): Source {
      return object : ForwardingSource(source) {
        var totalBytesRead = 0L

        override fun read(sink: Buffer, byteCount: Long): Long {
          val bytesRead = super.read(sink, byteCount)
          // read() returns the number of bytes read, or -1 if this source is exhausted.
          totalBytesRead += if (bytesRead != -1L) bytesRead else 0
          progressListener.update(
            bytesRead = totalBytesRead,
            contentLength = responseBody.contentLength(),
            done = bytesRead == -1L
          )
          return bytesRead
        }
      }
    }
  }

  interface Progress {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
  }
}
