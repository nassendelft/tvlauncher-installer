import okhttp3.Headers.Companion.headersOf
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


private val releasesUrl = "https://api.github.com/repos/nassendelft/tvlauncher/releases".toHttpUrl()

private val releasesRequestHeaders = headersOf("Accept", "application/vnd.github.v3+json")

private val releasesParser = { body: String? ->
  val jsonResponse = body?.let { JSONArray(body) }
  if (jsonResponse == null || jsonResponse.isEmpty) error("Could not get release response")
  jsonResponse.getJSONObject(0).asRelease
}

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

private val JSONObject.asRelease
  get() = getJSONArray("assets").getJSONObject(0).let { asset ->
    Release(
      version = getString("tag_name").substring(1), // removes the 'v' prefix,
      date = getString("published_at").let { dateFormat.parse(it) },
      size = asset.getLong("size"),
      name = asset.getString("name"),
      url = asset.getString("browser_download_url")
    )
  }

fun getReleases() = httpRequest(
  url = releasesUrl,
  headers = releasesRequestHeaders,
  parser = releasesParser
)

data class Release(
  val version: String,
  val date: Date,
  val size: Long,
  val name: String,
  val url: String
)
