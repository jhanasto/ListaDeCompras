@file:Suppress("DEPRECATION")
package com.uct.listadecompras

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ListaActivity : AppCompatActivity() {
    private lateinit var adapter: ComprasAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configurar el RecyclerView y el adaptador
        val recyclerView: RecyclerView = findViewById(R.id.recyclerCompras)
        adapter = ComprasAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Referencia al FloatingActionButton
        val salir : FloatingActionButton = findViewById(R.id.btnSalir)

        // Botón de cerrar sesión
        salir.setOnClickListener {
            // Mostrar un diálogo de confirmación antes de cerrar sesión+
            showLogoutConfirmationDialog()
        }

        val agregar: FloatingActionButton = findViewById(R.id.agregar)
        agregar.setOnClickListener {
            agregarFirebase()
        }

        listenToFirestoreUpdates()
    }

    @SuppressLint("InflateParams")
    private fun agregarFirebase() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogLayout = inflater.inflate(R.layout.agregar, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextItemName)

        with(builder) {
            setTitle("Agregar Compra")
            setPositiveButton("Anotar") { dialog, which ->
                val itemName = editText.text.toString()
                if (itemName.isNotEmpty()) {
                    addItemToFirestore(itemName)
                }
            }
            setNegativeButton("Cancelar") { dialog, which ->
                // No hacer nada
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun addItemToFirestore(itemName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        val newItem = hashMapOf(
            "nombre" to itemName,
            "userId" to userId  // Agregar el ID del usuario aquí
        )
        firestore.collection("compras")
            .add(newItem)
            .addOnSuccessListener {
                // Item añadido exitosamente
            }
            .addOnFailureListener {
                // Error al añadir item
            }
    }

    private fun listenToFirestoreUpdates() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        firestore.collection("compras")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Error al obtener documentos
                    return@addSnapshotListener
                }

                val itemList = mutableListOf<Compras>()
                for (doc in snapshots!!) {
                    val item = doc.toObject(Compras::class.java)
                    itemList.add(item)
                }
                adapter.submitList(itemList)
                }
        }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro que quieres cerrar sesión?")
        builder.setPositiveButton("Aceptar") { dialog, which ->
            // Cerrar sesión del usuario
            auth.signOut()
            googleSignInClient.signOut()
            // Redirigir al usuario a la pantalla de inicio de sesión
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Opcional: cerrar esta actividad
        }
        builder.setNegativeButton("Cancelar") { dialog, which ->
            // No hacer nada, el usuario decidió no cerrar sesión
        }
        builder.show()
    }
}

