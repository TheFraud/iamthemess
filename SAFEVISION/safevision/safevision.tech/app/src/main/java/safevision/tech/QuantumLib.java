package safevision.tech;

import android.util.Log;

import java.util.Optional;

public class QuantumLib {
    private static final String TAG = "QuantumLib";

    // Charger la bibliothèque native
    static {
        try {
            System.loadLibrary("native-lib"); // Le nom doit correspondre au fichier .so généré
            logDebug("Bibliothèque native chargée avec succès");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Échec du chargement de la bibliothèque native : " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Erreur inattendue lors du chargement de la bibliothèque native : " + e.getMessage(), e);
        }
    }

    /**
     * Méthode native pour générer une clé.
     * @return un tableau de bytes représentant la clé générée.
     */
    public native byte[] generateKey();

    /**
     * Génère une clé en toute sécurité en utilisant la méthode native.
     * @return Un objet {@link Optional} contenant la clé générée, ou vide en cas d'échec.
     */
    public Optional<byte[]> generateKeySecurely() {
        try {
            byte[] key = generateKey();
            if (isValidKey(key)) {
                logDebug("Clé générée avec succès");
                return Optional.of(key);
            } else {
                Log.e(TAG, "La méthode native a retourné une clé vide ou invalide");
                return Optional.empty();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la génération de la clé : " + e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Valide que la clé générée est non nulle et possède une taille acceptable.
     * @param key Le tableau de bytes représentant la clé.
     * @return true si la clé est valide, false sinon.
     */
    private boolean isValidKey(byte[] key) {
        return key != null && key.length > 16; // Exemple : clé minimale de 128 bits
    }

    /**
     * Journalisation sécurisée pour le mode DEBUG uniquement.
     * @param message Le message à enregistrer.
     */
    private static void logDebug(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }
}
