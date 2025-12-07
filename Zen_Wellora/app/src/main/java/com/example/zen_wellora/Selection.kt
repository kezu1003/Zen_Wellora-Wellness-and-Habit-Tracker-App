package com.example.zen_wellora

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Selection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val mainView: View = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cvLogin: CardView = findViewById(R.id.cv_login)
        val cvSignup: CardView = findViewById(R.id.cv_signup)

        cvLogin.setOnClickListener {
            // replace Login::class.java with your actual Login activity class
            startActivity(Intent(this, Login::class.java))
        }

        cvSignup.setOnClickListener {
            // replace Signup::class.java with your actual Signup activity class
            startActivity(Intent(this, Signup::class.java))
        }
    }
}
