package com.fcc.biblioteca.model

import java.io.Serializable

data class Prestamo(
    var id_prestamo: Int = 0,
    var libro: Libro,
    var fechaInicio: String,
    var fechaFin: String
) : Serializable
