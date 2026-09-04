package com.jumbo.services

import com.jumbo.dto.Estudiante
import com.jumbo.dto.Profesor
import com.jumbo.dto.nota

abstract class Aula {
    abstract val profesor: Profesor
    abstract val estudiantes: MutableSet<Estudiante>
    abstract val capacidad: UInt
    abstract val codigo: String

    // Se declara 'suspend' para obligar a las clases hijas a resolver este cálculo dentro
    // de una corrutina (aunque, como verás abajo, no siempre se necesitan corrutinas "reales"
    // para cumplir la firma: suspend solo significa "esta función puede pausarse", no que
    // SIEMPRE deba hacerlo).
    abstract suspend fun calcularPromedioClase(): Double
}

// Implementación concreta de la clase Aula
class AulaRegular(
    override val profesor: Profesor,
    override val capacidad: UInt,
    override val codigo: String
) : Aula() {

    override val estudiantes: MutableSet<Estudiante> = mutableSetOf()

    // Método para inscribir estudiantes respetando la capacidad máxima
    fun inscribirEstudiante(estudiante: Estudiante): Boolean {
        if (estudiantes.size < capacidad.toInt()) {
            return estudiantes.add(estudiante)
        }
        return false // Aula llena
    }

    // 'suspend' se mantiene porque lo exige la clase abstracta Aula, pero aquí NO abrimos un
    // coroutineScope ni usamos async/awaitAll: el cálculo es secuencial sobre una lista, así
    // que no hace falta ningún "return@coroutineScope" (ese estilo de retorno etiquetado dentro
    // de un builder de corrutinas es difícil de leer y lo evitamos en todo el proyecto).
    override suspend fun calcularPromedioClase(): Double {
        val notas = estudiantes.map { estudiante -> estudiante.calcularPromedioFinal().nota }

        // 'fold' es el equivalente a Stream.reduce(identidad, acumulador) en Java: recibe un
        // valor inicial ("semilla", aquí 0.0) y por eso funciona incluso con una lista vacía,
        // a diferencia de 'reduce' (usado en AulaMaterializada), que lanza una excepción si la
        // colección está vacía porque no tiene semilla de dónde partir.
        val sumaNotas = notas.fold(0.0) { acumulado, nota -> acumulado + nota }

        // Única expresión de retorno de la función: evitamos "return" tempranos adicionales.
        return if (notas.isEmpty()) 0.0 else sumaNotas / notas.size
    }
}
