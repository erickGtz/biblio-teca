package com.fcc.biblioteca.model

data class Libro(
    var id_libro: Int = 0,
    var titulo: String = "",
    var categoria: String? = null,
    var autor: String? = null,
    var isbn: String? = null,
    var estado: String = "disponible", // enum('disponible','prestado','mantenimiento')
    var stock: Int = 1
)
