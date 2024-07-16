package com.example.composetextrecognizer

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.composetextrecognizer.ui.theme.ComposeTextRecognizerTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTextRecognizerTheme {
              AppContent()
            }
        }
    }
}


@Composable
fun TextRecognization() {
    val textRecognizer: TextRecognizer
    val context = LocalContext.current
    val imageUri: Uri? = null

    textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
        val inputImage = InputImage.fromFilePath(context, imageUri!!)
        val testTaskResult = textRecognizer.process(inputImage)
            .addOnSuccessListener {
                val recognizeText = it.text
            }
            .addOnFailureListener{
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
    } catch (e:Exception){
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
    }
}





@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContent() {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )

    // Use remember to handle state properly in Compose
    var recognizeText by remember { mutableStateOf("") }
    val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        capturedImageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Compose Text Recognizer",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.black)
                ),
                border = BorderStroke(color = colorResource(id = R.color.white), width = 2.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                onClick = {
                val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(uri)
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }) {
                Text(text = "Take Photo")
                Icon(
                    painter = painterResource(id = R.drawable.add_photo),
                    contentDescription = "",
                    modifier = Modifier.size(25.dp)
                )
            }

            OutlinedButton(
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.black)
                ),
                border = BorderStroke(color = colorResource(id = R.color.white), width = 2.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                onClick = {
                try {
                    val inputImage = InputImage.fromFilePath(context, capturedImageUri)
                    textRecognizer.process(inputImage)
                        .addOnSuccessListener {
                            recognizeText = it.text
                            Log.d("YOMO:", "$recognizeText")
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Recognize Text")
                Icon(
                    painter = painterResource(id = R.drawable.document_scanner),
                    contentDescription = "",
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        if (capturedImageUri.path?.isNotEmpty() != true) {
            Image(
                modifier = Modifier
                    .padding(16.dp, 8.dp)
                    .size(300.dp)
                ,
                painter = rememberImagePainter(R.drawable.basic_image),
                contentDescription = null
            )
        }
        if (capturedImageUri.path?.isNotEmpty() == true) {
            Image(
                modifier = Modifier
                    .padding(8.dp)
                    .size(300.dp)
                    .border(width = 2.dp, color = Color.Black),
                contentScale = ContentScale.FillWidth,
                painter = rememberImagePainter(capturedImageUri),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(15.dp))
        Divider(thickness = 1.dp, color = Color.Gray)
        Text(
            text = "Recognized Text:",
            fontSize = 20.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge
        )
        SelectionContainer {
            Text(text = recognizeText, fontSize = 12.sp,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                lineHeight = 10.sp,
                modifier = Modifier.fillMaxWidth()
                )
        }
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}