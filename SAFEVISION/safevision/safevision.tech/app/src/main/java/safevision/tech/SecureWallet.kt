package safevision.tech

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.xrpl.xrpl4j.client.XrplClient
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory
import org.xrpl.xrpl4j.wallet.Wallet
import java.math.BigDecimal
import java.net.URI
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.Base64

class SecureWallet(private val xrplClient: XrplClient) {

    private val keyStoreAlias = "SafeVisionWalletKey"
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private var wallet: Wallet? = null

    init {
        requireNotNull(xrplClient) { "XrplClient ne peut pas être null." }
        generateEncryptionKey()
    }

    /**
     * Génère une clé de chiffrement si elle n'existe pas dans le KeyStore.
     */
    private fun generateEncryptionKey() {
        if (!keyStore.containsAlias(keyStoreAlias)) {
            try {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
                logDebug("Clé de chiffrement générée et stockée dans AndroidKeyStore.")
            } catch (e: Exception) {
                throw WalletException("Erreur lors de la génération de la clé : ${e.message}", e)
            }
        }
    }

    /**
     * Récupère la clé secrète du KeyStore.
     */
    private fun getSecretKey(): SecretKey {
        return try {
            (keyStore.getEntry(keyStoreAlias, null) as KeyStore.SecretKeyEntry).secretKey
        } catch (e: Exception) {
            throw WalletException("Impossible de récupérer la clé secrète : ${e.message}", e)
        }
    }

    /**
     * Chiffre des données avec AES/GCM et retourne une chaîne encodée en Base64.
     */
    fun encryptData(data: ByteArray): String {
        require(data.isNotEmpty()) { "Les données à chiffrer ne peuvent pas être vides." }

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data)
            Base64.getEncoder().encodeToString(iv + encryptedData)
        } catch (e: Exception) {
            throw WalletException("Échec du chiffrement : ${e.message}", e)
        }
    }

    /**
     * Déchiffre des données encodées en Base64 et retourne les données d'origine.
     */
    fun decryptData(encryptedData: String): ByteArray {
        require(encryptedData.isNotEmpty()) { "Les données chiffrées ne peuvent pas être vides." }

        return try {
            val decodedBytes = Base64.getDecoder().decode(encryptedData)
            val iv = decodedBytes.copyOfRange(0, 12)
            val encryptedContent = decodedBytes.copyOfRange(12, decodedBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
            cipher.doFinal(encryptedContent)
        } catch (e: Exception) {
            throw WalletException("Échec du déchiffrement : ${e.message}", e)
        }
    }

    /**
     * Crée un portefeuille XRPL aléatoire.
     */
    fun createXRPLWallet(): String {
        wallet = try {
            DefaultWalletFactory.getInstance().randomWallet(true)
        } catch (e: Exception) {
            throw WalletException("Erreur lors de la création du portefeuille : ${e.message}", e)
        }
        logDebug("Portefeuille créé avec l'adresse : ${wallet!!.classicAddress().value()}")
        return wallet!!.classicAddress().value()
    }

    /**
     * Vérifie le solde du portefeuille actuel.
     */
    fun checkBalance(): String {
        if (wallet == null) return "Aucun portefeuille généré."

        return try {
            val accountInfo = xrplClient.accountInfo(
                org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams.builder()
                    .account(wallet!!.classicAddress())
                    .build()
            )
            val balance = accountInfo.result().accountData().balance().toXrp()
            "Solde : $balance XRP"
        } catch (e: Exception) {
            throw WalletException("Erreur lors de la vérification du solde : ${e.message}", e)
        }
    }

    /**
     * Envoie des fonds vers un destinataire donné.
     */
    fun sendFunds(amount: BigDecimal, recipient: String): Boolean {
        require(amount > BigDecimal.ZERO) { "Le montant doit être supérieur à zéro." }
        require(recipient.startsWith("r") && recipient.length in 25..35) {
            "Adresse XRP invalide : $recipient"
        }

        if (wallet == null) {
            throw WalletException("Aucun portefeuille généré.")
        }

        return try {
            val payment = org.xrpl.xrpl4j.model.transactions.Payment.builder()
                .account(wallet!!.classicAddress())
                .destination(org.xrpl.xrpl4j.model.transactions.Address.of(recipient))
                .amount(org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount.ofXrp(amount))
                .build()

            val signedTransaction = wallet!!.sign(payment)
            val result = xrplClient.submit(signedTransaction)

            result.result().engineResult().startsWith("tesSUCCESS")
        } catch (e: Exception) {
            throw WalletException("Erreur lors de l'envoi des fonds : ${e.message}", e)
        }
    }

    /**
     * Journalisation sécurisée.
     */
    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("SecureWallet", message)
        }
    }
}

/**
 * Exception personnalisée pour les erreurs liées au portefeuille.
 */
class WalletException(message: String, cause: Throwable? = null) : Exception(message, cause)
