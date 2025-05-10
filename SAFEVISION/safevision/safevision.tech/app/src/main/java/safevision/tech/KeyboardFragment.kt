package safevision.tech

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import safevision.tech.databinding.FragmentKeyboardBinding

class KeyboardFragment : Fragment() {

    private lateinit var binding: FragmentKeyboardBinding
    private var passwordInput = StringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentKeyboardBinding.inflate(inflater, container, false)
        setupKeyboard()
        return binding.root
    }

    /**
     * Configure les boutons du clavier et leurs actions.
     */
    private fun setupKeyboard() {
        val buttonIds = listOf(
            binding.button1 to "1",
            binding.button2 to "2",
            binding.button3 to "3",
            binding.button4 to "4",
            binding.button5 to "5",
            binding.button6 to "6",
            binding.button7 to "7",
            binding.button8 to "8",
            binding.button9 to "9",
            binding.button0 to "0"
        )

        // Associer les actions de chaque bouton numérique
        buttonIds.forEach { (button, value) ->
            button.setOnClickListener { appendCharacter(value) }
        }

        // Bouton de suppression
        binding.buttonDelete.setOnClickListener { deleteLastCharacter() }

        // Bouton de validation
        binding.buttonValidate.setOnClickListener { validatePassword() }
    }

    /**
     * Ajoute un caractère au mot de passe en cours.
     */
    private fun appendCharacter(character: String) {
        passwordInput.append(character)
        updatePasswordDisplay()
    }

    /**
     * Supprime le dernier caractère du mot de passe.
     */
    private fun deleteLastCharacter() {
        if (passwordInput.isNotEmpty()) {
            passwordInput.deleteCharAt(passwordInput.length - 1)
            updatePasswordDisplay()
        }
    }

    /**
     * Met à jour l'affichage du mot de passe avec un masquage partiel.
     */
    private fun updatePasswordDisplay() {
        val maskedPassword = "*".repeat(passwordInput.length - 1) +
                if (passwordInput.isNotEmpty()) passwordInput.last() else ""
        binding.passwordDisplay.text = maskedPassword

        // Masquer complètement après un délai
        binding.passwordDisplay.postDelayed({
            binding.passwordDisplay.text = "*".repeat(passwordInput.length)
        }, 500)
    }

    /**
     * Valide le mot de passe selon les règles définies.
     */
    private fun validatePassword() {
        val password = getPassword()
        if (isPasswordValid(password)) {
            Toast.makeText(requireContext(), "Mot de passe valide", Toast.LENGTH_SHORT).show()
            clearPassword()
        } else {
            Toast.makeText(
                requireContext(),
                "Mot de passe invalide : minimum 8 caractères avec des lettres et des chiffres.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Retourne le mot de passe actuel.
     */
    fun getPassword(): String {
        return passwordInput.toString()
    }

    /**
     * Vérifie si le mot de passe respecte les critères de sécurité.
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    /**
     * Efface le mot de passe en cours.
     */
    private fun clearPassword() {
        passwordInput.clear()
        updatePasswordDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Efface les données sensibles à la destruction de la vue
        clearPassword()
    }
}
