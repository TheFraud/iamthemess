package safevision.tech

import android.util.Log
import org.openquantumsafe.KeyEncapsulation
import org.openquantumsafe.Signature

object CryptoManager {
    private const val ALGORITHM_KEM = "Kyber512" // Échange de clés
    private const val ALGORITHM_SIGNATURE = "Dilithium2" // Signature numérique

    private val keyEncapsulation: KeyEncapsulation = KeyEncapsulation(ALGORITHM_KEM)
    private val signature: Signature = Signature(ALGORITHM_SIGNATURE)

    init {
        // Validation des algorithmes au démarrage
        require(KeyEncapsulation.supported_kems().contains(ALGORITHM_KEM)) {
            "Algorithm $ALGORITHM_KEM is not supported by OpenQuantumSafe"
        }
        require(Signature.supported_sigs().contains(ALGORITHM_SIGNATURE)) {
            "Algorithm $ALGORITHM_SIGNATURE is not supported by OpenQuantumSafe"
        }
        logDebug("CryptoManager initialized successfully with $ALGORITHM_KEM and $ALGORITHM_SIGNATURE.")
    }

    /**
     * Génère une paire de clés (publique et privée).
     */
    fun generateKeyPair(): Pair<ByteArray, ByteArray> = try {
        val publicKey = keyEncapsulation.generate_keypair()
        val privateKey = keyEncapsulation.export_secret_key()
        logDebug("Key pair generated successfully.")
        Pair(publicKey, privateKey)
    } catch (e: Exception) {
        throw CryptoException("Failed to generate key pair", e)
    } finally {
        keyEncapsulation.clear()
    }

    /**
     * Chiffre des données avec une clé publique.
     */
    fun encryptWithPublicKey(data: ByteArray, publicKey: ByteArray): ByteArray = try {
        validateInputs(data, publicKey)
        keyEncapsulation.encapsulate(publicKey).also {
            logDebug("Data encrypted successfully.")
        }
    } catch (e: Exception) {
        throw CryptoException("Failed to encrypt data", e)
    } finally {
        keyEncapsulation.clear()
    }

    /**
     * Déchiffre des données avec une clé privée.
     */
    fun decryptWithPrivateKey(cipherText: ByteArray, privateKey: ByteArray): ByteArray = try {
        validateInputs(cipherText, privateKey)
        keyEncapsulation.import_secret_key(privateKey)
        keyEncapsulation.decapsulate(cipherText).also {
            logDebug("Data decrypted successfully.")
        }
    } catch (e: Exception) {
        throw CryptoException("Failed to decrypt data", e)
    } finally {
        keyEncapsulation.clear()
    }

    /**
     * Signe des données avec une clé privée.
     */
    fun signData(data: ByteArray, privateKey: ByteArray): ByteArray = try {
        validateInputs(data, privateKey)
        signature.import_secret_key(privateKey)
        signature.sign(data).also {
            logDebug("Data signed successfully.")
        }
    } catch (e: Exception) {
        throw CryptoException("Failed to sign data", e)
    } finally {
        signature.clear()
    }

    /**
     * Vérifie une signature avec une clé publique.
     */
    fun verifySignature(data: ByteArray, signatureData: ByteArray, publicKey: ByteArray): Boolean = try {
        validateInputs(data, signatureData, publicKey)
        signature.import_public_key(publicKey)
        signature.verify(data, signatureData).also {
            logDebug("Signature verification result: $it")
        }
    } catch (e: Exception) {
        throw CryptoException("Failed to verify signature", e)
    } finally {
        signature.clear()
    }

    /**
     * Validation des entrées pour éviter les erreurs silencieuses.
     */
    private fun validateInputs(vararg inputs: ByteArray) {
        for (input in inputs) {
            require(input.isNotEmpty()) { "Input data cannot be empty." }
        }
    }

    /**
     * Gestion des logs sécurisée.
     */
    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("CryptoManager", message)
        }
    }
}

/**
 * Exception personnalisée pour les erreurs liées à la cryptographie.
 */
class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)
