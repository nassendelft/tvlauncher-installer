import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.net.*


private const val MIN_PORT = 5555
private const val MAX_PORT = 5683

private val networkInterface = NetworkInterface.getNetworkInterfaces()
  .asSequence()
  .filter { !it.isLoopback && it.isUp }
  .filter { it.inetAddresses.asSequence().find { address -> address is Inet4Address } != null }
  .firstOrNull()
  ?.interfaceAddresses
  ?.find { it.address is Inet4Address }
private val prefixLength = networkInterface?.networkPrefixLength
private val inet4Address = networkInterface?.address

/**
 * Scans for android devices on local network that have ADB enabled.
 * Only devices we can connect to will be emitted.
 *
 * Checks only IPv4 address space.
 *
 * @param port only search given port. This will significantly reduce the amount of time to scan
 * because it only needs to check 1 port per host instead of the `MAX_PORT - MIN_PORT` range.
 * When null is given it will scan all valid port ranges per host.
 */
fun scanDevicesOnNetwork(port: Int? = MIN_PORT): Flow<ScanResult> {
  checkNotNull(networkInterface) { "Could not find interface with ipv4 address" }
  checkNotNull(prefixLength) { "Could not get the network mask from default (first) network device" }
  checkNotNull(inet4Address) { "Could not get the IP address from default (first) network device" }

  return when (prefixLength) {
    8.toShort() -> scan8(inet4Address, port)
    16.toShort() -> scan16(inet4Address, port)
    24.toShort() -> scan24(inet4Address, port)
    else -> error("unsupported mask: $prefixLength")
  }.flowOn(IO)
}

private fun scan8(inetAddress: InetAddress, port: Int?) = channelFlow {
  (0x01..0xFF)
    .map { i -> inetAddress.address.copyOf().also { it[1] = i.toByte() } }
    .map { Inet4Address.getByAddress(it) }
    .forEach { launch { scan16(it, port).collect(::send) } }
}

private fun scan16(inetAddress: InetAddress, port: Int?) = channelFlow {
  (0x01..0xFF)
    .map { i -> inetAddress.address.copyOf().also { it[2] = i.toByte() } }
    .map { Inet4Address.getByAddress(it) }
    .forEach { launch { scan24(it, port).collect(::send) } }
}

private fun scan24(inetAddress: InetAddress, port: Int?) = channelFlow {
  (0x01..0xFF)
    .map { i -> inetAddress.address.copyOf().also { it[3] = i.toByte() } }
    .map { InetAddress.getByAddress(it).hostAddress }
    .forEach { launch { check(it, port).collect(::send) } }
}

private fun check(address: String, port: Int?) = channelFlow {
  val doCheck = { p: Int ->
    launch {
      val canConnect = connectToPort(address, p)
      val result = ScanResult(canConnect, address, p)
      send(result)
    }
  }

  if (port == null) {
    (MIN_PORT..MAX_PORT).forEach { doCheck(it) }
  } else {
    doCheck(port)
  }
}

private fun connectToPort(address: String, port: Int) = try {
  // try to connect to this port with a low timeout
  Socket().apply {
    connect(InetSocketAddress(address, port), 500)
  }.close()
  true
} catch (_: Throwable) {
  false
}

data class ScanResult(
  val couldConnect: Boolean,
  val address: String,
  val port: Int
)

