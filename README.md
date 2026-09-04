# AulaDuoc — Ejercicio de Kotlin (POO + coroutines + scope functions)

Este proyecto modela un sistema de gestión de aulas (profesor, estudiantes, evaluaciones y
promedios) pensado como material de clase para estudiantes que ya conocen **Java** y están
aprendiendo **Kotlin**. El código está fuertemente comentado señalando, en cada concepto nuevo
de Kotlin, su equivalente (o la ausencia de equivalente directo) en Java.

## Conceptos que cubre el código de [Main.kt](src/main/kotlin/Main.kt)

| Concepto Kotlin | Dónde se usa | Equivalente/analogía en Java |
|---|---|---|
| `sealed class` | [EstadoCalificacion.kt](src/main/kotlin/dto/EstadoCalificacion.kt) | Enum con datos por variante / patrón Visitor / `sealed interface` (Java 17+) |
| `data class` | `Aprobado`, `Reprobado`, `Pendiente`, etc. | `record` (Java 16+) o clase con `equals/hashCode/toString` generados (Lombok `@Data`) |
| `Result<T>` + `.onSuccess`/`.onFailure` | login de un estudiante | `try/catch`, o `CompletableFuture.thenAccept/.exceptionally` |
| Función de extensión (`val EstadoCalificacion.nota`) | `EstadoCalificacion.kt` | Método estático utilitario (`EstadoUtils.getNota(estado)`) |
| **`apply`** | creación + carga de notas de cada `Estudiante` | Patrón Builder, sin escribir `return this;` |
| **`let`** | manejo seguro de `maxByOrNull` (nulable) | `if (x != null) { ... }` / `Optional.ofNullable(x).ifPresent(...)` |
| `count`, `map`, `maxByOrNull`, `reduce`, `fold` | sección "Operaciones sobre listas" y cálculo de promedios | `Stream.filter().count()`, `Stream.map()`, `Stream.max(Comparator)`, `Stream.reduce()` |
| `suspend fun` | `calcularPromedioClase`, `generarReporteAsync` | Método "pausable"; sin equivalente 1:1 en Java clásico (se acerca a un método `async` basado en `CompletableFuture`) |
| `delay(...)` | `AulaMaterializada.generarReporteAsync` | `Thread.sleep(...)`, pero SIN bloquear el hilo |
| `GlobalScope.launch { ... }` + `Job.join()` | notificación asíncrona en `Main.kt` | `new Thread(() -> {...}).start()` + `thread.join()`, o `executorService.execute(...)` + esperar el resultado |

> ⚠️ `GlobalScope.launch` se usa aquí **solo con fines didácticos**, para comparar 1:1 con
> `Thread`/`ExecutorService` de Java. En código de producción se prefiere siempre un
> `CoroutineScope` estructurado (por ejemplo, el que entrega `viewModelScope` en Android),
> para no "perder" corrutinas huérfanas fuera de control.

## Cómo ejecutar

```bash
./gradlew run
```

(o bien `./gradlew compileKotlin` para solo verificar que compila).

---

## Ejercicio propuesto (evaluación individual)

### Caso de estudio: **"RapidoYA Delivery"**

Una compañía de delivery llamada **RapidoYA** quiere modernizar el sistema con el que asigna
pedidos a sus repartidores. Actualmente todo se controla en planillas Excel y quieren un
prototipo en Kotlin que aplique los mismos conceptos vistos en clase con el sistema de aulas.

Te contratan para construir ese prototipo de consola. El sistema debe:

1. Registrar **usuarios** (`Repartidor` y `Cliente`), ambos con nombre, correo y contraseña,
   heredando de una clase abstracta `Usuario` (igual que `Estudiante`/`Profesor` heredan de
   `Usuario` en este proyecto) con `iniciarSesion(...)` que devuelva `Result<String>`.
2. Modelar el **estado de un pedido** con una `sealed class EstadoPedido` con (al menos) tres
   variantes: `EnCamino`, `Entregado` y `Cancelado`, cada una con los datos que le hagan
   sentido (por ejemplo, `Entregado` con `tiempoMinutos: Int`, `Cancelado` con un `motivo: String`).
3. Modelar un **centro de distribución** (`CentroDistribucion`), análogo al `Aula`: tiene una
   `capacidad` máxima de repartidores, un `codigo`, y un `MutableSet<Repartidor>`.
