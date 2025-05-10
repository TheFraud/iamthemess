cmake_minimum_required(VERSION 3.10.2)
project("safevision")

# Définir le chemin vers la bibliothèque OQS
set(OQS_LIB_PATH "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/liboqs.so")

# Vérification si liboqs.so existe dans le chemin spécifié
if (NOT EXISTS ${OQS_LIB_PATH})
    message(FATAL_ERROR "La bibliothèque liboqs n'a pas été trouvée. Veuillez vérifier le chemin dans jniLibs.")
endif()

# Définissez le fichier source pour QuantumLib
add_library(
        quantumlib  # Nom de la bibliothèque que nous construisons
        SHARED
        QuantumLib.cpp  # Nom du fichier source que nous avons créé
)

# Inclure les bibliothèques
find_library(log-lib log)

# Lier la bibliothèque native `quantumlib` avec `liboqs.so` et `log`
target_link_libraries(
        quantumlib
        ${OQS_LIB_PATH}  # Lien avec liboqs.so
        ${log-lib}  # Ajoute la bibliothèque log d'Android pour les logs
)

