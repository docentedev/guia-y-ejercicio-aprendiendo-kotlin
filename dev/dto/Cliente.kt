package com.rapidoya.dto

class Cliente(nombre: String, correo: String, contrasenia: String) : Usuario(nombre, correo, contrasenia) {

    // Los clientes tienen la misma tolerancia a intentos que un estudiante en el otro ejercicio.
    val INTENTOS_MAXIMOS = 3

    override fun iniciarSesion(nombreInput: String, contraseniaInput: String): Result<String> {
        val habilitado = super.intentos < INTENTOS_MAXIMOS
        if (!habilitado) {
            return Result.failure(IllegalStateException("Cuenta de cliente bloqueada por exceso de intentos."))
        }

        val mismaContrasenia = contraseniaInput == super.contrasenia
        val mismoNombre = nombreInput.lowercase() == super.nombre

        return if (mismaContrasenia && mismoNombre) {
            super.intentos = 0
            Result.success("¡Bienvenido cliente $nombre!")
        } else {
            super.intentos++
            Result.failure(IllegalArgumentException("Credenciales incorrectas. Intentos: ${super.intentos}"))
        }
    }
}
