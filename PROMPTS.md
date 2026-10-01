# Bitácora de Prompts · Botiquín al Día

Este archivo registra la evolución de las instrucciones y requerimientos del proyecto a lo largo de la escalera de desarrollo (P0 a M5).

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

## Prompt 2 (M1): Perfeccionamiento de Registro y Estados Visuales
**Objetivo:** Ajustar el selector de fechas (`DatePicker` de Material 3), selector de unidades (comprimidos, unidades, ml, sobres) y validaciones en tiempo real para evitar ingresos accidentales con fechas vacías o cantidades nulas.

---

## Prompt 3 (M2): Persistencia Local Robusta con SQLite / Room
**Objetivo:** Implementar la base de datos local con Room Database (`ProductEntity`, `ProductDao`, `ProductRepository`), garantizando que al cerrar la aplicación o reiniciar el dispositivo los datos no se pierdan.

---

## Prompt 4 (M3): Experiencia Móvil Pulida y Pantalla Vacía
**Objetivo:** Optimizar diseño táctil, padding ergonómico de 48dp en botones de acción rápida (+ / - stock), empty states amigables para cuando no hay alertas ni productos agotados, y soporte para modo oscuro nativo.

---

## Prompt 5 (M4): Validaciones y Prevención de Errores Críticos
**Objetivo:** Agregar diálogos de confirmación antes de eliminar productos, avisos específicos cuando un producto ya está vencido hoy (estado rojo crítico) frente a los que vencen en 30 días (amarillo preventivo), y badges dinámicos en la barra de navegación.

---

## Prompt 6 (M5): Inteligencia Local y Asistente de Compatibilidad
**Objetivo:** Incorporar recomendaciones inteligentes de almacenamiento del botiquín (separación de medicamentos fotosensibles, antisépticos como agua oxigenada/alcohol alejados de gasas estériles sin sellar) y ordenamiento por urgencia médica en la lista de reposición.
