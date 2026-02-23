@echo off
REM Lanceur Gestion de Parking
cd /d "%~dp0.."
java -jar GestionParking.jar
if %ERRORLEVEL% neq 0 (
    echo Erreur au lancement. Verifiez que Java 11+ est installe.
    pause
)
