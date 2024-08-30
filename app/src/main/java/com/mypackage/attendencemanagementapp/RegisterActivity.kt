package com.mypackage.attendencemanagementapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignUpButton: Button = findViewById(R.id.button_google)
        googleSignUpButton.setOnClickListener {
            signInWithGoogle()
        }

        val signUpButton: Button = findViewById(R.id.signUp)
        signUpButton.setOnClickListener {
            createAccount()
        }

        val loginTextView: TextView = findViewById(R.id.log)
        loginTextView.setOnClickListener {
             startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun createAccount() {
        val usernameEditText: EditText = findViewById(R.id.username)
        val emailEditText: EditText = findViewById(R.id.emailid)
        val phoneEditText: EditText = findViewById(R.id.phoneno)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.cnf_password)

        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val loginTextView: TextView = findViewById(R.id.log)
        loginTextView.setOnClickListener {
             startActivity(Intent(this, LoginActivity::class.java))
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-up successful
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Sign-up successful", Toast.LENGTH_SHORT).show()
                    // Navigate to your main activity or home screen
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                } else {
                    // If sign-up fails
                    Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.result
            firebaseAuthWithGoogle(account)
        } catch (e: Exception) {
            Log.w("Register", "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Account creation successful
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Google account created successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to your main activity or home screen
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))

                } else {
                    // If sign-in fails
                    Log.w("Register", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
