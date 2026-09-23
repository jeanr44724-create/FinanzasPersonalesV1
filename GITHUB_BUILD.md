# Finanzas Personales V1 — compilación desde GitHub

Este proyecto está preparado para compilarse en GitHub Actions sin Android Studio ni PC.

## Desde el teléfono

1. Crea un repositorio nuevo en GitHub llamado `FinanzasPersonalesV1`.
2. Sube el contenido del proyecto a la raíz del repositorio.
3. Abre **Actions**.
4. Selecciona **Build Android APK**.
5. Pulsa **Run workflow**.
6. Espera a que aparezca el check verde.
7. Entra en la ejecución y baja hasta **Artifacts**.
8. Descarga `FinanzasPersonales-debug-apk`.
9. Abre el ZIP descargado y extrae el APK para instalarlo.

El workflow instala Gradle en el runner y ejecuta `assembleDebug`, por lo que no necesita que el proyecto incluya el Gradle Wrapper.
