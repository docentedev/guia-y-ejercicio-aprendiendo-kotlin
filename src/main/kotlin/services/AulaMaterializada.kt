package com.jumbo.services

import com.jumbo.dto.Estudiante
import com.jumbo.dto.Profesor
import com.jumbo.dto.nota
import kotlinx.coroutines.delay

class AulaMaterializada(
    override val profesor: Profesor,
    override val capacidad: UInt,
    override val codigo: String
) : Aula() {

    override val estudiantes: MutableSet<Estudiante> = mutableSetOf()

    // Método para agregar estudiantes validando la capacidad máxima del aula
    fun agregarEstudiante(estudiante: Estudiante): Boolean {
        if (estudiantes.size < capacidad.toInt()) {
            return estudiantes.add(estudiante)
        }
        return false // Aula llena
    }

    // 'suspend fun' sin coroutineScope/async: el cálculo es puramente secuencial sobre una
    // lista, así que no necesitamos "return@coroutineScope 0.0" para el caso vacío. Ese estilo
    // de retorno etiquetado dentro de un builder de corrutinas se evita en todo el proyecto.
    //
    // 'reduce' es el equivalente a Stream.reduce(acumulador) en Java (SIN valor inicial): toma
    // el primer elemento como semilla y va acumulando. Por eso primero descartamos el caso de
    // lista vacía con un único 'return' de guarda: reduce() sobre una colección vacía lanza
    // UnsupportedOperationException, tal como Java lanza NoSuchElementException / no puede
    // reducir un Stream vacío sin una identidad explícita.
    override suspend fun calcularPromedioClase(): Double {
        if (estudiantes.isEmpty()) {
            return 0.0
        }

        val notas = estudiantes.map { estudiante -> estudiante.calcularPromedioFinal().nota }
        val sumaNotas = notas.reduce { acumulado, nota -> acumulado + nota }

        return sumaNotas / notas.size
    }

    // Función 'suspend' "pura": su única responsabilidad es pausar (delay) y devolver un
    // resultado. NO abre su propio coroutineScope ni lanza corrutinas nuevas (no hay async ni
    // launch aquí dentro): eso es responsabilidad de quien la LLAMA (ver Main.kt, sección de
    // GlobalScope.launch). Simula, por ejemplo, la latencia de invocar un servicio externo que
    // genera un reporte/PDF.
    //
    // 'delay' es el equivalente conceptual a Thread.sleep() en Java, pero SIN bloquear el hilo:
    // libera el hilo subyacente para que pueda hacer otro trabajo mientras la corrutina "espera".
    suspend fun generarReporteAsync(promedio: Double): String {
        delay(500L)
        return "Reporte generado para el aula $codigo -> Promedio general: %.2f".format(promedio)
    }
}
