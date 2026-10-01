# Bitácora de Prompts · Botiquín al Día

Este archivo registra la evolución de las instrucciones, pruebas y requerimientos del proyecto a lo largo de la escalera de desarrollo (P0 a M5).

---

## Prompt 1 (P0 + M1 + M2 Inicial): Configuración y Tres Funciones Esenciales

**Fecha:** 1 de octubre de 2026  
**Objetivo:** Crear la primera versión funcional de "Botiquín al Día" con las tres funciones mínimas sin librerías de pago ni servicios externos complejos.

```markdown
ROL: Sos un desarrollador senior de aplicaciones web (o Android con Kotlin y Jetpack Compose).

CONTEXTO: Estoy construyendo una app llamada BOTIQUIN AL DIA 

TAREA: Generá la primera versión funcional, con estas tres funciones y nada más:
1. Registrar producto con cantidad y fecha de vencimiento.
2. Alerta de lo que vence en 30 días.
3. Lista de reposición.

RESTRICCIONES: en español, sin librerías de pago, sin login, sin base de datos en
servidor todavía. Que se vea bien en un celular. Código comentado en los puntos
donde alguien vaya a equivocarse.

FORMATO DE SALIDA: los archivos completos, cada uno con su nombre, y al final una
lista de lo que NO hiciste y por qué.

CRITERIO DE ACEPTACIÓN: abro la app, Entro a la aplicacion y la pruebo y veo que todo funcione correctamente. sin ningún error en la consola.

BOTIQUÍN AL DÍA
Salud del hogar

Problema:
La mitad del botiquín de la casa está vencido.

Usuario:
Responsable del hogar.

Las tres funciones mínimas (esto es P0 + M1):
- Registrar producto con cantidad y fecha de vencimiento.
- Alerta de lo que vence en 30 días.
- Lista de reposición.

Dato clave que maneja (esto es M2):
- Producto + vencimiento.

Sello de IA (esto es M5):
- La IA ordena la lista de reposición por prioridad y advierte qué productos no conviene guardar juntos.

Dónde guardar:
- Inventario del botiquín.
```

---

## Prompt 2: Estructura de Repositorio y Carpetas de Entrega

**Fecha:** 1 de octubre de 2026  
**Objetivo:** Crear la estructura de carpetas `BOTIQUIN-AL-DIA/` con la documentación técnica (13 partes), bitácora de prompts, variables de entorno de ejemplo y directorio de evidencias gráficas.

```markdown
(podrias hacerme la siguiente estrucura en el repositorio pero solo las carpetas ademas de hacerme los (README) y pega el prompt anterior 
en sus respectiva carpeta)
BOTIQUIN-AL-DIA/
├── README.md ← las 13 partes
├── PROMPTS.md ← la bitácora de los 6 prompts
├── .gitignore ← con .env adentro, siempre
├── .env.ejemplo ← las variables sin sus valores reales
├── evidencias/
│ ├── E0-inicial.png
│ ├── E1-antes.png E1-despues.png
│ ├── E2-antes.png E2-despues.png
│ ├── E3-celular.png E3-vacio.png
│ ├── E4-error.png
│ ├── E5-json.png E5-app.png E5-falla.png
│ └── qr.png
└── (tu código)
```

---

## Prompt 3 (M4 - Corrección de Error Crítico): Desbloqueo del Selector de Fecha

**Fecha:** 1 de octubre de 2026  
**Problema reportado por el usuario:** Imposibilidad de seleccionar o cambiar la fecha de vencimiento en el formulario de carga/edición.

```markdown
Necesito que arregles un error el cual es que no puedo cambiar la fecha de vencimiento estoy vas de intentar y no puedo cambiarla
```

**Diagnóstico Técnico:**  
En Jetpack Compose, el componente `OutlinedTextField` con `readOnly = true` consume los gestos táctiles internamente para el manejo de foco y accesibilidad, impidiendo que el modificador `.clickable { showDatePicker = true }` fuera invocado.

**Solución aplicada:**
1. Implementación de una capa transparente (overlay táctil en `Box`) sobre el campo para capturar cualquier pulsación táctil de forma infalible.
2. Conversión del ícono de calendario en un `IconButton` interactivo.
3. Botón explícito visible: *"Abrir Calendario para Cambiar Fecha"*.
4. Inclusión de atajos rápidos de vencimiento farmacéutico común (+1 mes, +6 meses, +1 año, +2 años, ya vencido).
5. Conversión y normalización de fecha UTC a hora local del dispositivo.

---

## Prompt 4: Consolidación y Registro en Archivos de GitHub y Evidencias

**Fecha:** 1 de octubre de 2026  
**Objetivo:** Guardar todos los prompts, sincronizar la estructura de evidencias, documentar la solución y dejar el repositorio preparado para entrega.

```markdown
ok y me guardas el promp y todo lo necesario en las evidencias y archivos porfa de github
```

**Acciones realizadas:**
- Actualización de `PROMPTS.md` con todos los requerimientos y correcciones.
- Sincronización de `README.md` (13 secciones) detallando la corrección en la escalera de mejoras y tarjeta anti-alucinación.
- Aseguramiento de todos los archivos de evidencias (`E0` a `E5`, `qr.png`).
- Sincronización en la carpeta `BOTIQUIN-AL-DIA/` y en la raíz del proyecto.

---

## Prompt 5: Integración de la API de Gemini (Sello de IA con Salida JSON Estructurada)

**Fecha:** 1 de octubre de 2026  
**Objetivo:** Integrar una llamada real a la API de Gemini (`gemini-3.5-flash`) para el Sello de IA del ejercicio: priorización médica de la lista de reposición y advertencias de almacenamiento e incompatibilidad en el botiquín del hogar.

