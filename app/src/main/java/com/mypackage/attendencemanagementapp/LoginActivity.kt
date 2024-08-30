package com.mypackage.attendencemanagementapp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.login_btn)
        val emailField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Email/Password Login
        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show()
            }
        }
        val registerText = findViewById<TextView>(R.id.register)
        registerText.setOnClickListener {
            val openSignUpActivity = Intent(this, RegisterActivity::class.java)
            startActivity(openSignUpActivity)
        }

        // Google Sign-In
        val googleSignInButton = findViewById<Button>(R.id.button_google)
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Navigate to Register Activity
        val signInText = findViewById<TextView>(R.id.register)
        signInText.setOnClickListener {
            val openSignUpActivity = Intent(this, RegisterActivity::class.java)
            startActivity(openSignUpActivity)
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val user = auth.currentUser
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("USER_NAME", user?.displayName)
                    startActivity(intent)
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("USER_NAME", user?.displayName)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}
