package safevision.tech

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val xrpService = XRPService()
    private lateinit var accountBalanceTextView: TextView
    private lateinit var progressBar: ProgressBar
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Ajout des nouveaux composants
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var webRTCManager: WebRTCManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        accountBalanceTextView = findViewById(R.id.accountBalanceTextView)
        progressBar = findViewById(R.id.progressBar)

        // Configuration du NavController
        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)

        // Initialiser WebSocket et WebRTC
        initializeWebSocketAndWebRTC()

        // Exemple d'adresse XRP
        val xrpAddress = "rA8FHjGydwY4vUAxwwfQ6xwBB6YCb1nHs"

        // Récupération des informations du compte
        fetchAccountInfo(xrpAddress)
    }

    /**
     * Initialise le client WebSocket et WebRTCManager.
     */
    private fun initializeWebSocketAndWebRTC() {
        val serverUrl = "ws://10.0.2.2:8080" // Adresse du serveur WebSocket (remplacez si nécessaire)

        try {
            // Initialiser le client WebSocket
            webSocketClient = WebSocketClient(serverUrl)
            val userId = "user_${System.currentTimeMillis()}" // Exemple d'identifiant unique
            webSocketClient.connect(userId)

            // Initialiser WebRTCManager
            webRTCManager = WebRTCManager(this, webSocketClient)
            webRTCManager.createPeerConnection()

            // Écoute des connexions établies
            webRTCManager.onConnectionEstablished = {
                runOnUiThread {
                    Toast.makeText(this, "Connexion WebRTC établie avec succès !", Toast.LENGTH_SHORT).show()
                }
            }

            // Écoute des messages reçus
            webRTCManager.onMessageReceived = { message ->
                runOnUiThread {
                    Toast.makeText(this, "Message reçu : $message", Toast.LENGTH_LONG).show()
                }
            }

            // Gérer les erreurs
            webRTCManager.onError = { errorMessage ->
                runOnUiThread {
                    Toast.makeText(this, "Erreur WebRTC : $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            // Envoyer une offre SDP pour établir la connexion
            coroutineScope.launch {
                delay(1000) // Attendre que la connexion WebSocket soit établie
                webRTCManager.createOffer()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors de l'initialisation de WebSocket/WebRTC : ${e.message}")
            Toast.makeText(this, "Échec de l'initialisation : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Valide une adresse XRP.
     */
    private fun isValidXRPAddress(address: String): Boolean {
        val regex = Regex("^r[1-9A-HJ-NP-Za-km-z]{25,34}$")
        return regex.matches(address)
    }

    /**
     * Récupère les informations d'un compte XRPL et met à jour l'UI.
     */
    private fun fetchAccountInfo(account: String) {
        if (!isValidXRPAddress(account)) {
            updateUIWithError("Adresse XRP invalide.")
            return
        }

        // Afficher la barre de chargement
        showLoading(true)

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val accountInfo = xrpService.getAccountInfo(account).get()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (accountInfo != null) {
                        val balance = accountInfo.accountData().balance().toDouble() / 1_000_000
                        accountBalanceTextView.text = "Solde : %.2f XRP".format(balance)
                    } else {
                        updateUIWithError("Aucune information de compte trouvée.")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors de la récupération des informations : ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    updateUIWithError("Erreur : ${e.message ?: "Inconnue"}")
                }
            }
        }
    }

    /**
     * Met à jour l'interface utilisateur avec un message d'erreur.
     */
    private fun updateUIWithError(message: String) {
        accountBalanceTextView.text = message
    }

    /**
     * Affiche ou masque la barre de chargement.
     */
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Annuler les coroutines lors de la destruction de l'activité
        webSocketClient.disconnect() // Fermer la connexion WebSocket
    }
}
