package safevision.tech

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import org.xrpl.xrpl4j.client.XrplClient
import safevision.tech.databinding.FragmentWalletBinding
import java.math.BigDecimal
import java.net.URI

class WalletFragment : Fragment() {

    private lateinit var binding: FragmentWalletBinding
    private val xrplClient = XrplClient(URI("https://s.altnet.rippletest.net:51234"))
    private val secureWallet = SecureWallet(xrplClient)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalletBinding.inflate(inflater, container, false)
        setupListeners()
        return binding.root
    }

    /**
     * Configure les listeners pour les actions utilisateur.
     */
    private fun setupListeners() {
        binding.generateWalletButton.setOnClickListener {
            generateWallet()
        }

        binding.checkBalanceButton.setOnClickListener {
            checkBalance()
        }

        binding.sendButton.setOnClickListener {
            sendTransaction()
        }
    }

    /**
     * Génère un portefeuille sécurisé.
     */
    private fun generateWallet() {
        secureWallet.generateWallet()
        displayWalletDetails()
        showToast("Portefeuille généré avec succès")
    }

    /**
     * Vérifie le solde du portefeuille.
     */
    private fun checkBalance() {
        coroutineScope.launch {
            showLoading(true)
            try {
                val balance = withContext(Dispatchers.IO) {
                    secureWallet.checkBalance()
                }
                binding.balanceText.text = balance
            } catch (e: Exception) {
                showToast("Erreur lors de la vérification du solde : ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * Envoie des fonds à une adresse spécifiée.
     */
    private fun sendTransaction() {
        val destination = binding.destinationInput.text.toString()
        val amount = binding.amountInput.text.toString().toDoubleOrNull()

        if (!isValidXRPAddress(destination) || amount == null || amount <= 0) {
            showToast("Veuillez entrer une adresse et un montant valides.")
            return
        }

        coroutineScope.launch {
            showLoading(true)
            try {
                val result = withContext(Dispatchers.IO) {
                    secureWallet.sendTransaction(destination, BigDecimal(amount))
                }
                showToast("Transaction réussie : $result")
                clearInputFields()
            } catch (e: Exception) {
                showToast("Échec de la transaction : ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * Affiche ou masque l'indicateur de chargement.
     */
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Vérifie si une adresse XRP est valide.
     */
    private fun isValidXRPAddress(address: String): Boolean {
        val regex = Regex("^r[1-9A-HJ-NP-Za-km-z]{25,34}$")
        return regex.matches(address)
    }

    /**
     * Affiche les détails du portefeuille.
     */
    private fun displayWalletDetails() {
        val address = secureWallet.walletAddress
        binding.walletAddress.text = if (address.isNullOrEmpty()) {
            "Aucun portefeuille généré."
        } else {
            "Adresse : $address"
        }
        binding.walletStatus.text = if (address.isNullOrEmpty()) {
            "Veuillez générer un portefeuille."
        } else {
            "Portefeuille généré avec succès."
        }
    }

    /**
     * Vide les champs de saisie après une transaction.
     */
    private fun clearInputFields() {
        binding.destinationInput.text.clear()
        binding.amountInput.text.clear()
    }

    /**
     * Affiche un message Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel() // Annule toutes les coroutines à la destruction de la vue
    }
}
