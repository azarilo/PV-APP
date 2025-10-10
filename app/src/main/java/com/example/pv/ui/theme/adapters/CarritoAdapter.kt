package com.example.pv.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.pv.R
import com.example.pv.model.Producto

class CarritoAdapter(
    private val productos: MutableList<Producto>,
    private val onCambio: () -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.textNombre.text = producto.nombre
        holder.textPrecio.text = "Precio: \$${producto.precio}"
        holder.textCategoria.text = "Categoría: ${producto.categoria}"
        holder.textCantidad.text = "x${producto.cantidad}"

        holder.botonMas.setOnClickListener {
            producto.cantidad++
            notifyItemChanged(position)
            onCambio()
        }

        holder.botonMenos.setOnClickListener {
            if (producto.cantidad > 1) {
                producto.cantidad--
                notifyItemChanged(position)
                onCambio()
            }
        }

        holder.botonEliminar.setOnClickListener {
            productos.removeAt(position)
            notifyItemRemoved(position)
            onCambio()
        }
    }

    override fun getItemCount(): Int = productos.size

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNombre: TextView = itemView.findViewById(R.id.textNombre)
        val textPrecio: TextView = itemView.findViewById(R.id.textPrecio)
        val textCategoria: TextView = itemView.findViewById(R.id.textCategoria)
        val textCantidad: TextView = itemView.findViewById(R.id.textCantidad)
        val botonMas: Button = itemView.findViewById(R.id.botonMas)
        val botonMenos: Button = itemView.findViewById(R.id.botonMenos)
        val botonEliminar: Button = itemView.findViewById(R.id.botonEliminar)
    }
}
