package com.rapidoya.services

import com.rapidoya.dto.Repartidor
import kotlinx.coroutines.delay

class CentroDistribucion(
    val capacidad: UInt,
    val codigo: String
) {
    init {
        // La capacidad no puede ser 0: nadie abre un centro de distribución sin cupo.
        require(capacidad > 0u) { "La capacidad del centro de distribución debe ser mayor a 0." }
    }

    val repartidores: MutableSet<Repartidor> = mutableSetOf()

    // Método para registrar repartidores respetando la capacidad máxima (mismo patrón que
    // Aula.agregarEstudiante en el otro ejercicio).
    fun registrarRepartidor(repartidor: Repartidor): Boolean {
        if (repartidores.size < capacidad.toInt()) {
            return repartidores.add(repartidor)
        }
        return false // Centro lleno
    }

    // 'suspend fun' SIN coroutineScope/async: el cálculo es secuencial sobre listas, por lo
    // tanto no necesitamos ningún "return@coroutineScope 0.0" para el caso vacío (ese estilo
    // de retorno etiquetado dentro de un builder de corrutinas se evita en todo el proyecto).
    suspend fun calcularPromedioEntregaCentro(): Double {
        // 'flatMap' aplana la lista de listas (una lista de tiempos POR repartidor) en una
        // sola lista de tiempos. Es el equivalente a Stream.flatMap(...) en Java.
        val tiempos = repartidores
            .flatMap { repartidor -> repartidor.entregasRegistradas }
            .map { it.toDouble() }

        if (tiempos.isEmpty()) {
            return 0.0
        }

        // 'reduce' es el equivalente a Stream.reduce(acumulador) en Java (SIN valor inicial):
        // toma el primer elemento como semilla. Por eso arriba descartamos primero el caso
        // vacío: reduce() sobre una colección vacía lanza UnsupportedOperationException.
        val sumaTiempos = tiempos.reduce { acumulado, tiempo -> acumulado + tiempo }

        return sumaTiempos / tiempos.size
    }

    // Función 'suspend' "pura": su única responsabilidad es pausar (delay) y devolver un
    // resultado. NO abre su propio coroutineScope ni lanza corrutinas nuevas: eso es
    // responsabilidad de quien la LLAMA (ver Main.kt, sección de GlobalScope.launch). Simula
    // la latencia de invocar un servicio externo de notificaciones push/SMS.
    //
    // 'delay' es el equivalente conceptual a Thread.sleep() en Java, pero SIN bloquear el
    // hilo: libera el hilo subyacente para que pueda hacer otro trabajo mientras "espera".
    suspend fun notificarClienteAsync(mensaje: String): String {
        delay(500L)
        return "Notificación enviada al cliente -> $mensaje"
    }
}
