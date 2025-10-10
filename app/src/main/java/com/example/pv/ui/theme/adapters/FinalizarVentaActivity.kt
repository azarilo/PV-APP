package com.example.pv.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pv.R
import com.example.pv.data.Carrito
import com.example.pv.model.Producto
import com.example.pv.ui.adapters.CarritoAdapter
import com.example.pv.utils.parsearProductoCSV
import com.google.zxing.integration.android.IntentIntegrator
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

class FinalizarVentaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textoTotal: TextView
    private lateinit var adapter: CarritoAdapter
    private val archivo by lazy {
        File(getExternalFilesDir(null), "ventas_${LocalDate.now()}.csv")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizar_venta)

        recyclerView = findViewById(R.id.recyclerProductos)
        textoTotal = findViewById(R.id.textoTotal)
        val botonEscanear = findViewById<Button>(R.id.botonEscanear)
        val botonCompartir = findViewById<Button>(R.id.botonCompartir)
        val botonCerrar = findViewById<Button>(R.id.botonCerrar)
        val botonResumen = findViewById<Button>(R.id.botonResumen)
        val botonFinalizarDia = findViewById<Button>(R.id.botonFinalizarDia)

        adapter = CarritoAdapter(Carrito.productos) {
            actualizarResumen()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        botonEscanear.setOnClickListener {
            IntentIntegrator(this).initiateScan()
        }

        botonCompartir.setOnClickListener {
            if (!archivo.exists()) {
                Toast.makeText(this, "No hay archivo para compartir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", archivo)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Compartir archivo de venta"))
        }

        botonCerrar.setOnClickListener {
            if (Carrito.productos.isEmpty()) {
                Toast.makeText(this, "No hay productos en el carrito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            actualizarCSV()
            Carrito.productos.clear()
            adapter.notifyDataSetChanged()
            actualizarResumen()
            Toast.makeText(this, "Venta cerrada", Toast.LENGTH_SHORT).show()
        }

        botonResumen.setOnClickListener {
            if (!archivo.exists()) {
                Toast.makeText(this, "No hay archivo de ventas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lineas = archivo.readLines().drop(1)
            if (lineas.isEmpty()) {
                Toast.makeText(this, "El archivo está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resumen = StringBuilder("Resumen de ventas:\n\n")
            lineas.forEach { linea ->
                val partes = linea.split(",")
                val nombre = partes.getOrNull(0) ?: "?"
                val precio = partes.getOrNull(1) ?: "?"
                val categoria = partes.getOrNull(2) ?: "?"
                val cantidad = partes.getOrNull(3) ?: "?"
                val hora = partes.getOrNull(4) ?: "?"
                resumen.append("• $nombre ($categoria) - $cantidad × $$precio a las $hora\n")
            }

            AlertDialog.Builder(this)
                .setTitle("Resumen de ventas")
                .setMessage(resumen.toString())
                .setPositiveButton("OK", null)
                .show()
        }

        botonFinalizarDia.setOnClickListener {
            if (archivo.exists()) archivo.delete()
            Carrito.productos.clear()
            adapter.notifyDataSetChanged()
            actualizarResumen()
            Toast.makeText(this, "Día finalizado. Archivo y carrito borrados.", Toast.LENGTH_SHORT).show()
        }

        // Easter egg: 5 toques en el total
        var toqueContador = 0
        textoTotal.setOnClickListener {
            toqueContador++
            if (toqueContador >= 5) {
                try {
                    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Toast.makeText(this, "👾 App creada por Azarilo. ¡Gracias por descubrirme!", Toast.LENGTH_LONG).show()
                toqueContador = 0
            }
        }

        actualizarResumen()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val texto = result.contents
            val producto = parsearProductoCSV(texto) ?: Producto(texto, 20.0)
            val existente = Carrito.productos.find { it.nombre == producto.nombre }
            if (existente != null) {
                existente.cantidad++
            } else {
                Carrito.productos.add(producto)
            }
            adapter.notifyDataSetChanged()
            actualizarResumen()
            actualizarCSV()
            Toast.makeText(this, "Producto agregado: ${producto.nombre}", Toast.LENGTH_SHORT).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun actualizarResumen() {
        val total = Carrito.productos.sumOf { it.precio * it.cantidad }
        textoTotal.text = "Total: $${"%.2f".format(total)}"
    }

    private fun actualizarCSV() {
        if (!archivo.exists()) {
            archivo.writeText("Nombre,Precio,Categoría,Cantidad,Hora\n")
        }
        val horaActual = LocalTime.now().toString()
        Carrito.productos.forEach {
            archivo.appendText("${it.nombre},${it.precio},${it.categoria},${it.cantidad},$horaActual\n")
        }
    }
}
