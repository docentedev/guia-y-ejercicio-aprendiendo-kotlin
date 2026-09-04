package com.jumbo.dto

import com.jumbo.services.ICapacidadEstudiantes

class Estudiante(nombre: String, correo: String, contrasenia: String) : Usuario(nombre, correo, contrasenia), ICapacidadEstudiantes {
    val INTENTOS_MAXIMOS = 3

    // Implementación de la lista de pruebas de la interfaz
    override val pruebasRendidas: MutableList<UInt> = mutableListOf()

    override fun rendirPrueba(puntaje: UInt) {
        require(puntaje <= 100u) { "El puntaje de la prueba no puede superar los 100 puntos." }
        pruebasRendidas.add(puntaje)
    }

    override fun calcularPromedioFinal(): EstadoCalificacion {
        if (pruebasRendidas.isEmpty()) {
            return EstadoCalificacion.Pendiente(1.0, 0u, "No hay pruebas registradas.")
        }

        // 1. Calculamos el promedio de puntos (0 a 100)
        val promedioPuntosDouble = pruebasRendidas.map { it.toDouble() }.average()
        val promedioPuntosUInt = promedioPuntosDouble.toUInt()

        // 2. Transformamos los puntos a la escala de notas de 1.0 a 7.0
        // Fórmula: 1.0 + (promedio de puntos / 100) * 6.0
        val notaFinal = 1.0 + (promedioPuntosDouble / 100.0) * 6.0

        // 3. Evaluamos según las reglas solicitadas
        return when {
            notaFinal >= 4.0 -> EstadoCalificacion.Aprobado(notaFinal, promedioPuntosUInt)
            notaFinal in 3.5..3.9 -> EstadoCalificacion.Pendiente(notaFinal, promedioPuntosUInt, "Estudiante en rango de revisión pendiente.")
            else -> EstadoCalificacion.Reprobado(notaFinal, promedioPuntosUInt)
        }
    }

    override fun iniciarSesion(nombreInput: String, contraseniaInput: String): Result<String> {
        val habilitado = super.intentos < INTENTOS_MAXIMOS
        if (!habilitado) {
            return Result.failure(IllegalStateException("Cuenta bloqueada por exceso de intentos."))
        }

        val mismaContrasenia = contraseniaInput == super.contrasenia
        val mismoNombre = nombreInput.lowercase() == super.nombre

        return if (mismaContrasenia && mismoNombre) {
            super.intentos = 0
            Result.success("¡Bienvenido estudiante $nombre!")
        } else {
            super.intentos++
            Result.failure(IllegalArgumentException("Credenciales incorrectas. Intentos: ${super.intentos}"))
        }
    }
}