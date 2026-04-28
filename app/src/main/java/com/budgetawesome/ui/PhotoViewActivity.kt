package com.budgetawesome.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.budgetawesome.databinding.ActivityPhotoViewBinding
import java.io.File

class PhotoViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Expense Photo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val path = intent.getStringExtra("photo_path")
        if (path != null) {
            binding.ivPhoto.setImageURI(Uri.fromFile(File(path)))
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
