@file:Suppress("DEPRECATION")
package com.uct.listadecompras

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ListaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista)
        auth = FirebaseAuth.getInstance()

        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Referencia al FloatingActionButton
        val fabAddItem2: FloatingActionButton = findViewById(R.id.fabAddItem2)

        // Botón de cerrar sesión
        fabAddItem2.setOnClickListener {
            // Mostrar un diálogo de confirmación antes de cerrar sesión+
            showLogoutConfirmationDialog()
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