4. Cada `Repartidor` puede **registrar entregas** (`registrarEntrega(tiempoMinutos: UInt)`),
   análogo a `rendirPrueba(...)`, y calcular su propio rendimiento
   (`calcularRendimiento(): EstadoPedido` o similar) según reglas de negocio que tú definas
   (por ejemplo: promedio de tiempo de entrega bajo 30 min = buen rendimiento).

### Requisitos técnicos obligatorios (deben quedar explícitos y comentados en el código)

- **Validaciones** en los `init` de tus clases usando `require(...)`, análogas a las de
  [Usuario.kt](src/main/kotlin/dto/Usuario.kt):
  - El nombre de usuario debe tener más de 3 caracteres.
  - El correo debe cumplir un formato válido (puedes reusar el `Regex` de `Usuario`).
  - La contraseña debe tener al menos 8 caracteres.
  - El tiempo de entrega registrado no puede ser negativo, y no puede superar un máximo
    razonable que tú definas (ej. 180 minutos) — lanza `IllegalArgumentException` con
    `require(...)` si se viola.
  - La capacidad del `CentroDistribucion` no puede ser 0 (nadie abre un centro sin cupo).
- Al menos **un uso de `apply`** para construir y configurar un objeto en un solo bloque
  (por ejemplo, crear un `Repartidor` y cargarle sus entregas de ejemplo).
- Al menos **un uso de `let`** sobre un valor nulable devuelto por una operación de lista
  (por ejemplo, encontrar al repartidor más rápido con `minByOrNull` y usar `?.let { ... }`
  para notificarlo solo si existe).
- Al menos **dos operaciones distintas sobre listas** de la librería estándar (`map`, `filter`,
  `count`, `reduce`, `fold`, `groupBy`, `minByOrNull`/`maxByOrNull`, `sumOf`, etc.), cada una
  comentada indicando su equivalente en `Stream` de Java.
- Una función `calcularPromedioEntregaCentro(): Double` marcada `suspend`, **sin**
  `coroutineScope`/`async` y **sin** ningún `return@coroutineScope` (debe resolverse con
  operaciones de lista, como se hizo en `AulaMaterializada.calcularPromedioClase`).
- Una función `suspend fun notificarClienteAsync(pedido: ...): String` que use `delay(...)`
  para simular la latencia de enviar una notificación push/SMS, y que **no** abra su propio
  `coroutineScope` ni lance corrutinas por sí misma (solo `suspend` + `delay`).
- En `main()`, lanzar esa notificación con `GlobalScope.launch { ... }` y esperar su
  finalización con `Job.join()`, comentando la analogía con `Thread`/`ExecutorService` de Java.
- Evitar `return` tempranos dentro de builders de corrutinas (`return@coroutineScope`,
  `return@launch`, etc.); si necesitas un caso base (lista vacía, etc.), resuélvelo con una
  expresión `if/else` o eligiendo la función de lista adecuada (`fold` con semilla en vez de
  `reduce`, como se explica en [Aula.kt](src/main/kotlin/services/Aula.kt)).

### Entregable

Un archivo `Main.kt` ejecutable con `runBlocking`, que:
1. Cree un `CentroDistribucion` y al menos 4 `Repartidor`.
2. Registre entregas para cada uno (con `apply`).
3. Muestre el estado de cada repartidor con un `when` exhaustivo sobre `EstadoPedido`.
4. Muestre al menos dos estadísticas calculadas con operaciones de lista (por ejemplo,
   cantidad de entregas exitosas y el repartidor más rápido, este último con `let`).
5. Calcule y muestre el promedio de tiempos de entrega del centro (usando `reduce` o `fold`).
6. Lance una notificación asíncrona con `GlobalScope.launch` + `Job.join()`.

### Criterios de evaluación (sugeridos)

- Corrección de las validaciones (`require`) y manejo de errores con `Result`.
- Uso correcto y justificado (con comentario) de `apply` y `let`.
- Uso correcto de al menos dos operaciones de listas distintas.
- Coroutines: `suspend` bien aplicado, `delay` solo en una función "pura", `GlobalScope.launch`
  + `join()` en `main()`, sin `return@coroutineScope`.
- Comentarios que expliquen la analogía con Java, tal como en este proyecto.
