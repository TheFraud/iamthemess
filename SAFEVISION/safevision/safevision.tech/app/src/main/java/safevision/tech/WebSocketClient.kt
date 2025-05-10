package safevision.tech

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketClient(private val serverUrl: String) {

    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var listener: WebSocketListener? = null

    /**
     * Connecte le client WebSocket au serveur.
     * @param userId Identifiant unique de l'utilisateur (peut être généré dynamiquement).
     */
    fun connect(userId: String) {
        val request = Request.Builder().url("$serverUrl?userId=$userId").build()
        listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketClient", "Connecté au serveur WebSocket.")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketClient", "Message reçu : $text")
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketClient", "Erreur WebSocket : ${t.message}", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketClient", "Connexion WebSocket fermée : $reason")
            }
        }
        webSocket = client.newWebSocket(request, listener!!)
    }

    /**
     * Envoie un message au serveur WebSocket.
     * @param message Contenu du message au format Map<String, Any>.
     */
    fun sendMessage(message: Map<String, Any>) {
        try {
            val jsonMessage = JSONObject(message).toString()
            webSocket?.send(jsonMessage)
            Log.d("WebSocketClient", "Message envoyé : $jsonMessage")
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Erreur lors de l'envoi du message : ${e.message}", e)
        }
    }

    /**
     * Traite les messages reçus du serveur WebSocket.
     * @param message Le message reçu sous forme de chaîne JSON.
     */
    private fun handleMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.getString("type")
            when (type) {
                "offer" -> {
                    val sdp = json.getString("sdp")
                    WebRTCManagerSingleton.instance.handleOffer(sdp)
                }
                "answer" -> {
                    val sdp = json.getString("sdp")
                    WebRTCManagerSingleton.instance.handleAnswer(sdp)
                }
                "candidate" -> {
                    val candidate = mapOf(
                        "candidate" to json.getString("candidate"),
                        "sdpMid" to json.getString("sdpMid"),
                        "sdpMLineIndex" to json.getInt("sdpMLineIndex")
                    )
                    WebRTCManagerSingleton.instance.handleIceCandidate(candidate)
                }
                else -> {
                    Log.w("WebSocketClient", "Type de message inconnu : $type")
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Erreur lors du traitement du message : ${e.message}", e)
        }
    }

    /**
     * Ferme la connexion WebSocket.
     */
    fun disconnect() {
        webSocket?.close(1000, "Fermeture de la connexion.")
        webSocket = null
    }
}
