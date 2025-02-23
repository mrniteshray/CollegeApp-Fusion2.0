package xcom.niteshray.apps.collegeapp.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val transformation = "AES/CBC/PKCS5Padding"
    private const val algorithm = "AES"
    
    private const val secretKeyString = "0123456789abcdef0123456789abcdef"

    private const val ivString = "abcdef9876543210"

    private val secretKey: SecretKey = SecretKeySpec(secretKeyString.toByteArray(), algorithm)
    private val iv: IvParameterSpec = IvParameterSpec(ivString.toByteArray())

    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        val encryptedBytes = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(encryptedInput: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val decodedBytes = Base64.decode(encryptedInput, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
}
