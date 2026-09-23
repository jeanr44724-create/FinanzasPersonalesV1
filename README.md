# Finanzas Personales V1

Prototipo Android en Kotlin + Jetpack Compose.

Incluye:
- Inicio
- Ingresos y gastos
- Metas de ahorro
- Estadísticas
- Módulo inicial de deudas

## Compilación
Abrir en Android Studio y ejecutar:
`./gradlew assembleDebug`

El APK se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

Nota: esta V1 mantiene los datos en memoria para facilitar el prototipo. La siguiente versión puede incorporar Room/DataStore para persistencia real, exportación CSV/PDF, presupuestos y un módulo completo de préstamos.

## Calculadora salarial de Panamá
La V1 ahora incluye una pantalla de salario donde se puede introducir el ingreso bruto mensual o quincenal y obtener una estimación de:
- CSS del trabajador: 9.75%.
- Seguro Educativo: configurable, 1.25% por defecto.
- ISR anual y retención mensual estimada.
- Salario neto mensual y quincenal.
- Proyección de renta gravable anual.

La tabla de ISR usada es: hasta B/.11,000 = 0%; de B/.11,000 a B/.50,000 = 15% sobre el excedente; más de B/.50,000 = B/.5,850 + 25% sobre el excedente.

Las tasas y reglas tributarias pueden cambiar; por eso la tasa de Seguro Educativo quedó configurable.
