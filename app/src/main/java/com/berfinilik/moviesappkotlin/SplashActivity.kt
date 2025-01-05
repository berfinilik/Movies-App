package com.berfinilik.moviesappkotlin.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MoviesAppKotlin)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
