package safevision.tech

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import safevision.tech.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisation de la liaison de vues
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigation()
    }

    /**
     * Configure les actions de navigation pour les boutons.
     */
    private fun setupNavigation() {
        binding.buttonNext.setOnClickListener {
            navigateTo(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.buttonNavigateToWallet.setOnClickListener {
            navigateTo(R.id.action_FirstFragment_to_WalletFragment)
        }
    }

    /**
     * Effectue la navigation vers une action spécifique.
     * @param actionId L'identifiant de l'action de navigation.
     */
    private fun navigateTo(actionId: Int) {
        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            // Gestion des erreurs de navigation
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Erreur de navigation : ${e.message ?: "Action inconnue"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Nettoyage de la liaison de vues pour éviter les fuites de mémoire
        _binding = null
    }
}
