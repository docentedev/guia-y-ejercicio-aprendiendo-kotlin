package com.jumbo

import com.jumbo.dto.Profesor
import com.jumbo.dto.Estudiante
import com.jumbo.dto.EstadoCalificacion
import com.jumbo.dto.nota
import com.jumbo.services.AulaMaterializada
import kotlinx.coroutines.*

/*
 * ─── CHEAT SHEET: Funciones de ámbito (scope functions) de Kotlin ────────────────────────
 * Si vienes de Java, esto no existe como concepto propio: son funciones de la librería
 * estándar de Kotlin que reciben una lambda y te evitan escribir variables temporales,
 * builders o métodos utilitarios. Las 5 son:
 *
 *   objeto.apply { ... }   -> dentro del bloque usas "this", y retorna el MISMO objeto.
 *                             (≈ patrón Builder de Java, pero sin escribir "return this;")
 *   valor.let   { ... }    -> dentro del bloque usas "it", y retorna el RESULTADO del bloque.
 *                             (≈ "if (x != null) { ... }" o Optional.ifPresent/.map en Java)
 *   objeto.also  { ... }   -> usas "it", retorna el MISMO objeto. (≈ un "peek" para logging)
 *   objeto.run   { ... }   -> usas "this", retorna el RESULTADO. (≈ bloque que inicializa y calcula)
 *   with(objeto) { ... }   -> usas "this", retorna el RESULTADO. (≈ igual que run, no es extensión)
 *
 * En este archivo usamos sobre todo 'apply' (para configurar un objeto recién creado, sección 3)
 * y 'let' (para operar de forma segura sobre un valor que podría ser null, sección 6).
 * ───────────────────────────────────────────────────────────────────────────────────────
 */

