package safevision.tech

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import safevision.tech.databinding.FragmentMessagingBinding

class MessagingFragment : Fragment() {

    private var _binding: FragmentMessagingBinding? = null
    private val binding get() = _binding!!

    private lateinit var webRTCManager: WebRTCManager
    private lateinit var webSocketClient: WebSocketClient
    private val userId = "user_${System.currentTimeMillis()}"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebSocketAndWebRTC()

        // Initialiser la connexion avec un utilisateur
        binding.connectButton.setOnClickListener {
            val targetId = binding.targetUserId.text.toString()
            if (targetId.isNotEmpty()) {
                webSocketClient.sendMessage(mapOf("type" to "connect_request", "targetId" to targetId))
                Toast.makeText(requireContext(), "Invitation envoyée à $targetId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Entrez un ID utilisateur valide", Toast.LENGTH_SHORT).show()
            }
        }

        // Envoyer un message
        binding.sendMessageButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotEmpty()) {
                webRTCManager.sendMessageOverDataChannel(message)
                binding.messageHistory.append("Vous : $message\n")
                binding.messageInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "Le message est vide", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupWebSocketAndWebRTC() {
        val serverUrl = "ws://10.0.2.2:8080" // Remplacez si nécessaire
        webSocketClient = WebSocketClient(serverUrl)

        webSocketClient.connect(userId)
        webRTCManager = WebRTCManager(requireContext(), webSocketClient)

        webRTCManager.onConnectionEstablished = {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Connexion établie !", Toast.LENGTH_SHORT).show()
            }
        }

        webRTCManager.onMessageReceived = { message ->
            requireActivity().runOnUiThread {
                binding.messageHistory.append("Reçu : $message\n")
            }
        }

        webRTCManager.onError = { error ->
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Erreur : $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
