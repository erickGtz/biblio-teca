package com.fcc.biblioteca.model

data class UsuarioCredencial(
    var id_credencial: Int = 0,
    var id_usuario: Int = 0,
    var correo: String = "",
    var usuario: String = "",
    var contrasena: String = ""
)
