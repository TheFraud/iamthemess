package safevision.tech;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendButton;

    private QuantumLib quantumLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialiser QuantumLib
        quantumLib = new QuantumLib();

        // Générer une clé au démarrage et afficher dans le TextView
        quantumLib.generateKeySecurely().ifPresentOrElse(
                publicKey -> {
                    StringBuilder keyString = new StringBuilder();
                    for (byte b : publicKey) {
                        keyString.append(String.format("%02X ", b));
                    }
                    chatTextView.setText("Clé publique générée : \n" + keyString.toString());
                    Log.i(TAG, "Clé publique générée : " + keyString);
                },
                () -> Log.e(TAG, "Erreur lors de la génération de la clé publique")
        );

        // Configurer l'envoi de messages
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                chatTextView.append("\nMoi : " + message);
                messageEditText.setText("");
                // Placeholder : Vous pouvez ajouter ici une logique pour envoyer le message
            }
        });
    }
}
