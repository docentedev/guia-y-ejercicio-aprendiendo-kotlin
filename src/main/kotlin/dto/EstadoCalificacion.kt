package com.jumbo.dto

// 'sealed class' es el equivalente en Kotlin a un enum "enriquecido" con datos distintos por
// variante. En Java, para lograr algo similar sin sealed interfaces (disponibles desde Java 17)
// se recurría al patrón Visitor o a una jerarquía abierta con instanceof + casting manual.
// Como el compilador SÍ conoce todas las subclases posibles, un 'when' sobre un
// EstadoCalificacion no necesita una rama 'else': es exhaustivo, como un switch "sellado".
sealed class EstadoCalificacion {
    data class Aprobado(val nota: Double, val promedioPuntos: UInt) : EstadoCalificacion()
    data class Reprobado(val nota: Double, val promedioPuntos: UInt) : EstadoCalificacion()
    data class Pendiente(val nota: Double, val promedioPuntos: UInt, val mensaje: String) : EstadoCalificacion()
}

// Extension property: agrega una propiedad calculada "nota" a CUALQUIER EstadoCalificacion,
// sin modificar la clase sellada ni repetir el 'when' en cada lugar donde se necesite la nota.
// Es el equivalente a un método utilitario estático en Java (ej. EstadoUtils.getNota(estado)),
// pero con sintaxis de propiedad: se escribe "estado.nota", no "EstadoUtils.getNota(estado)".
// Nota: dentro de cada rama, "nota" resuelve al miembro de esa subclase (Aprobado.nota, etc.),
// no a esta misma extensión, porque Kotlin prioriza los miembros sobre las extensiones.
val EstadoCalificacion.nota: Double
    get() = when (this) {
        is EstadoCalificacion.Aprobado -> nota
        is EstadoCalificacion.Reprobado -> nota
        is EstadoCalificacion.Pendiente -> nota
    }
