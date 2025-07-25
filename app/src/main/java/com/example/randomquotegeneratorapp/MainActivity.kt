package com.example.randomquotegeneratorapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.randomquotegeneratorapp.databinding.ActivityMainBinding
import com.example.randomquotegeneratorapp.retrofit.QuoteModel
import com.example.randomquotegeneratorapp.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getQuote()
        binding.btnNew.setOnClickListener {
            getQuote()
        }
    }

    private fun getQuote() {
        setProgressBar(true)

        lifecycleScope.launch {
            val result = runCatching {
                RetrofitInstance.quoteApi.getQuote()
            }

            result.onSuccess { response ->
                if (response.isSuccessful) {
                    val quote = response.body()?.firstOrNull()
                    if (quote != null) {
                        setUI(quote)
                    } else {
                        showError("No quote found.")
                    }
                } else {
                    showError("Server error: ${response.code()}")
                }
            }.onFailure { error ->
                showError(
                    when (error) {
                        is IOException -> "Check your internet connection."
                        else -> "An unexpected error occurred."
                    }
                )
            }

            setProgressBar(false)
        }
    }

    private fun setUI(quote: QuoteModel) {
        binding.txtQuote.text = quote.q
        binding.txtAuthor.text = if (quote.a.isNotBlank()) "-${quote.a}" else ""
    }

    private fun setProgressBar(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnNew.isVisible = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
