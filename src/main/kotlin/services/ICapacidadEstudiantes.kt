package com.jumbo.services

import com.jumbo.dto.EstadoCalificacion

interface ICapacidadEstudiantes {
    // Listado de pruebas rendidas (puntajes de 0 a 100)
    val pruebasRendidas: MutableList<UInt>

    // Método para rendir una nueva prueba validando su puntaje
    fun rendirPrueba(puntaje: UInt)

    // Método para calcular el promedio final y retornar el estado correspondiente
    fun calcularPromedioFinal(): EstadoCalificacion
}