package com.budgetawesome.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.data.entity.Expense
import com.budgetawesome.databinding.ActivityAddExpenseBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var session: SessionManager
    private var photoPath: String? = null
    private var photoUri: Uri? = null
    private var selectedDate = ""
    private var startTime = ""
    private var endTime = ""
    private var categoryList = listOf<com.budgetawesome.data.entity.Category>()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoPath != null) {
            showPhotoPreview(photoPath!!)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoPath = copyUriToFile(it)
            showPhotoPreview(photoPath!!)
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.CAMERA] == true) openCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Add Expense"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        session = SessionManager(this)

        // Default date/time
        val cal = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
        cal.add(Calendar.HOUR_OF_DAY, 1)
        endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
        updateDateTimeDisplay()

        loadCategories()

        binding.btnPickDate.setOnClickListener { pickDate() }
        binding.btnPickStartTime.setOnClickListener { pickTime(true) }
        binding.btnPickEndTime.setOnClickListener { pickTime(false) }
        binding.btnCamera.setOnClickListener { requestCameraPermission() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener { removePhoto() }
        binding.btnSave.setOnClickListener { saveExpense() }
    }

    private fun loadCategories() {
        val db = AppDatabase.getDatabase(this)
        db.categoryDao().getAllLive().observe(this) { cats ->
            categoryList = cats
            if (cats.isEmpty()) {
                Toast.makeText(this, "Please add categories first!", Toast.LENGTH_LONG).show()
            }
            val names = cats.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            selectedDate = String.format("%d-%02d-%02d", y, m + 1, d)
            updateDateTimeDisplay()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime(isStart: Boolean) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val t = String.format("%02d:%02d", h, m)
            if (isStart) startTime = t else endTime = t
            updateDateTimeDisplay()
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun updateDateTimeDisplay() {
        binding.tvDate.text = selectedDate
        binding.tvStartTime.text = startTime
        binding.tvEndTime.text = endTime
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoPath = photoFile.absolutePath
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("EXPENSE_${timeStamp}_", ".jpg", storageDir)
    }

    private fun copyUriToFile(uri: Uri): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, "EXPENSE_${timeStamp}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    private fun showPhotoPreview(path: String) {
        binding.layoutPhoto.visibility = View.VISIBLE
        binding.ivPhotoPreview.setImageURI(Uri.fromFile(File(path)))
    }

    private fun removePhoto() {
        photoPath = null
        photoUri = null
        binding.layoutPhoto.visibility = View.GONE
    }

    private fun saveExpense() {
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()

        if (selectedDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please set date and times.", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "Description is required"
            return
        }
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter a valid positive amount"
            return
        }
        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Please add a category first!", Toast.LENGTH_LONG).show()
            return
        }

        val selectedCat = categoryList[binding.spinnerCategory.selectedItemPosition]
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            db.expenseDao().insert(
                Expense(
                    date = selectedDate,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    amount = amount,
                    categoryId = selectedCat.id,
                    categoryName = selectedCat.name,
                    photoPath = photoPath,
                    userId = session.getUserId()
                )
            )
            runOnUiThread {
                Toast.makeText(this@AddExpenseActivity, "Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
