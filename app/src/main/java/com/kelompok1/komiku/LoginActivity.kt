package com.kelompok1.komiku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelompok1.komiku.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email == "admin@komiku.id" && pass == "admin123") {
                // Login as Admin
                getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_in", true)
                    .putBoolean("is_admin", true)
                    .apply()
                
                val intent = Intent(this, com.kelompok1.komiku.admin.AdminActivity::class.java)
                android.util.Log.d("LoginActivity", "Logging in as Admin, starting AdminActivity")
                startActivity(intent)
                finish()
            } else if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Login as User (Dummy)
                getSharedPreferences("komiku_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_in", true)
                    .putBoolean("is_admin", false)
                    .apply()
                
                val intent = Intent(this, MainActivity::class.java)
                android.util.Log.d("LoginActivity", "Logging in as User, starting MainActivity")
                startActivity(intent)
                finish()
            }
        }
    }
}
