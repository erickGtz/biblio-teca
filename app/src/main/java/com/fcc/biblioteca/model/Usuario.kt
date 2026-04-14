package com.fcc.biblioteca.model

data class Usuario(
    var id_usuario: Int = 0,
    var nombre: String = "",
    var apellido1: String = "",
    var apellido2: String? = null,
    var rol: String = "usuario" // enum('admin','usuario')
)
