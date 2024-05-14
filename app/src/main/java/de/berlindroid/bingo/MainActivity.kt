package de.berlindroid.bingo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest

val list = listOf(
    "hidden"
)

fun randomList(): List<List<String>> = list.shuffled().take(25).chunked(5)

/**
 * Source: https://stackoverflow.com/a/64171625/2384934
 */
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun saveBitmapToDownloads(context: Context, bitmap: Bitmap, fileName: String) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
    }

    val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        val outputStream: OutputStream? = resolver.openOutputStream(it)
        outputStream?.use { os ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
    }
}

@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val captureController = rememberCaptureController()
            var bingoData by remember { mutableStateOf(randomList()) }
            Column {
                Content(
                    bingoData = bingoData,
                    modifier = Modifier.capturable(captureController),
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Button(onClick = { bingoData = randomList() }) {
                        Text("Randomize")
                    }
                    val scope = rememberCoroutineScope()
                    Button(onClick = {
                        scope.launch {
                            val bitmapAsync = captureController.captureAsync()
                            val bitmap = bitmapAsync.await()
                            val fileName = "${
                                bingoData.joinToString(";") { it.joinToString(",") }.md5()
                            }.png"
                            saveBitmapToDownloads(this@MainActivity, bitmap.asAndroidBitmap(), fileName)
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = "",
            modifier = Modifier.height(128.dp)
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Column {
            Text("Google I/O 2024 Bingo Card", fontSize = 24.sp)
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Name: ")
                Text("                                                                        ", textDecoration = TextDecoration.Underline)
            }
        }
    }
}

@Composable
fun Content(
    bingoData: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(color = Color.White)
            .padding(32.dp)
            .aspectRatio(0.7070707f) // ISO216 A-series
    ) {
        Header()
        Spacer(modifier = Modifier.padding(16.dp))
        Bingo(data = bingoData)
    }
}

@Composable
fun Bingo(
    modifier: Modifier = Modifier,
    data: List<List<String>>,
) {
    val flatData = data.flatten()
    LazyVerticalGrid(
        columns = GridCells.Fixed(data.size),
        modifier = modifier,
    ) {
        items(flatData) { item ->
            Text(
                item,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .background(color = Color.Gray.copy(alpha = 0.2f))
                    .padding(8.dp)
                    .wrapContentSize(align = Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}