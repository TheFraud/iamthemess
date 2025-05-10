package safevision.tech

import org.xrpl.xrpl4j.client.XrplClient
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult
import org.xrpl.xrpl4j.model.transactions.Payment
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class XRPService(private val serverUri: URI = URI("https://s.altnet.rippletest.net:51234")) {

    private val client: XrplClient = XrplClient(serverUri)

    companion object {
        private const val XRP_ADDRESS_REGEX = "^r[1-9A-HJ-NP-Za-km-z]{25,34}$"

        /**
         * Valide une adresse XRP en utilisant une expression régulière.
         */
        fun isValidXRPAddress(address: String): Boolean {
            val pattern = Pattern.compile(XRP_ADDRESS_REGEX)
            return pattern.matcher(address).matches()
        }
    }

    /**
     * Récupère les informations d'un compte XRPL.
     * @param account Adresse XRPL valide
     * @return CompletableFuture contenant le résultat ou null en cas d'erreur
     */
    fun getAccountInfo(account: String): CompletableFuture<AccountInfoResult?> {
        require(isValidXRPAddress(account)) { "Adresse XRPL invalide : $account" }

        val request = AccountInfoRequestParams.builder()
            .account(account)
            .build()

        return client.accountInfo(request).exceptionally { ex ->
            logError("Erreur lors de la récupération des informations du compte : ${ex.message}")
            null
        }
    }

    /**
     * Crée une transaction de paiement XRPL.
     * @param fromAccount Adresse XRPL de l'expéditeur
     * @param toAccount Adresse XRPL du destinataire
     * @param amount Montant en "drops" (1 XRP = 1,000,000 drops)
     * @return Objet Payment prêt à être signé et soumis
     */
    private fun createPayment(fromAccount: String, toAccount: String, amount: Long): Payment {
        require(isValidXRPAddress(fromAccount)) { "Adresse XRPL invalide pour l'expéditeur." }
        require(isValidXRPAddress(toAccount)) { "Adresse XRPL invalide pour le destinataire." }
        require(amount > 0) { "Le montant doit être supérieur à zéro." }

        return Payment.builder()
            .account(fromAccount)
            .destination(toAccount)
            .amount(XrpCurrencyAmount.ofDrops(amount))
            .build()
    }

    /**
     * Soumet une transaction de paiement XRPL.
     * @param fromAccount Adresse XRPL de l'expéditeur
     * @param toAccount Adresse XRPL du destinataire
     * @param amount Montant en drops
     * @param privateKey Clé privée pour signer la transaction
     * @return CompletableFuture contenant le hash de la transaction ou un message d'erreur
     */
    fun sendPayment(
        fromAccount: String,
        toAccount: String,
        amount: Long,
        privateKey: String
    ): CompletableFuture<String> {
        require(privateKey.isNotEmpty()) { "La clé privée ne peut pas être vide." }

        val payment = createPayment(fromAccount, toAccount, amount)

        return try {
            client.submit(payment).thenApply { result ->
                if (result.isSuccessful) {
                    logDebug("Transaction réussie : ${result.transactionHash}")
                    "Transaction réussie : ${result.transactionHash}"
                } else {
                    logError("Transaction échouée avec le statut : ${result.status}")
                    "Transaction échouée avec le statut : ${result.status}"
                }
            }.exceptionally { ex ->
                logError("Erreur lors de l'envoi de la transaction : ${ex.message}")
                "Erreur lors de l'envoi de la transaction : ${ex.message}"
            }
        } catch (e: Exception) {
            CompletableFuture.completedFuture("Erreur lors de la transaction : ${e.message}")
        }
    }

    /**
     * Journalisation sécurisée pour le mode DEBUG.
     */
    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            println("DEBUG: $message")
        }
    }

    /**
     * Gestion des erreurs avec journalisation.
     */
    private fun logError(message: String) {
        println("ERROR: $message")
    }
}
