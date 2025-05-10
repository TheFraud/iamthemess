#include <stdio.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <android/log.h>

// Définitions pour Android log
#define LOG_TAG "TestOpenSSL"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Fonction de rappel pour afficher les erreurs
int log_errors(const char *str, size_t len, void *u) {
    LOGE("%.*s", (int)len, str);
    return 1;
}

int main() {
    // Initialiser la bibliothèque SSL
    SSL_library_init();
    LOGI("OpenSSL library initialized successfully.");

    // Créer un contexte client TLS
    const SSL_METHOD *method = TLS_client_method();
    if (!method) {
        // Utiliser le rappel pour afficher les erreurs
        ERR_print_errors_cb(log_errors, NULL);
        return 1;
    }

    LOGI("TLS client method created successfully.");
    return 0;
}

