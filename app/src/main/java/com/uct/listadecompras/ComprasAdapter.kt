    package com.uct.listadecompras

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageButton
    import android.widget.TextView
    import androidx.recyclerview.widget.DiffUtil
    import androidx.recyclerview.widget.ListAdapter
    import androidx.recyclerview.widget.RecyclerView
    import com.google.firebase.Firebase
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.firestore

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    class ComprasAdapter : ListAdapter<Compras, ComprasAdapter.ComprasViewHolder>(ComprasDiffCallback()) {

        inner class ComprasViewHolder(item: View) : RecyclerView.ViewHolder(item) {
            private val name = item.findViewById<TextView>(R.id.viewName)
            private val btnEliminar = item.findViewById<ImageButton>(R.id.btnEliminar)

            fun bind(compra: Compras) {
                name.text = compra.nombre

                btnEliminar.setOnClickListener {
                    eliminarCompra(compra)
                }
            }
        }
        fun eliminarCompra(compra: Compras) {
            val db = Firebase.firestore
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""
            val comprasRef = db.collection("compras")

            comprasRef
                .whereEqualTo("userId", userId) // Filtrar por userId
                .whereEqualTo("nombre", compra.nombre) // Filtrar por nombre
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        comprasRef.document(document.id).delete()
                            .addOnSuccessListener {
                                // Éxito al eliminar
                                // Aquí puedes agregar cualquier lógica adicional después de eliminar
                            }
                            .addOnFailureListener { e ->
                                // Manejar errores al eliminar
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar errores al obtener la colección
                }
        }



        class ComprasDiffCallback : DiffUtil.ItemCallback<Compras>() {
            override fun areItemsTheSame(oldItem: Compras, newItem: Compras): Boolean {
                return oldItem.nombre == newItem.nombre
            }

            override fun areContentsTheSame(oldItem: Compras, newItem: Compras): Boolean {
                return oldItem == newItem
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComprasViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.compras_list, parent, false)
            return ComprasViewHolder(view)
        }

        override fun onBindViewHolder(holder: ComprasViewHolder, position: Int) {
            val producto = getItem(position)
            holder.bind(producto)
            }
    }