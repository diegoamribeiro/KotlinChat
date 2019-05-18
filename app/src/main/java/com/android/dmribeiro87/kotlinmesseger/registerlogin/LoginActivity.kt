package com.android.dmribeiro87.kotlinmesseger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.dmribeiro87.kotlinmesseger.R
import com.android.dmribeiro87.kotlinmesseger.messages.LatestMessageActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar!!.hide()

        login_button.setOnClickListener {
            performLogin()
        }

        create_account_textview.setOnClickListener {
            finish()
        }
    }

    private fun performLogin(){

        val email = email_edittext_login.text.toString()
        val password = password_et_login.text.toString()

        if (email.isEmpty() && password.isEmpty()){
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            //return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("Login", "Sucesso ao logar em: ${it.result.user.uid}")

                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao logar", Toast.LENGTH_LONG).show()
            }

    }


}
