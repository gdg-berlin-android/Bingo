package de.berlindroid.bingo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch

import java.math.BigInteger
import java.security.MessageDigest

fun <T> List<List<T>>.nestedShuffle(): List<List<T>> = this.flatten().shuffled().chunked(this.size)

/**
 * Source: https://stackoverflow.com/a/64171625/2384934
 */
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val captureController = rememberCaptureController()
            var bingoData by remember {
                mutableStateOf(
                    listOf(
                        listOf("A", "B", "C", "D", "E"),
                        listOf("B", "B", "Android", "D", "E"),
                        listOf("C", "B", "AI", "D", "E"),
                        listOf("D", "B", "C", "D", "E"),
                        listOf("E", "B", "C", "D", "E"),
                    )
                )
            }
            Column(
                modifier = Modifier.capturable(captureController)
            ) {
                Content(bingoData)
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(onClick = { bingoData = bingoData.nestedShuffle() }) {
                        Text("Randomize")
                    }
                    val scope = rememberCoroutineScope()
                    Button(onClick = {
                        scope.launch {
                            val bitmapAsync = captureController.captureAsync()
//                            try {
                            val bitmap = bitmapAsync.await()
                            val fileName = "${
                                bingoData.joinToString(";") { it.joinToString(",") }.md5()
                            }.png"
                            Log.d("MainActivity", "File name: $fileName")
                            // TODO save it
//                            } catch (error: Throwable) {
//                                // Error occurred, do something.
//                            }
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}


@Composable
fun Content(
    bingoData: List<List<String>>,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(32.dp)
    ) {
        Text("Berlindroid Google I/O 2024 Bingo Card") // TODO logo
        Spacer(modifier = Modifier.padding(16.dp))
        Bingo(
            data = bingoData,
            modifier = Modifier.aspectRatio(0.7070707f) // ISO216 A-series
        )
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
                    .wrapContentSize(align = Alignment.Center),
            )
        }
    }
}