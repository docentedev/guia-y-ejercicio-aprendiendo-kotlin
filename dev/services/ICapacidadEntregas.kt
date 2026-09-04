package com.rapidoya.services

import com.rapidoya.dto.EstadoPedido

// Mismo rol que ICapacidadEstudiantes en el ejercicio del aula: define el "contrato" que debe
// cumplir cualquier tipo que registre entregas y calcule su propio rendimiento. Equivalente a
// una interface de Java con métodos + un campo expuesto solo por getter (acá, una 'val').
interface ICapacidadEntregas {
    // Listado de tiempos de entrega (en minutos) registrados por el repartidor.
    val entregasRegistradas: MutableList<UInt>

    // Registra una nueva entrega validando su tiempo.
    fun registrarEntrega(tiempoMinutos: UInt)

    // Calcula el rendimiento acumulado y retorna el estado correspondiente.
    fun calcularRendimiento(): EstadoPedido
}
