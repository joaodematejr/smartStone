package com.demate.homostone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.demate.homostone.ui.theme.HomoStoneTheme
import com.demate.homostone.viewmodel.ViewModelStone

class MainActivity : ComponentActivity() {
    private val viewModelStone: ViewModelStone by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomoStoneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent("João", viewModelStone = viewModelStone)
                }
            }
        }
    }
}

@Composable
fun AppContent(name: String, modifier: Modifier = Modifier, viewModelStone: ViewModelStone) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Hello $name!")
        Button(onClick = {
            viewModelStone.handleInit(context)
        }) {
            Text(text = "HandleInit")
        }
        Button(onClick = {
            viewModelStone.handlePrint(context)
        }) {
            Text(text = "HandlePrint")
        }
        Button(onClick = {
            viewModelStone.handleInformation(context)
        }) {
            Text(text = "Get Informação")
        }
        Button(onClick = {
            viewModelStone.handlePayment(context)
        }) {
            Text(text = "Payment")
        }
        Button(onClick = {
            viewModelStone.getListTransaction(context)
        }) {
            Text(text = "Get List transaction")
        }
        Button(onClick = {
            viewModelStone.getIdTransaction(context)
        }) {
            Text(text = "Get Id transaction")
        }
        Button(onClick = {
            viewModelStone.cancelPayment(context)
        }) {
            Text(text = "Cancel transaction")
        }
        Button(onClick = {
            viewModelStone.abortPayment(context)
        }) {
            Text(text = "About transaction")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HomoStoneTheme {
        AppContent("Android", viewModelStone = ViewModelStone())
    }
}