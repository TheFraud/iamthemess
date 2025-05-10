package safevision.tech

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import safevision.tech.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du binding
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Afficher le logo et passer à l'activité principale après un délai
        lifecycleScope.launch {
            delaySplashScreen()
        }
    }

    /**
     * Gère le délai d'affichage du SplashScreen avant de naviguer.
     */
    private suspend fun delaySplashScreen() {
        delay(2000) // Temporisation de 2 secondes
        navigateToMainActivity()
    }

    /**
     * Navigue vers l'activité principale avec une animation de transition.
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish() // Terminer l'activité SplashScreen
    }
}
