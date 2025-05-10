package safevision.tech

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import safevision.tech.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisation de la liaison de vues
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuration des actions de navigation
        setupNavigation()
    }

    /**
     * Configure le bouton de navigation pour passer à l'écran principal.
     */
    private fun setupNavigation() {
        binding.buttonNext.setOnClickListener {
            if (isUserReady()) {
                navigateToMainActivity()
            } else {
                showToast("Veuillez compléter les étapes nécessaires.")
            }
        }
    }

    /**
     * Vérifie si l'utilisateur est prêt à passer à l'écran suivant.
     * @return true si l'utilisateur est prêt, false sinon.
     */
    private fun isUserReady(): Boolean {
        // Logique pour vérifier si l'utilisateur est prêt
        return true // Modifier selon vos besoins
    }

    /**
     * Effectue la navigation vers l'activité principale.
     */
    private fun navigateToMainActivity() {
        try {
            findNavController().navigate(R.id.action_onboardingFragment_to_mainActivity)
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
