# Glosario Canónico del Dominio (Ubiquitous Language)

> **Proyecto:** P20 — Recipe and Meal Planning  
> **Asignatura:** Rapid Development of Java Applications Using Programming Frameworks  
> **Institución:** Algebra Bernays University  
> **Estudiante:** Javier Liaño Rioz  

Este documento define el **Lenguaje Ubicuo** (*Ubiquitous Language*) del proyecto. Todos los nombres de clases, atributos, tablas de base de datos, métodos de servicio, endpoints de la API REST y conversaciones durante la defensa oral deben utilizar estos términos con rigor semántico.

---

## 1. Términos Culinarios y Recetario

### `MateriaPrima`
* **Definición:** Alimento o insumo básico e indivisible disponible en el supermercado o mercado de abastos. Representa la hoja terminal del árbol de ingredientes.
* **Analogía en la realidad:** El paquete de sal, la botella de aceite de oliva, los huevos o la harina de trigo.
* **Función en el sistema:** Provee los valores de referencia nutricionales normalizados por cada 100g (energía, proteínas, hidratos y grasas) y la constante física de densidad $(\rho)$. Es el único tipo de elemento que debe aparecer en la lista de la compra final consolidada.

### `Receta`
* **Definición:** Ficha técnica y procedimental completa para elaborar un plato o una preparación culinaria compuesta.
* **Analogía en la realidad:** La página del libro de recetas o ficha técnica de cocina.
* **Función en el sistema:** Unidad fundamental de conocimiento gastronómico. Contiene nombre, tiempos de preparación activa y cocción pasiva, instrucciones paso a paso, porciones de referencia y su lista de `IngredientesReceta`. Puede consumirse como plato final o utilizarse recursivamente como subreceta.

### `Subreceta`
* **Definición:** Elaboración culinaria intermedia que, siendo una receta completa y autónoma en sí misma, es utilizada como ingrediente dentro de otra receta de mayor nivel jerárquico.
* **Analogía en la realidad:** Una salsa de tomate frito casera, un caldo de verduras o una masa brisa utilizada como base para una quiche.
* **Función en el sistema:** Modela el patrón *Composite*. El motor de compras y análisis nutricional desciende recursivamente por las subrecetas hasta descomponerlas en sus materias primas originales.

### `IngredienteReceta`
* **Definición:** Renglón cuantitativo dentro de la lista de suministros requeridos para elaborar una receta. Contiene una magnitud escalar (`cantidad`) y una unidad dimensional (`unidadMedida`).
* **Analogía en la realidad:** La línea del recetario: *"2 cucharadas soperas de aceite de oliva"* o *"300 gramos de salsa bechamel"*.
* **Función en el sistema:** Resuelve la entidad intermedia obligatoria del proyecto P20 (*The hard part*). Asocia una `Receta` con exclusión mutua hacia una `MateriaPrima` o hacia otra `Receta` (subreceta).

### `PorcionesReferencia` ($P_{ref}$)
* **Definición:** Número estándar de raciones para las cuales están calculadas originalmente las cantidades base de una receta en su ficha técnica (ej. *"Receta formulada para 4 raciones"*).
* **Función en el sistema:** Actúa como el denominador constante del factor de escalado. Garantiza que las cantidades de la receta base permanezcan inmutables en el catálogo con independencia de cuántas personas vayan a comer.

---

## 2. Términos de Menú y Planificación Temporal

### `Menu`
* **Definición:** Propuesta gastronómica coordinada que agrupa uno o varios platos pensados para ser servidos juntos en una misma sentada a la mesa.
* **Analogía en la realidad:** Un menú completo: *"Ensalada mixta de primero, salmón al horno de segundo y fruta de postre"*.
* **Función en el sistema:** Entidad autónoma desacoplada del calendario. Puede diseñarse, guardarse en la biblioteca personal del usuario y reutilizarse en múltiples semanas o días sin duplicar registros.

### `PlatoMenu`
* **Definición:** Pase, componente o tiempo individual dentro de un `Menu` (ej. entrante, plato fuerte, guarnición o postre).
* **Analogía en la realidad:** Cada plato individual que se lleva a la mesa en un almuerzo o cena.
* **Función en el sistema:** Vincula el menú con una `Receta` concreta y asigna un número de comensales específico para ese plato (permitiendo que no todos los comensales tomen postre o compartan una ración).

### `PlanSemanal`
* **Definición:** Ciclo temporal de 7 días naturales consecutivos en el que un usuario u hogar organiza su alimentación.
* **Analogía en la realidad:** La cuadrícula semanal de la nevera o agenda alimentaria.
* **Función en el sistema:** Raíz del agregado temporal. Define la fecha de inicio (`fechaInicio`) del ciclo y agrupa todas las entradas agendadas en la semana. Es la base para calcular la lista de la compra consolidada y el balance nutricional semanal.

### `EntradaCalendario`
* **Definición:** Ranura temporal unívoca en la agenda de alimentación, definida por la intersección de un día de la semana (`DiaSemana`) y una franja horaria (`MomentoIngesta`).
* **Analogía en la realidad:** La casilla concreta en el cuadrante de comidas (ej. *"Almuerzo del Miércoles"* o *"Cena del Sábado"*).
* **Función en el sistema:** Conecta la dimensión temporal con la gastronómica. Puede tener asignado un `Menu` o permanecer vacía si el usuario come fuera ese día.

---

## 3. Tipos Enumerados y Magnitudes Físicas

### `Comensales` ($C$)
* **Definición:** Número real de personas que van a consumir un plato específico en una comida determinada.
* **Función en el sistema:** Actúa como el numerador del factor de escalado de raciones:
  $$F = \frac{C}{P_{ref}}$$

### `MomentoIngesta`
* **Definición:** Clasificación enumerada de las diferentes ingestas fisiológicas del día:
  - `DESAYUNO`
  - `ALMUERZO`
  - `CENA`
  - `MERIENDA`

### `DiaSemana`
* **Definición:** Clasificación enumerada de los días naturales del ciclo semanal:
  - `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`, `SABADO`, `DOMINGO`

### `UnidadMedida`
* **Definición:** Magnitudes físicas admitidas para cuantificar un ingrediente en cocina:
  - **Masa:** `GRAMO`, `KILOGRAMO`.
  - **Volumen:** `MILILITRO`, `LITRO`, `CUCHARADA` ($15\text{ ml}$), `CUCHARADITA` ($5\text{ ml}$).
  - **Unidades contables:** `UNIDAD_ENTERA`.

### `Densidad` ($\rho$)
* **Definición:** Constante física intensiva de cada materia prima expresada en $\text{g/ml}$.
* **Función en el sistema:** Permite convertir de forma matemáticamente exacta volúmenes a masas para balance nutricional:
  $$M = V \times \rho$$
