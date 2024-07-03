@file:Suppress("UNUSED_VARIABLE", "DEPRECATION", "PrivatePropertyName")

package com.example.barcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_PICK = 100
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de Firestore
        db = FirebaseFirestore.getInstance()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .build()

        val code = findViewById<Button>(R.id.selectCode)
        code.setOnClickListener {
            abrirGaleria()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                val imageUri = data?.data
                imageUri?.let {
                    val image = InputImage.fromFilePath(applicationContext, it)
                    scanBarcodes(image)
                }
            } else {
                Log.d(TAG, "Selección de imagen cancelada por el usuario")
            }
        }
    }

    private fun scanBarcodes(image: InputImage) {
        val scanner = BarcodeScanning.getClient()
        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue ?: ""

                    // Construir el HashMap con los datos del código de barras
                    val documentData = hashMapOf(
                        "rawValue" to rawValue,
                    )

                    // Guardar en Firestore
                    db.collection("barcodes")
                        .add(documentData)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "Documento guardado con ID: ${documentReference.id}")
                            Toast.makeText(this, "Código de barras guardado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al guardar documento", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al escanear códigos de barras", e)
            }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}






