import java.io.File
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

private val adbPrivateKeyFile = File(System.getenv("HOME"), ".android/adbkey")
private val adbPublicKeyFile = File(System.getenv("HOME"), ".android/adbkey.pub")
private val lineSeparator = System.getProperty("line.separator")

private const val RSA_BEGIN = "-----BEGIN PRIVATE KEY-----"
private const val RSA_END = "-----END PRIVATE KEY-----"

private const val ANDROID_PUBKEY_MODULUS_SIZE = (2048 / 8) // 256 bytes
private const val ANDROID_PUBKEY_ENCODED_SIZE =
  (3 * 4 + 2 * ANDROID_PUBKEY_MODULUS_SIZE) // 524 bytes
private const val ANDROID_PUBKEY_MODULUS_SIZE_WORDS = (ANDROID_PUBKEY_MODULUS_SIZE / 4) // 64 bytes

/**
 * Generates new ADB keys needed to connect to an external device.
 * Does nothing if already exists.
 */
fun generateAdbKeyPair() {
  if (adbPrivateKeyFile.exists() && adbPublicKeyFile.exists()) return
  val (private, public) = generateRsaKeyPair()
  generatePrivateKey(private).let { adbPrivateKeyFile.writeText(it) }
  generatePublicKey(private, public).let { adbPublicKeyFile.writeText(it) }
}

private fun generateRsaKeyPair() = KeyPairGenerator.getInstance("RSA")
  .apply { initialize(2048) }
  .genKeyPair()
  .let { Pair(it.private as RSAPrivateKey, it.public as RSAPublicKey) }

private fun generatePrivateKey(key: PrivateKey): String {
  val encoder = Base64.getMimeEncoder(64, lineSeparator.toByteArray())
  val encoded = encoder.encodeToString(key.encoded)
  return "$RSA_BEGIN$lineSeparator$encoded$lineSeparator$RSA_END"
}

/**
 * Generates a specific custom public key that android uses.
 * This code has been ported from android's original source code:
 * https://cs.android.com/android/platform/superproject/+/master:system/core/libcrypto_utils/android_pubkey.cpp;l=127;drc=master;bpv=0;bpt=1
 */
private fun generatePublicKey(key: RSAPrivateKey, public: RSAPublicKey): String {
  val modulusSize = ANDROID_PUBKEY_MODULUS_SIZE_WORDS

  // n0inv = -1 / modulus mod 2^32
  val n0Inv = BigInteger.valueOf(32).let { r32 ->
    key.modulus
      .mod(r32)
      .modInverse(r32)
      .let { r32.subtract(it) }
      .intValueExact()
  }

  val modulus = key.modulus.toByteArrayWithoutSignBit().padded()

  // rr = (2^(rsa_size)) ^ 2 mod N.
  val rr = BigInteger.TWO.pow(ANDROID_PUBKEY_MODULUS_SIZE * 8)
    .pow(2)
    .mod(key.modulus)
    .toByteArrayWithoutSignBit()
    .padded()

  val value = ByteBuffer.allocate(ANDROID_PUBKEY_ENCODED_SIZE).let {
    it.order(LITTLE_ENDIAN)
    it.putInt(modulusSize)
    it.putInt(n0Inv)
    it.put(modulus)
    it.put(rr)
    it.putInt(public.publicExponent.intValueExact())
  }.array()

  val encoder = Base64.getEncoder()
  val keyBase64 = encoder.encodeToString(value)

  val loginName = System.getProperty("user.name")
  val hostName = InetAddress.getLocalHost().hostName

  return "$keyBase64 $loginName@$hostName"
}


/**
 * When calling [BigInteger.toByteArray] it will include a single byte
 * indicating the sign (`+` or`-`). Call this if you don't care about that byte.
 */
private fun BigInteger.toByteArrayWithoutSignBit() = toByteArray().let {
  if (it[0] != 0.toByte()) it else it.copyOfRange(1, it.size)
}

private fun ByteArray.padded(): ByteArray {
  if (ANDROID_PUBKEY_MODULUS_SIZE < size)
    error("Padded length ($size) is smaller then given size (${ANDROID_PUBKEY_MODULUS_SIZE})")

  return if (ANDROID_PUBKEY_MODULUS_SIZE == size) {
    this
  } else {
    copyOf(ANDROID_PUBKEY_MODULUS_SIZE)
  }
}
