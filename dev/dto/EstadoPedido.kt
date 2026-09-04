package com.rapidoya.dto

// Igual patrón que EstadoCalificacion en el ejercicio del aula (com.jumbo.dto.EstadoCalificacion):
// una jerarquía 'sealed' que representa el rendimiento derivado de un repartidor a partir de
// sus entregas registradas. El compilador conoce TODAS las subclases posibles, así que un
// 'when' sobre un EstadoPedido es exhaustivo (no necesita rama 'else'), como un switch "sellado".
//
// 'EnCamino' se declara como 'object' (no 'data class') porque no necesita datos propios: es
// un único valor, equivalente a una constante de enum sin campos en Java (ej. Status.PENDING).
sealed class EstadoPedido {
    object EnCamino : EstadoPedido()
    data class Entregado(val tiempoMinutos: Int) : EstadoPedido()
    data class Cancelado(val motivo: String) : EstadoPedido()
}