// Usamos runBlocking como el punto de anclaje inicial obligatorio en consola de JVM
// para que el programa espere la respuesta asíncrona antes de apagarse.
fun main(): Unit = runBlocking {
    println("=== SISTEMA DE GESTIÓN DE AULAS ===")

    // 1. Creamos un profesor
    val profesor = Profesor("profe_claudio", "claudio.profesor@duoc.cl", "claveSegura2026")

    // 2. Creamos un Aula Materializada con capacidad para 4 estudiantes
    val aula = AulaMaterializada(
        profesor = profesor,
        capacidad = 4u,
        codigo = "MAT-PROGRAMACION-101"
    )
    println("Aula creada exitosamente: ${aula.codigo} a cargo del profesor ${aula.profesor.nombre}")

    // 3. Creamos estudiantes y les "cargamos" sus notas usando 'apply'.
    // 'apply' ejecuta la lambda con el objeto recién construido como receptor (this) y
    // devuelve ESE MISMO objeto. Es el equivalente conceptual al patrón Builder de Java
    // (Estudiante.builder()....build()), pero sin necesitar una clase Builder aparte ni
    // escribir "return this;" al final de cada método encadenado: aquí construimos al
    // estudiante y, en el mismo bloque, registramos sus pruebas rendidas.
    val est1 = Estudiante("ana_dev", "ana.dev@duoc.cl", "passwordAna123").apply {
        rendirPrueba(85u)
        rendirPrueba(90u)
    }
    val est2 = Estudiante("pedro_code", "pedro.code@duoc.cl", "passwordPedro123").apply {
        rendirPrueba(50u)
        rendirPrueba(55u)
    }
    val est3 = Estudiante("lucia_qa", "lucia.qa@duoc.cl", "passwordLucia123").apply {
        rendirPrueba(20u)
        rendirPrueba(30u)
    }
    val est4 = Estudiante("pepe_qa", "pepe.qa@duoc.cl", "passwordPepe123").apply {
        rendirPrueba(50u)
        rendirPrueba(45u)
    }

    // 4. Registramos los estudiantes en el aula
    aula.agregarEstudiante(est1)
    aula.agregarEstudiante(est2)
    aula.agregarEstudiante(est3)
    aula.agregarEstudiante(est4)
    println("Estudiantes inscritos en el aula: ${aula.estudiantes.size} / ${aula.capacidad}")

    // 5. Probamos el inicio de sesión de un estudiante (Usando onSuccess / onFailure)
    println("\n--- INTENTO DE INICIO DE SESIÓN ---")
    val resultadoLogin = est1.iniciarSesion("ana_dev", "passwordAna123")

    // Result<T>.onSuccess/.onFailure es el equivalente en Kotlin a un
    // try/catch de Java, pero expresado como cadena de callbacks (parecido a
    // promise.then(...).catch(...) en JS, o a CompletableFuture.thenAccept/.exceptionally en Java).
    resultadoLogin
        .onSuccess { mensaje ->
            println("ÉXITO (.then): $mensaje")
        }
        .onFailure { error ->
            println("ERROR (.catch): ${error.message}")
        }

    // 6. Simulamos la rendición de pruebas (ya cargadas arriba con 'apply' en el paso 3).

    // 7. Mostramos los resultados individuales de cada estudiante
    println("\n--- EVALUACIÓN FINAL DE ESTUDIANTES ---")
    for (estudiante in aula.estudiantes) {
        when (val estado = estudiante.calcularPromedioFinal()) {
            is EstadoCalificacion.Aprobado -> {
                println("Estudiante [${estudiante.nombre}] -> APROBADO | Nota: %.2f | Puntos: %d".format(estado.nota, estado.promedioPuntos.toInt()))
            }
            is EstadoCalificacion.Pendiente -> {
                println("Estudiante [${estudiante.nombre}] -> PENDIENTE | Nota: %.2f | Puntos: %d | Razón: ${estado.mensaje}".format(estado.nota, estado.promedioPuntos.toInt()))
            }
            is EstadoCalificacion.Reprobado -> {
                println("Estudiante [${estudiante.nombre}] -> REPROBADO | Nota: %.2f | Puntos: %d".format(estado.nota, estado.promedioPuntos.toInt()))
            }
        }
    }

    // 8. Operaciones de Kotlin sobre listas/colecciones (equivalentes a los Streams de Java)
    println("\n--- OPERACIONES SOBRE LISTAS ---")

    // 'count' con predicado es el equivalente a: collection.stream().filter(pred).count() en Java.
    val totalAprobados = aula.estudiantes.count { estudiante ->
        estudiante.calcularPromedioFinal() is EstadoCalificacion.Aprobado
    }
    println("Estudiantes aprobados: $totalAprobados de ${aula.estudiantes.size}")

    // 'maxByOrNull' es el equivalente a: collection.stream().max(Comparator.comparing(...)).orElse(null).
    // Devuelve un valor NULEABLE (Estudiante?) porque la colección podría estar vacía y no
    // habría "máximo" posible; por eso el resultado se maneja con '?.let' a continuación.
    val mejorEstudiante = aula.estudiantes.maxByOrNull { estudiante -> estudiante.calcularPromedioFinal().nota }

    // 'let' recibe el valor SOLO SI no es null (gracias al "?." previo) y lo expone como "it"
    // (aquí renombrado a "estudiante" para mayor claridad). Es el equivalente en Java a:
    // if (mejorEstudiante != null) { ... } o a Optional.ofNullable(mejorEstudiante).ifPresent(e -> {...}).
    // La ventaja frente al "if" de Java es que 'let' es una EXPRESIÓN: se puede encadenar o
    // devolver un valor, no solo ejecutar un efecto secundario.
    mejorEstudiante?.let { estudiante ->
        println("Mejor estudiante del aula: ${estudiante.nombre} (nota %.2f)".format(estudiante.calcularPromedioFinal().nota))
    }

    // 9. Calculamos el promedio general de la clase (el cálculo interno usa 'reduce', ver
    // AulaMaterializada.calcularPromedioClase). Lo envolvemos en un Result para aplicar el
    // equivalente Kotlin a un try/then/catch.
    println("\n--- RESUMEN DEL AULA ---")

    val resultadoPromedio = try {
        val promedioClase = aula.calcularPromedioClase()
        Result.success(promedioClase)
    } catch (e: Exception) {
        Result.failure(e)
    }

    resultadoPromedio
        .onSuccess { promedio ->
            println("Promedio general de la clase ${aula.codigo}: %.2f".format(promedio))
        }
        .onFailure { error ->
            println("No se pudo calcular el promedio de la clase. Detalle: ${error.message}")
        }

    // 10. Notificación asíncrona con GlobalScope.launch
    println("\n--- NOTIFICACIÓN ASÍNCRONA (GlobalScope.launch) ---")

    // GlobalScope.launch es el equivalente conceptual a: new Thread(() -> { ... }).start();
    // o a executorService.execute(runnable) en Java: lanza una corrutina "suelta" que NO
    // depende del coroutineScope de runBlocking ni de ningún padre estructurado, y no
    // bloquea el hilo principal mientras se ejecuta.
    // (Se usa GlobalScope solo con fines didácticos: en código de producción se prefiere
    //  siempre un scope estructurado, para no "perder" corrutinas huérfanas fuera de control.)
    //
    // Dentro del bloque solo llamamos a una función 'suspend' que internamente usa 'delay'
    // sin abrir su propio coroutineScope ni lanzar más corrutinas (ver
    // AulaMaterializada.generarReporteAsync): ese es el único lugar del proyecto con 'delay'.
    val promedioParaReporte = resultadoPromedio.getOrDefault(0.0)
    val trabajoNotificacion: Job = GlobalScope.launch {
        val reporte = aula.generarReporteAsync(promedioParaReporte)
        println(reporte)
    }

    // Job.join() es el equivalente a Thread.join() en Java: suspende (sin bloquear el hilo)
    // hasta que la corrutina lanzada arriba termine, para no cerrar el programa antes de tiempo.
    trabajoNotificacion.join()
}
