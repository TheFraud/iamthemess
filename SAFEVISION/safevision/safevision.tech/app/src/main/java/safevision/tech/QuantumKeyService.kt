package safevision.tech

import org.openquantumsafe.*
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class QuantumKeyService {

    companion object {
        private const val SUPPORTED_ALGORITHM = "KYBER_512" // Algorithme supporté
    }

    /**
     * Vérifie si l'algorithme spécifié est supporté.
     * @param algorithm Nom de l'algorithme.
     * @return true si l'algorithme est supporté, sinon false.
     */
    private fun isAlgorithmSupported(algorithm: String): Boolean {
        return try {
            OQSKeyPair.getSupportedAlgorithms().contains(algorithm)
        } catch (e: Exception) {
            throw IllegalStateException("Impossible de vérifier les algorithmes supportés : ${e.message}", e)
        }
    }

    /**
     * Génère une paire de clés post-quantiques utilisant KYBER_512.
     * @return Une instance de [OQSKeyPair].
     */
    fun generateQuantumKeyPair(): OQSKeyPair {
        require(isAlgorithmSupported(SUPPORTED_ALGORITHM)) { "Algorithme $SUPPORTED_ALGORITHM non supporté." }
        return try {
            OQSKeyPair.generate(SUPPORTED_ALGORITHM)
        } catch (e: Exception) {
            throw IllegalStateException("Erreur lors de la génération de la paire de clés : ${e.message}", e)
        }
    }

    /**
     * Chiffre un message avec une clé publique post-quantique.
     * @param message Le message à chiffrer.
     * @param publicKey La clé publique utilisée pour le chiffrement.
     * @return Un tableau de bytes contenant les données chiffrées.
     */
    fun encryptMessage(message: String, publicKey: OQSPublicKey): ByteArray {
        require(message.isNotEmpty()) { "Le message ne peut pas être vide." }
        return try {
            OQSCipher(publicKey).use { cipher ->
                cipher.encrypt(message.toByteArray())
            }
        } catch (e: Exception) {
            throw IllegalStateException("Erreur lors du chiffrement : ${e.message}", e)
        }
    }

    /**
     * Déchiffre un message avec une clé privée post-quantique.
     * @param encryptedMessage Les données chiffrées.
     * @param privateKey La clé privée utilisée pour le déchiffrement.
     * @return Le message déchiffré sous forme de chaîne.
     */
    fun decryptMessage(encryptedMessage: ByteArray, privateKey: OQSPrivateKey): String {
        require(encryptedMessage.isNotEmpty()) { "Les données chiffrées ne peuvent pas être vides." }
        return try {
            OQSCipher(privateKey).use { cipher ->
                val decryptedMessage = cipher.decrypt(encryptedMessage)
                String(decryptedMessage)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Erreur lors du déchiffrement : ${e.message}", e)
        }
    }

    /**
     * Génère un hachage SHA-256 pour un message donné.
     * @param message Le message à hacher.
     * @return Un tableau de bytes représentant le hachage.
     */
    fun generateMessageHash(message: String): ByteArray {
        require(message.isNotEmpty()) { "Le message ne peut pas être vide." }
        return try {
            MessageDigest.getInstance("SHA-256").digest(message.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Erreur lors de la génération du hachage : ${e.message}", e)
        }
    }

    /**
     * Génère un HMAC-SHA256 pour un message donné avec une clé spécifique.
     * @param message Le message à hacher.
     * @param key La clé utilisée pour le HMAC.
     * @return Un tableau de bytes représentant le HMAC.
     */
    fun generateHMAC(message: String, key: ByteArray): ByteArray {
        require(message.isNotEmpty()) { "Le message ne peut pas être vide." }
        require(key.isNotEmpty()) { "La clé HMAC ne peut pas être vide." }
        return try {
            val hmacSHA256 = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key, "HmacSHA256")
            hmacSHA256.init(secretKey)
            hmacSHA256.doFinal(message.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Erreur lors de la génération du HMAC : ${e.message}", e)
        }
    }
}
