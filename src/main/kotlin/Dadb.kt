import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

const val APP_TV_LAUNCHER_DEFAULT = "com.google.android.tvlauncher"
const val APP_TV_LAUNCHER_CUSTOM = "nl.ncaj.tvlauncher"

suspend fun connectToAdbDevice(address: String, port: Int): Dadb? = withContext(IO) {
  try {
    val dadb = Dadb.create(address, port, AdbKeyPair.readDefault())
    val response = dadb.shell("echo success")
    if (response.allOutput != "success\n") null
    else dadb
  } catch (e: Throwable) {
    null
  }
}

fun Dadb.install2(file: File, vararg options: String): Boolean {
  val sdkVersion = shell("getprop ro.build.version.sdk").output.trimEnd().toInt()
  return if (sdkVersion <= 28) {
    push(file, "/data/local/tmp/${file.name}")
    val command = listOf("pm", "install") + options
    val result = shell((command + "/data/local/tmp/${file.name}").joinToString(" "))
    result.output.trim() == "Success"
  } else {
    try {
      install(file, *options)
      true
    } catch (_: IOException) {
      false
    }
  }
}

suspend fun Dadb.enableApp(packageName: String): Unit = withContext(IO) {
  shell("pm enable $packageName").output.trim()
}

suspend fun Dadb.disableApp(packageName: String) = withContext(IO) {
  shell("pm disable-user $packageName")
    .output.trim() == "Package $packageName new state: disabled-user"
}


suspend fun Dadb.getInstalledApps() = withContext(IO) {
  shell("pm list packages").output
    .split("\n")
    .filter { it.isNotBlank() }
    .map { it.split(":")[1] }
}

suspend fun Dadb.isAppInstalled(packageName: String) = getInstalledApps()
  .find { it == packageName } != null


suspend fun Dadb.getDisabledApps() = withContext(IO) {
  shell("pm list packages -d").output
    .split("\n")
    .filter { it.isNotBlank() }
    .map { it.split(":")[1] }
}

suspend fun Dadb.isAppEnabled(packageName: String) = getDisabledApps()
  .find { it == packageName } == null


suspend fun Dadb.getProductInfo() = withContext(IO) {
  DadbProduct(
    shell("getprop ro.product.model").output,
    shell("getprop ro.product.name").output,
    shell("getprop ro.product.device").output
  )
}

data class DadbProduct(
  val model: String,
  val name: String,
  val device: String
)
