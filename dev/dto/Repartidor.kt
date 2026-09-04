package com.rapidoya.dto

import com.rapidoya.services.ICapacidadEntregas

class Repartidor(nombre: String, correo: String, contrasenia: String) :
    Usuario(nombre, correo, contrasenia), ICapacidadEntregas {

    val INTENTOS_MAXIMOS = 3

    // Implementación de la lista de entregas exigida por la interfaz.
    override val entregasRegistradas: MutableList<UInt> = mutableListOf()

    // Tiempo máximo aceptable para UNA entrega, en minutos. Es una validación de dato de
    // entrada (como un "esto no puede pasar"), distinta de las reglas de negocio de
    // calcularRendimiento() (que evalúan el PROMEDIO, no una entrega individual).
    private val TIEMPO_MAXIMO_MINUTOS = 180u

    override fun registrarEntrega(tiempoMinutos: UInt) {
        require(tiempoMinutos > 0u) { "El tiempo de entrega debe ser mayor a 0 minutos." }
        require(tiempoMinutos <= TIEMPO_MAXIMO_MINUTOS) {
            "El tiempo de entrega no puede superar los $TIEMPO_MAXIMO_MINUTOS minutos."
        }
        entregasRegistradas.add(tiempoMinutos)
    }

    // Sin coroutineScope/async y sin ningún "return@coroutineScope": es un cálculo secuencial
    // sobre una lista, así que el único 'return' que hay es una guarda simple para el caso vacío.
    override fun calcularRendimiento(): EstadoPedido {
        if (entregasRegistradas.isEmpty()) {
            return EstadoPedido.Cancelado("Sin entregas registradas.")
        }

        // 'fold' es el equivalente a Stream.reduce(identidad, acumulador) en Java: recibe un
        // valor inicial (0.0) y por eso funciona sin problema aunque ya sabemos que la lista
        // no está vacía (se usa igual, por consistencia con CentroDistribucion).
        val sumaMinutos = entregasRegistradas.fold(0.0) { acumulado, minutos -> acumulado + minutos.toDouble() }
        val promedioMinutos = sumaMinutos / entregasRegistradas.size

        // Reglas de negocio del ejemplo (tú puedes ajustarlas): a menor tiempo promedio,
        // mejor rendimiento.
        return when {
            promedioMinutos <= 20.0 -> EstadoPedido.Entregado(promedioMinutos.toInt())
            promedioMinutos <= 30.0 -> EstadoPedido.EnCamino
            else -> EstadoPedido.Cancelado("Tiempo promedio de entrega excede el máximo aceptable.")
        }
    }

    override fun iniciarSesion(nombreInput: String, contraseniaInput: String): Result<String> {
        val habilitado = super.intentos < INTENTOS_MAXIMOS
        if (!habilitado) {
            return Result.failure(IllegalStateException("Cuenta de repartidor bloqueada por exceso de intentos."))
        }

        val mismaContrasenia = contraseniaInput == super.contrasenia
        val mismoNombre = nombreInput.lowercase() == super.nombre

        return if (mismaContrasenia && mismoNombre) {
            super.intentos = 0
            Result.success("¡Bienvenido repartidor $nombre!")
        } else {
            super.intentos++
            Result.failure(IllegalArgumentException("Credenciales incorrectas. Intentos: ${super.intentos}"))
        }
    }
}
