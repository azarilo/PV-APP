package com.example.pv.model

data class Producto(
    val nombre: String,
    val precio: Double,
    val categoria: String = "Sin categoría",
    var cantidad: Int = 1
)
