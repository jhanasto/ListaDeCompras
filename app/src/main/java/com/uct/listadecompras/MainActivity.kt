@file:Suppress("DEPRECATION")

package com.uct.listadecompras

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("NAME_SHADOWING", "UNUSED_PARAMETER", "UNUSED_ANONYMOUS_PARAMETER")
class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

        auth = FirebaseAuth.getInstance()

        // Configuración del cliente de inicio de sesión de Google
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Configurar el botón de Google para iniciar sesión
        val btnGoogle = findViewById<ImageButton>(R.id.btnGoogle)
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            mostrarDialogoRegistro()
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.etUsername).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                iniciarSesionAnonima()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesionAnonima() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, navigate to ListaActivity
                    val intent = Intent(this, ListaActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign-in failed
                    Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @SuppressLint("InflateParams")
    private fun mostrarDialogoRegistro() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.registro, null)
        val user = dialogLayout.findViewById<EditText>(R.id.User)
        val password = dialogLayout.findViewById<EditText>(R.id.Password)

        builder.apply {
            setTitle("Registrar Usuario")
            setPositiveButton("Registrar") { dialog, which ->
                val email = user.text.toString()
                val password = password.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    registrarAnonimo(email, password)
                } else {
                    Toast.makeText(this@MainActivity, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancelar") { dialog, which ->
                // No hacer nada
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun registrarAnonimo(email: String, password: String) {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    runOnUiThread {
                        Toast.makeText(this, "Usuario registrado ", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    // Sign-in failed
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Error al registrar el usuario: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
            finish()
            }
        }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(Exception::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, ListaActivity::class.java))
            finish()
        } else {
            googleSignInClient.signOut().addOnCompleteListener {

            }
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }


}