```markdown
Integrá una llamada a la API de Gemini dentro de la app para esta tarea concreta:
[SELLO DE IA DE MI EJERCICIO].

Requisitos:
1. La respuesta debe venir como JSON con un esquema fijo (responseSchema), no como
   texto libre. Dame el esquema.
2. La app consume ese JSON y lo muestra en pantalla como dato, no como párrafo.
3. La llave de API se lee de una variable de entorno; mostrame cómo configurarla.
4. Manejo de fallo: qué se muestra si la IA no responde, responde lento o devuelve
   algo que no cumple el esquema.
5. Un ejemplo de respuesta de prueba para desarrollar sin gastar llamadas.

y guarda este prompt en "PROMPTS" como el 5° prompt
```

### Respuestas a los 5 requisitos técnicos:

1. **Esquema JSON fijo (`responseSchema`):**
```json
{
  "type": "OBJECT",
  "properties": {
    "prioritizedItems": {
      "type": "ARRAY",
      "items": {
        "type": "OBJECT",
        "properties": {
          "name": { "type": "STRING", "description": "Nombre del producto a reponer" },
          "priority": { "type": "STRING", "description": "Nivel de urgencia médica: URGENTE, ALTA, o MEDIA" },
          "category": { "type": "STRING", "description": "Categoría farmacológica o indicación" },
          "medicalReason": { "type": "STRING", "description": "Motivo conciso de por qué debe reponerse con esa prioridad" }
        },
        "required": ["name", "priority", "category", "medicalReason"]
      }
    },
    "storageWarnings": {
      "type": "ARRAY",
      "items": {
        "type": "OBJECT",
        "properties": {
          "productA": { "type": "STRING", "description": "Primer producto o categoría incompatible" },
          "productB": { "type": "STRING", "description": "Segundo producto o factor de riesgo ambiental" },
          "dangerLevel": { "type": "STRING", "description": "Nivel de riesgo: ALTO, MEDIO, o PRECAUCION" },
          "recommendation": { "type": "STRING", "description": "Indicación precisa de separación física o guardado" }
        },
        "required": ["productA", "productB", "dangerLevel", "recommendation"]
      }
    }
  },
  "required": ["prioritizedItems", "storageWarnings"]
}
```

2. **Consumo y visualización de datos estructurados:**
La pantalla `RestockScreen` no muestra párrafos extensos de texto. Mapea cada elemento del JSON a componentes visuales dedicados:
- Badges de urgencia coloreados (`URGENTE` en rojo, `ALTA` en naranja, `MEDIA` en azul).
- Chips de categoría farmacológica.
- Cajas de motivo sanitario individual.
- Tarjetas de incompatibilidad con visualización `[Producto A] ❌ [Producto B]` y badge de nivel de riesgo (`RIESGO ALTO`, `RIESGO MEDIO`, `PRECAUCIÓN`).

3. **Lectura de la llave de API:**
- Se configuró el plugin Secrets Gradle para leer `GEMINI_API_KEY` desde `.env` o el panel de Secrets de AI Studio.
- En código Kotlin se accede de manera segura mediante `BuildConfig.GEMINI_API_KEY`.
- Se habilitó el permiso `<uses-permission android:name="android.permission.INTERNET" />` en `AndroidManifest.xml`.

4. **Manejo de fallos y resiliencia:**
- Timeout de 60 segundos en conexión, lectura y escritura mediante `OkHttpClient`.
- En caso de error HTTP, falta de clave de API, sin conexión a internet o JSON no conforme al esquema, la app nunca se congela ni crashea:
  - Muestra un banner ámbar explicativo: *"No se pudo conectar con la API de Gemini (Timeout/Sin clave). Se activó automáticamente el respaldo local de seguridad."*
  - Activa de inmediato el motor de reglas sanitarias locales predefinidas en `GeminiRestockService.getLocalFallbackResult()`.
  - Ofrece botón de reintento y botón de datos simulados.

5. **Ejemplo de respuesta de prueba (Mock):**
Disponible en el botón *"Prueba Mock"* de la interfaz y definido en `GeminiRestockService.MOCK_RESPONSE_JSON`.
```json
{
  "prioritizedItems": [
    {
      "name": "Ibuprofeno 400mg",
      "priority": "URGENTE",
      "category": "Analgésico / Antinflamatorio",
      "medicalReason": "Básico para alivio rápido de dolor agudo, fiebre o inflamación en el hogar."
    },
    {
      "name": "Gasas estériles",
      "priority": "ALTA",
      "category": "Curación básica",
      "medicalReason": "Imprescindibles para contener heridas sangrantes y prevenir infecciones inmediatas."
    },
    {
      "name": "Alcohol 70%",
      "priority": "MEDIA",
      "category": "Antiséptico",
      "medicalReason": "Útil para desinfección de material e instrumental; no colocar directo en heridas abiertas."
    }
  ],
  "storageWarnings": [
    {
      "productA": "Alcohol / Antisépticos líquidos",
      "productB": "Gasas estériles sin sellar",
      "dangerLevel": "ALTO",
      "recommendation": "Guardar líquidos inflamables en compartimentos inferiores y apósitos estériles en bolsa hermética para evitar contaminación por vapores o derrames."
    },
    {
      "productA": "Medicamentos fotosensibles (Ibuprofeno)",
      "productB": "Luz solar directa o humedad",
      "dangerLevel": "MEDIO",
      "recommendation": "Mantener siempre dentro de su caja original de cartón y lejos de fuentes de calor o vapor del baño."
    }
  ]
}
```
