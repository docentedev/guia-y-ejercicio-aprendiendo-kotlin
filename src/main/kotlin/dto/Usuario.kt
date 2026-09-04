package com.jumbo.dto

abstract class Usuario(nombreInput: String, correoInput: String, contraseniaInput: String) {

    companion object {
        val IS_EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    }

    val nombre: String
    val correo: String
    val contrasenia: String
    var intentos: Int = 0

    init {
        require(nombreInput.length > 3) { "El nombre de usuario debe tener más de 3 caracteres." }
        this.nombre = nombreInput.lowercase()

        require(correoInput.matches(IS_EMAIL)) { "El formato del correo electrónico no es válido." }
        this.correo = correoInput.lowercase()

        require(contraseniaInput.length >= 8) { "La contraseña debe tener al menos 8 caracteres." }
        this.contrasenia = contraseniaInput
    }

    abstract fun iniciarSesion(nombreInput: String, contraseniaInput: String): Result<String>
}