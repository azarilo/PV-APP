package com.example.pv.utils

import com.example.pv.model.Producto
import org.json.JSONObject

fun parsearProductoCSV(texto: String): Producto? {
    val partes = texto.split(",")
    return if (partes.size >= 2) {
        val nombre = partes[0].trim()
        val precio = partes[1].trim().toDoubleOrNull()
        val categoria = if (partes.size >= 3) partes[2].trim() else "Sin categoría"
        if (precio != null) Producto(nombre, precio, categoria) else null
    } else null
}

fun parsearProductoJSON(texto: String): Producto? {
    return try {
        val json = JSONObject(texto)
        val nombre = json.getString("nombre")
        val precio = json.getDouble("precio")
        val categoria = json.optString("categoria", "Sin categoría")
        Producto(nombre, precio, categoria)
    } catch (e: Exception) {
        null
    }
}
