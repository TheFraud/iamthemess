package safevision.tech

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import safevision.tech.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisation de la liaison de vues
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuration des actions de navigation
        setupNavigation()
    }

    /**
     * Configure le bouton pour naviguer vers le fragment précédent.
     */
    private fun setupNavigation() {
        binding.buttonPrevious.setOnClickListener {
            navigateTo(R.id.action_secondFragment_to_firstFragment)
        }
    }

    /**
     * Navigue vers un autre fragment en utilisant l'identifiant de l'action.
     * @param actionId L'identifiant de l'action de navigation.
     */
    private fun navigateTo(actionId: Int) {
        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Erreur de navigation : ${e.message ?: "Action inconnue"}")
        }
    }

    /**
     * Affiche un message Toast.
     * @param message Le message à afficher.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Nettoyage de la liaison de vues pour éviter les fuites de mémoire
        _binding = null
    }
}
