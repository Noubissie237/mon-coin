#!/bin/bash

# Script d'exécution des tests pour MonCoin
# Usage: ./run_tests.sh [option]
# Options:
#   unit       - Exécuter uniquement les tests unitaires
#   integration - Exécuter uniquement les tests d'intégration
#   all        - Exécuter tous les tests (défaut)
#   coverage   - Générer le rapport de couverture
#   critical   - Exécuter uniquement les tests critiques

set -e  # Arrêter en cas d'erreur

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher un message coloré
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Fonction pour afficher un titre
print_title() {
    echo ""
    print_message "$BLUE" "=========================================="
    print_message "$BLUE" "$1"
    print_message "$BLUE" "=========================================="
    echo ""
}

# Fonction pour exécuter les tests unitaires
run_unit_tests() {
    print_title "🧪 Exécution des Tests Unitaires"
    ./gradlew test
    
    if [ $? -eq 0 ]; then
        print_message "$GREEN" "✅ Tests unitaires réussis !"
        echo "📊 Rapport disponible : app/build/reports/tests/testDebugUnitTest/index.html"
    else
        print_message "$RED" "❌ Tests unitaires échoués !"
        exit 1
    fi
}

# # Fonction pour exécuter les tests d'intégration
# run_integration_tests() {
#     print_title "🔧 Exécution des Tests d'Intégration"
    
#     # Trouver adb dans le SDK Android
#     ADB_PATH=""
#     if [ -n "$ANDROID_HOME" ]; then
#         ADB_PATH="$ANDROID_HOME/platform-tools/adb"
#     elif [ -n "$ANDROID_SDK_ROOT" ]; then
#         ADB_PATH="$ANDROID_SDK_ROOT/platform-tools/adb"
#     elif [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
#         ADB_PATH="$HOME/Android/Sdk/platform-tools/adb"
#     fi
    
#     # Vérifier qu'un appareil est connecté (si adb est disponible)
#     if [ -n "$ADB_PATH" ] && [ -f "$ADB_PATH" ]; then
#         if ! $ADB_PATH devices | grep -q "device$"; then
#             print_message "$YELLOW" "⚠️  Aucun appareil détecté via adb"
#             print_message "$YELLOW" "Les tests vont quand même s'exécuter (Gradle détectera l'émulateur)"
#         else
#             print_message "$GREEN" "📱 Appareil détecté"
#         fi
#     else
#         print_message "$YELLOW" "⚠️  adb non trouvé dans le PATH"
#         print_message "$YELLOW" "Les tests vont quand même s'exécuter (Gradle détectera l'émulateur)"
#     fi
    
#     ./gradlew connectedAndroidTest
    
#     if [ $? -eq 0 ]; then
#         print_message "$GREEN" "✅ Tests d'intégration réussis !"
#         echo "📊 Rapport disponible : app/build/reports/androidTests/connected/index.html"
#     else
#         print_message "$RED" "❌ Tests d'intégration échoués !"
#         exit 1
#     fi
# }

# Fonction pour exécuter les tests critiques
run_critical_tests() {
    print_title "⚡ Exécution des Tests Critiques"
    
    print_message "$YELLOW" "Tests du système d'alarmes..."
    ./gradlew test --tests AlarmSchedulerTest
    ./gradlew test --tests TaskStateCheckerTest
    
    print_message "$YELLOW" "Tests du repository..."
    ./gradlew test --tests TaskRepositoryTest
    
    print_message "$YELLOW" "Tests du ViewModel..."
    ./gradlew test --tests HomeViewModelTest
    
    if [ $? -eq 0 ]; then
        print_message "$GREEN" "✅ Tests critiques réussis !"
    else
        print_message "$RED" "❌ Tests critiques échoués !"
        exit 1
    fi
}

# Fonction pour générer le rapport de couverture
generate_coverage() {
    print_title "📊 Génération du Rapport de Couverture"
    
    ./gradlew test jacocoTestReport
    
    if [ $? -eq 0 ]; then
        print_message "$GREEN" "✅ Rapport de couverture généré !"
        echo "📊 Rapport disponible : app/build/reports/jacoco/jacocoTestReport/html/index.html"
        
        # Ouvrir le rapport dans le navigateur (Linux)
        if command -v xdg-open &> /dev/null; then
            xdg-open app/build/reports/jacoco/jacocoTestReport/html/index.html
        fi
    else
        print_message "$RED" "❌ Génération du rapport échouée !"
        exit 1
    fi
}

# Fonction pour nettoyer avant les tests
clean_build() {
    print_title "🧹 Nettoyage du Projet"
    ./gradlew clean
    print_message "$GREEN" "✅ Nettoyage terminé"
}

# Fonction pour afficher l'aide
show_help() {
    echo "Usage: ./run_tests.sh [option]"
    echo ""
    echo "Options:"
    echo "  unit        - Exécuter uniquement les tests unitaires"
    echo "  integration - Exécuter uniquement les tests d'intégration"
    echo "  all         - Exécuter tous les tests (défaut)"
    echo "  critical    - Exécuter uniquement les tests critiques"
    echo "  coverage    - Générer le rapport de couverture"
    echo "  clean       - Nettoyer le projet avant les tests"
    echo "  help        - Afficher cette aide"
    echo ""
    echo "Exemples:"
    echo "  ./run_tests.sh unit"
    echo "  ./run_tests.sh integration"
    echo "  ./run_tests.sh coverage"
}

# Fonction principale
main() {
    local option=${1:-all}
    
    print_title "🚀 MonCoin - Exécution des Tests"
    print_message "$YELLOW" "Option sélectionnée : $option"
    
    case $option in
        unit)
            run_unit_tests
            ;;
        all)
            run_unit_tests
            ;;
        critical)
            run_critical_tests
            ;;
        coverage)
            generate_coverage
            ;;
        clean)
            clean_build
            ;;
        help)
            show_help
            ;;
        *)
            print_message "$RED" "❌ Option invalide : $option"
            show_help
            exit 1
            ;;
    esac
    
    print_title "✨ Tests Terminés avec Succès !"
}

# Exécuter le script
main "$@"
