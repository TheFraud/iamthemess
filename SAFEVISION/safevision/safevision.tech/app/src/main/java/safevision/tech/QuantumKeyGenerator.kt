package safevision.tech

import org.openquantumsafe.*

class QuantumKeyGenerator {

    companion object {
        private const val ALGORITHM_KYBER_512 = "KYBER_512" // Algorithme par défaut
    }

    /**
     * Vérifie si un algorithme est supporté par liboqs.
     * @param algorithm Nom de l'algorithme.
     * @return true si l'algorithme est supporté, sinon false.
     */
    private fun isAlgorithmSupported(algorithm: String): Boolean {
        return try {
            OQSKeyGenerator.getSupportedAlgorithms().contains(algorithm)
        } catch (e: Exception) {
            throw QuantumKeyException("Impossible de vérifier les algorithmes supportés : ${e.message}", e)
        }
    }

    /**
     * Génère une paire de clés en utilisant l'algorithme Kyber512.
     * @return Une instance de [OQSKeyPair].
     */
    fun generateKyberKeys(): OQSKeyPair {
        require(isAlgorithmSupported(ALGORITHM_KYBER_512)) { "Algorithme $ALGORITHM_KYBER_512 non supporté." }
        return try {
            OQSKeyGenerator(ALGORITHM_KYBER_512).generateKeyPair()
        } catch (e: Exception) {
            throw QuantumKeyException("Erreur lors de la génération de la paire de clés : ${e.message}", e)
        }
    }

    /**
     * Récupère la clé publique d'une paire de clés.
     * @param keyPair La paire de clés générée.
     * @return Une instance de [OQSPublicKey].
     */
    fun getPublicKey(keyPair: OQSKeyPair): OQSPublicKey {
        return keyPair.publicKey
    }

    /**
     * Récupère la clé privée d'une paire de clés.
     * @param keyPair La paire de clés générée.
     * @return Une instance de [OQSPrivateKey].
     */
    fun getPrivateKey(keyPair: OQSKeyPair): OQSPrivateKey {
        return keyPair.privateKey
    }

    /**
     * Sérialise une clé publique pour la stocker ou la transmettre.
     * @param publicKey La clé publique à sérialiser.
     * @return Un tableau de bytes représentant la clé encodée.
     */
    fun serializePublicKey(publicKey: OQSPublicKey): ByteArray {
        return try {
            publicKey.encode()
        } catch (e: Exception) {
            throw QuantumKeyException("Erreur lors de la sérialisation de la clé publique : ${e.message}", e)
        }
    }

    /**
     * Sérialise une clé privée pour la stocker en toute sécurité.
     * @param privateKey La clé privée à sérialiser.
     * @return Un tableau de bytes représentant la clé encodée.
     */
    fun serializePrivateKey(privateKey: OQSPrivateKey): ByteArray {
        return try {
            privateKey.encode()
        } catch (e: Exception) {
            throw QuantumKeyException("Erreur lors de la sérialisation de la clé privée : ${e.message}", e)
        }
    }

    /**
     * Désérialise une clé publique à partir de son encodage.
     * @param encodedKey Un tableau de bytes représentant la clé publique encodée.
     * @return Une instance de [OQSPublicKey].
     */
    fun deserializePublicKey(encodedKey: ByteArray): OQSPublicKey {
        require(encodedKey.isNotEmpty()) { "La clé publique encodée est vide." }
        return try {
            OQSPublicKey.decode(encodedKey)
        } catch (e: Exception) {
            throw QuantumKeyException("Erreur lors de la désérialisation de la clé publique : ${e.message}", e)
        }
    }

    /**
     * Désérialise une clé privée à partir de son encodage.
     * @param encodedKey Un tableau de bytes représentant la clé privée encodée.
     * @return Une instance de [OQSPrivateKey].
     */
    fun deserializePrivateKey(encodedKey: ByteArray): OQSPrivateKey {
        require(encodedKey.isNotEmpty()) { "La clé privée encodée est vide." }
        return try {
            OQSPrivateKey.decode(encodedKey)
        } catch (e: Exception) {
            throw QuantumKeyException("Erreur lors de la désérialisation de la clé privée : ${e.message}", e)
        }
    }

    /**
     * Génère une paire de clés, les sérialise, et retourne les clés encodées.
     * @return Un [Pair] contenant la clé publique et la clé privée encodées.
     */
    fun generateAndSerializeKeys(): Pair<ByteArray, ByteArray> {
        val keyPair = generateKyberKeys()
        val publicKey = serializePublicKey(keyPair.publicKey)
        val privateKey = serializePrivateKey(keyPair.privateKey)
        return Pair(publicKey, privateKey)
    }
}

/**
 * Exception personnalisée pour les erreurs liées à la génération et gestion des clés quantiques.
 */
class QuantumKeyException(message: String, cause: Throwable? = null) : Exception(message, cause)
