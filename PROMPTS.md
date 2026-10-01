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
