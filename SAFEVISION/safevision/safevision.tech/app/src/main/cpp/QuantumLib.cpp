#include <jni.h>
#include <string>
#include "oqs/oqs.h"  // Inclure liboqs
#include <android/log.h>  // Pour les logs Android

// Définitions pour les logs Android
#define LOG_TAG "QuantumLib"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_safevision_tech_QuantumLib_generateKey(JNIEnv *env, jobject /* this */) {
    // Initialisation de l'algorithme post-quantique (Kyber-512)
    OQS_KEM *kem = OQS_KEM_new(OQS_KEM_alg_kyber_512);
    if (kem == nullptr) {
        LOGE("OQS_KEM_new failed: Unable to initialize Kyber-512");
        return nullptr;
    }
    LOGI("Kyber-512 initialized successfully");

    // Allocation de mémoire pour les clés publique et privée
    uint8_t *public_key = new (std::nothrow) uint8_t[kem->length_public_key];
    uint8_t *secret_key = new (std::nothrow) uint8_t[kem->length_secret_key];

    if (!public_key || !secret_key) {
        LOGE("Memory allocation failed for public or private keys");
        delete[] public_key;
        delete[] secret_key;
        OQS_KEM_free(kem);
        return nullptr;
    }
    LOGI("Memory allocated for public and private keys");

    // Génération de la paire de clés
    if (OQS_KEM_keypair(kem, public_key, secret_key) != OQS_SUCCESS) {
        LOGE("OQS_KEM_keypair failed: Unable to generate keypair");
        delete[] public_key;
        delete[] secret_key;
        OQS_KEM_free(kem);
        return nullptr;
    }
    LOGI("Keypair generated successfully");

    // Conversion de la clé publique en byte[] Java
    jbyteArray publicKeyArray = env->NewByteArray(static_cast<jsize>(kem->length_public_key));
    if (publicKeyArray == nullptr) {
        LOGE("NewByteArray failed: Unable to allocate memory for public key in Java");
        delete[] public_key;
        delete[] secret_key;
        OQS_KEM_free(kem);
        return nullptr;
    }

    env->SetByteArrayRegion(publicKeyArray, 0, static_cast<jsize>(kem->length_public_key), reinterpret_cast<jbyte *>(public_key));
    LOGI("Public key successfully passed to Java as byte array");

    // Nettoyage de la mémoire allouée
    delete[] public_key;
    delete[] secret_key;
    OQS_KEM_free(kem);

    return publicKeyArray;
}
