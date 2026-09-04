package com.rapidoya

import com.rapidoya.dto.Cliente
import com.rapidoya.dto.EstadoPedido
import com.rapidoya.dto.Repartidor
import com.rapidoya.services.CentroDistribucion
import kotlinx.coroutines.*

// ── SOLUCIÓN DE REFERENCIA: ejercicio "RapidoYA Delivery" propuesto en README.md ──
// Aplica los mismos conceptos que src/main/kotlin/Main.kt (ejercicio del aula), pero sobre
// un dominio distinto: repartidores, clientes y un centro de distribución. Revisa el cheat
// sheet de scope functions al inicio de src/main/kotlin/Main.kt si necesitas repasar
// apply/let antes de leer este archivo.

fun main(): Unit = runBlocking {
    println("=== SISTEMA DE GESTIÓN DE ENTREGAS: RAPIDOYA ===")

    // 1. Creamos el centro de distribución (capacidad debe ser > 0, validado en su 'init').
    val centro = CentroDistribucion(capacidad = 4u, codigo = "CD-SANTIAGO-CENTRO")
    println("Centro creado exitosamente: ${centro.codigo}")

    // 2. Creamos repartidores y les cargamos sus entregas usando 'apply'.
    // 'apply' ejecuta la lambda con el objeto recién construido como receptor (this) y
    // devuelve ESE MISMO objeto: construimos al repartidor y, en el mismo bloque,
    // registramos sus entregas ya realizadas (equivalente al patrón Builder de Java, sin
    // necesitar una clase Builder aparte ni escribir "return this;").
    val rep1 = Repartidor("juan_moto", "juan.moto@rapidoya.cl", "claveJuan123").apply {
        registrarEntrega(15u)
        registrarEntrega(18u)
    }
    val rep2 = Repartidor("maria_bici", "maria.bici@rapidoya.cl", "claveMaria123").apply {
        registrarEntrega(25u)
        registrarEntrega(28u)
    }
    val rep3 = Repartidor("carlos_auto", "carlos.auto@rapidoya.cl", "claveCarlos123").apply {
        registrarEntrega(40u)
        registrarEntrega(50u)
    }
    val rep4 = Repartidor("sofia_moto", "sofia.moto@rapidoya.cl", "claveSofia123").apply {
        registrarEntrega(10u)
        registrarEntrega(12u)
    }

    // 3. Registramos los repartidores en el centro.
    centro.registrarRepartidor(rep1)
    centro.registrarRepartidor(rep2)
    centro.registrarRepartidor(rep3)
    centro.registrarRepartidor(rep4)
    println("Repartidores registrados en el centro: ${centro.repartidores.size} / ${centro.capacidad}")

    // 4. Probamos el inicio de sesión de un repartidor y de un cliente (Result.onSuccess/onFailure,
    // equivalente en Kotlin a un try/catch de Java expresado como cadena de callbacks).
    println("\n--- INTENTOS DE INICIO DE SESIÓN ---")
    rep1.iniciarSesion("juan_moto", "claveJuan123")
        .onSuccess { mensaje -> println("ÉXITO (.then): $mensaje") }
        .onFailure { error -> println("ERROR (.catch): ${error.message}") }

    val cliente1 = Cliente("pablo_cliente", "pablo.cliente@rapidoya.cl", "clavePablo123")
    cliente1.iniciarSesion("pablo_cliente", "claveIncorrecta")
        .onSuccess { mensaje -> println("ÉXITO (.then): $mensaje") }
        .onFailure { error -> println("ERROR (.catch): ${error.message}") }

    // 5. Mostramos el rendimiento de cada repartidor con un 'when' EXHAUSTIVO sobre
    // EstadoPedido (sealed class): no hace falta rama 'else' porque el compilador conoce
    // las 3 subclases posibles. Nota que 'EnCamino' es un 'object' (sin datos), por eso se
    // compara sin 'is'.
    println("\n--- RENDIMIENTO DE REPARTIDORES ---")
    for (repartidor in centro.repartidores) {
        when (val estado = repartidor.calcularRendimiento()) {
            is EstadoPedido.Entregado -> {
                println("Repartidor [${repartidor.nombre}] -> BUEN RENDIMIENTO | Promedio: ${estado.tiempoMinutos} min")
            }
            EstadoPedido.EnCamino -> {
                println("Repartidor [${repartidor.nombre}] -> RENDIMIENTO REGULAR | En revisión")
            }
            is EstadoPedido.Cancelado -> {
                println("Repartidor [${repartidor.nombre}] -> RENDIMIENTO DEFICIENTE | Razón: ${estado.motivo}")
            }
        }
    }

    // 6. Operaciones de Kotlin sobre listas/colecciones (equivalentes a los Streams de Java).
    println("\n--- OPERACIONES SOBRE LISTAS ---")

    // 'count' con predicado es el equivalente a: collection.stream().filter(pred).count() en Java.
    val totalConBuenRendimiento = centro.repartidores.count { repartidor ->
        repartidor.calcularRendimiento() is EstadoPedido.Entregado
    }
    println("Repartidores con buen rendimiento: $totalConBuenRendimiento de ${centro.repartidores.size}")

    // 'minByOrNull' es el equivalente a: collection.stream().min(Comparator.comparing(...)).orElse(null).
    // Devuelve un valor NULEABLE (Repartidor?) porque la colección podría estar vacía.
    val repartidorMasRapido = centro.repartidores.minByOrNull { repartidor ->
        repartidor.entregasRegistradas.map { it.toDouble() }.average()
    }

    // 'let' recibe el valor SOLO SI no es null (gracias al "?." previo) y lo expone como "it"
    // (aquí renombrado a "repartidor"). Es el equivalente en Java a:
    // if (repartidorMasRapido != null) { ... } o a Optional.ofNullable(x).ifPresent(r -> {...}).
    repartidorMasRapido?.let { repartidor ->
        println("Repartidor más rápido: ${repartidor.nombre}")
    }

    // 7. Calculamos el promedio general de entregas del centro (usa 'reduce' internamente,
    // ver CentroDistribucion.calcularPromedioEntregaCentro). Lo envolvemos en un Result para
    // aplicar el equivalente Kotlin a un try/then/catch.
    println("\n--- RESUMEN DEL CENTRO ---")

    val resultadoPromedio = try {
        Result.success(centro.calcularPromedioEntregaCentro())
    } catch (e: Exception) {
        Result.failure(e)
    }

    resultadoPromedio
        .onSuccess { promedio ->
            println("Promedio general de entrega del centro ${centro.codigo}: %.2f min".format(promedio))
        }
        .onFailure { error ->
            println("No se pudo calcular el promedio del centro. Detalle: ${error.message}")
        }

    // 8. Notificación asíncrona con GlobalScope.launch + Job.join().
    println("\n--- NOTIFICACIÓN ASÍNCRONA (GlobalScope.launch) ---")

    // GlobalScope.launch es el equivalente conceptual a: new Thread(() -> { ... }).start();
    // o a executorService.execute(runnable) en Java: lanza una corrutina "suelta" que NO
    // depende del coroutineScope de runBlocking, y no bloquea el hilo principal mientras
    // se ejecuta. (Se usa GlobalScope solo con fines didácticos, igual que en el ejercicio
    // del aula: en producción se prefiere siempre un scope estructurado.)
    //
    // Dentro del bloque solo llamamos a una función 'suspend' que internamente usa 'delay'
    // sin abrir su propio coroutineScope ni lanzar más corrutinas (ver
    // CentroDistribucion.notificarClienteAsync): ese es el único lugar con 'delay'.
    // 'Job' es la REFERENCIA (el handle) a la corrutina que se lanza con 'launch', no su
    // resultado: es lo que te permite controlarla desde afuera (join(), cancel(), isActive...).
    // Equivalente a Java: el objeto Thread que obtienes al hacer "new Thread(runnable)" (puedes
    // hacer thread.join()/interrupt(), pero el Thread no "contiene" ningún valor de retorno), o
    // al Future<Void> que devuelve executorService.submit(runnable). En cambio, si hubiéramos
    // usado 'async' en lugar de 'launch', obtendríamos un 'Deferred<T>' (equivalente a un
    // Future<T> real, con valor, que se obtiene con '.await()' ≈ future.get()).
    val promedioParaNotificacion = resultadoPromedio.getOrDefault(0.0)
    val trabajoNotificacion: Job = GlobalScope.launch {
        val notificacion = centro.notificarClienteAsync(
            "tu pedido llegará en %.0f minutos aproximadamente".format(promedioParaNotificacion)
        )
        println(notificacion)
    }

    // Job.join() es el equivalente a Thread.join() en Java: suspende (sin bloquear el hilo)
    // hasta que la corrutina lanzada arriba termine, para no cerrar el programa antes de tiempo.
    trabajoNotificacion.join()
}
