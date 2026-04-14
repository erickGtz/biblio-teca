package com.fcc.biblioteca.model

data class Prestamo(
    var id_prestamo: Int = 0,
    var id_usuario: Int = 0,
    var id_libro: Int = 0,
    var fecha_inicio: String = "", // Format: YYYY-MM-DD
    var fecha_fin: String? = null
)
