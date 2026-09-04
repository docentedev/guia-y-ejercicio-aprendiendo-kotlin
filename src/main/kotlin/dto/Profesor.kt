package com.jumbo.dto

class Profesor(nombre: String, correo: String, contrasenia: String) : Usuario(nombre, correo, contrasenia) {

    // Los profesores tienen un límite de intentos más restrictivo
    val INTENTOS_MAXIMOS = 1

    override fun iniciarSesion(nombreInput: String, contraseniaInput: String): Result<String> {

        val habilitado = super.intentos < INTENTOS_MAXIMOS
        if (!habilitado) {
            return Result.failure(IllegalStateException("Acceso de profesor bloqueado por seguridad."))
        }

        val mismaContrasenia = contraseniaInput == super.contrasenia
        val mismoNombre = nombreInput.lowercase() == super.nombre

        return if (mismaContrasenia && mismoNombre) {
            super.intentos = 0
            Result.success("¡Bienvenido profesor $nombre!")
        } else {
            super.intentos++
            Result.failure(IllegalArgumentException("Credenciales de profesor incorrectas. Intentos fallidos: ${super.intentos}"))
        }
    }
}