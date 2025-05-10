package safevision.tech

import android.content.Context
import android.util.Log
import org.openquantumsafe.KeyEncapsulation
import org.webrtc.*

class WebRTCManager(
    private val context: Context,
    private val signalingClient: WebSocketClient
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection
    private var dataChannel: DataChannel? = null
    private val iceServers = mutableListOf<PeerConnection.IceServer>()

    // Ajout de liboqs pour gérer le chiffrement
    private val keyEncapsulation = KeyEncapsulation("Kyber512")
    private lateinit var sharedSecret: ByteArray

    // Callbacks configurables
    var onConnectionEstablished: (() -> Unit)? = null
    var onMessageReceived: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    init {
        initializePeerConnectionFactory()
        setupIceServers()
        setupKeyExchange()
    }

    /**
     * Initialiser PeerConnectionFactory.
     */
    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(EglBase.create().eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(EglBase.create().eglBaseContext, true, true))
            .createPeerConnectionFactory()
    }

    /**
     * Configurer les serveurs STUN/TURN.
     */
    private fun setupIceServers() {
        iceServers.add(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        // Ajoutez ici un serveur TURN si nécessaire
    }

    /**
     * Configuration de l'échange de clés.
     */
    private fun setupKeyExchange() {
        val publicKey = keyEncapsulation.generate_keypair()
        signalingClient.sendMessage(
            mapOf(
                "type" to "public_key",
                "key" to publicKey.encodeToString()
            )
        )
        Log.d("WebRTCManager", "Clé publique envoyée.")
    }

    fun handleRemotePublicKey(encodedKey: String) {
        val remotePublicKey = encodedKey.decodeFromString()
        sharedSecret = keyEncapsulation.encapsulate(remotePublicKey)
        Log.d("WebRTCManager", "Secret partagé généré avec succès.")
    }

    private fun String.decodeFromString(): ByteArray =
        android.util.Base64.decode(this, android.util.Base64.DEFAULT)

    private fun ByteArray.encodeToString(): String =
        android.util.Base64.encodeToString(this, android.util.Base64.DEFAULT)

    /**
     * Chiffrer un message.
     */
    private fun encryptMessage(message: String): ByteArray {
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = javax.crypto.spec.SecretKeySpec(sharedSecret, 0, 16, "AES")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedMessage = cipher.doFinal(message.toByteArray())
        return iv + encryptedMessage
    }

    /**
     * Déchiffrer un message.
     */
    private fun decryptMessage(encryptedData: ByteArray): String {
        val iv = encryptedData.sliceArray(0 until 12)
        val encryptedMessage = encryptedData.sliceArray(12 until encryptedData.size)
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = javax.crypto.spec.SecretKeySpec(sharedSecret, 0, 16, "AES")
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, javax.crypto.spec.GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encryptedMessage))
    }

    /**
     * Créer une connexion pair-à-pair.
     */
    fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                Log.d("WebRTCManager", "Candidat ICE généré : ${candidate.sdp}")
                signalingClient.sendMessage(
                    mapOf(
                        "type" to "candidate",
                        "candidate" to candidate.sdp,
                        "sdpMid" to candidate.sdpMid,
                        "sdpMLineIndex" to candidate.sdpMLineIndex
                    )
                )
            }

            override fun onDataChannel(channel: DataChannel) {
                Log.d("WebRTCManager", "Canal de données ouvert.")
                dataChannel = channel
                setupDataChannelCallbacks(channel)
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                Log.d("WebRTCManager", "État de la connexion ICE : $newState")
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    onConnectionEstablished?.invoke()
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    onError?.invoke("Connexion déconnectée.")
                }
            }

            override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onTrack(transceiver: RtpTransceiver) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onRenegotiationNeeded() {}
        })!!

        val dataChannelInit = DataChannel.Init()
        dataChannel = peerConnection.createDataChannel("SafeVisionData", dataChannelInit)
        setupDataChannelCallbacks(dataChannel!!)
    }

    /**
     * Configurer les callbacks du canal de données.
     */
    private fun setupDataChannelCallbacks(channel: DataChannel) {
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {}
            override fun onStateChange() {
                Log.d("WebRTCManager", "État du canal de données : ${channel.state()}")
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                val bytes = ByteArray(buffer.data.remaining())
                buffer.data.get(bytes)
                val decryptedMessage = decryptMessage(bytes)
                Log.d("WebRTCManager", "Message reçu : $decryptedMessage")
                onMessageReceived?.invoke(decryptedMessage)
            }
        })
    }

    /**
     * Envoyer un message via le canal de données.
     */
    fun sendMessageOverDataChannel(message: String) {
        if (dataChannel?.state() == DataChannel.State.OPEN) {
            val encryptedMessage = encryptMessage(message)
            val buffer = DataChannel.Buffer(
                java.nio.ByteBuffer.wrap(encryptedMessage),
                false
            )
            dataChannel?.send(buffer)
            Log.d("WebRTCManager", "Message chiffré envoyé : $message")
        } else {
            onError?.invoke("Canal de données non ouvert.")
        }
    }

    /**
     * Créer une offre SDP.
     */
    fun createOffer() {
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                peerConnection.setLocalDescription(this, description)
                signalingClient.sendMessage(
                    mapOf("type" to "offer", "sdp" to description.description)
                )
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                onError?.invoke("Erreur création offre : $error")
            }

            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    /**
     * Gérer une offre SDP reçue.
     */
    fun handleOffer(sdp: String) {
        val sessionDescription = SessionDescription(SessionDescription.Type.OFFER, sdp)
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                createAnswer()
            }

            override fun onSetFailure(error: String?) {
                onError?.invoke("Erreur réception offre : $error")
            }
        }, sessionDescription)
    }

    /**
     * Créer une réponse SDP.
     */
    private fun createAnswer() {
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) {
                peerConnection.setLocalDescription(this, description)
                signalingClient.sendMessage(
                    mapOf("type" to "answer", "sdp" to description.description)
                )
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                onError?.invoke("Erreur création réponse : $error")
            }

            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    /**
     * Gérer un candidat ICE reçu.
     */
    fun handleIceCandidate(candidateData: Map<String, Any>) {
        val candidate = IceCandidate(
            candidateData["sdpMid"] as String,
            (candidateData["sdpMLineIndex"] as Double).toInt(),
            candidateData["candidate"] as String
        )
        peerConnection.addIceCandidate(candidate)
        Log.d("WebRTCManager", "Candidat ICE ajouté : ${candidate.sdp}")
    }
}
