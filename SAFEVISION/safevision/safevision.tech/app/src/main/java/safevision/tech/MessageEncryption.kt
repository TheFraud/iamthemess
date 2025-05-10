package safevision.tech

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class MessageEncryption {

    companion object {
        private const val AES_KEY_SIZE = 128 // Clé AES de 128 bits
        private const val GCM_IV_LENGTH = 12 // Taille de l'IV pour AES/GCM
        private const val GCM_TAG_LENGTH = 128 // Taille du tag GCM
    }

    /**
     * Génère une clé symétrique (AES) de 128 bits.
     * @return [SecretKey] La clé symétrique générée.
     */
    fun generateSymmetricKey(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(AES_KEY_SIZE)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            throw EncryptionException("Erreur lors de la génération de la clé symétrique : ${e.message}", e)
        }
    }

    /**
     * Chiffre un message en utilisant une clé symétrique.
     * @param message Le message en clair à chiffrer.
     * @param secretKey La clé symétrique pour le chiffrement.
     * @return Le message chiffré encodé en Base64.
     */
    fun encryptMessage(message: String, secretKey: SecretKey): String {
        require(message.isNotEmpty()) { "Le message ne peut pas être vide." }

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val encryptedBytes = cipher.doFinal(message.toByteArray())
            val ivAndCiphertext = iv + encryptedBytes
            Base64.getEncoder().encodeToString(ivAndCiphertext)
        } catch (e: Exception) {
            throw EncryptionException("Erreur lors du chiffrement : ${e.message}", e)
        }
    }

    /**
     * Déchiffre un message en utilisant une clé symétrique.
     * @param encryptedMessage Le message chiffré en Base64.
     * @param secretKey La clé symétrique pour le déchiffrement.
     * @return Le message déchiffré en clair.
     */
    fun decryptMessage(encryptedMessage: String, secretKey: SecretKey): String {
        require(encryptedMessage.isNotEmpty()) { "Le message chiffré ne peut pas être vide." }

        return try {
            val decodedBytes = Base64.getDecoder().decode(encryptedMessage)
            val iv = decodedBytes.sliceArray(0 until GCM_IV_LENGTH)
            val ciphertext = decodedBytes.sliceArray(GCM_IV_LENGTH until decodedBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes)
        } catch (e: Exception) {
            throw EncryptionException("Erreur lors du déchiffrement : ${e.message}", e)
        }
    }

    /**
     * Sérialise une clé symétrique en tableau d'octets.
     * @param secretKey La clé symétrique à sérialiser.
     * @return Un tableau d'octets représentant la clé.
     */
    fun serializeSecretKey(secretKey: SecretKey): ByteArray {
        return secretKey.encoded ?: throw EncryptionException("Clé non sérialisable.", null)
    }

    /**
     * Désérialise un tableau d'octets en clé symétrique.
     * @param serializedKey Le tableau d'octets représentant la clé.
     * @return [SecretKey] La clé symétrique reconstruite.
     */
    fun deserializeSecretKey(serializedKey: ByteArray): SecretKey {
        require(serializedKey.isNotEmpty()) { "La clé sérialisée ne peut pas être vide." }
        require(serializedKey.size == 16 || serializedKey.size == 24 || serializedKey.size == 32) {
            "Clé AES invalide. Longueur a
