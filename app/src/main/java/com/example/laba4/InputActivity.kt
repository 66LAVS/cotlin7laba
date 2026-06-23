package com.example.laba4

import androidx.activity.ComponentActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laba4.ui.theme.Laba4Theme

class InputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Laba4Theme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MakeInputPart()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeInputPart() {
        var medicineName by remember { mutableStateOf("") }
        var medicinePrice by remember { mutableStateOf(0.0) }
        var medicineQuantity by remember { mutableStateOf(0) }
        var medicineManufacturer by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // First row: Name and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = medicineName,
                    onValueChange = { newText ->
                        medicineName = newText
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Medicine Name") },
                    modifier = Modifier.weight(1f)
                )

                TextField(
                    value = if (medicinePrice == 0.0) "" else medicinePrice.toString(),
                    onValueChange = { newText ->
                        medicinePrice = newText.toDoubleOrNull() ?: 0.0
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Second row: Quantity and Manufacturer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = if (medicineQuantity == 0) "" else medicineQuantity.toString(),
                    onValueChange = { newText ->
                        medicineQuantity = newText.toIntOrNull() ?: 0
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f)
                )

                TextField(
                    value = medicineManufacturer,
                    onValueChange = { newText ->
                        medicineManufacturer = newText
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Manufacturer") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Add button
            Button(
                onClick = {
                    println("added $medicineName $medicinePrice $medicineQuantity $medicineManufacturer")
                    val intent = Intent()
                    // Create a new Medicine object with the entered parameters
                    val newMedicine = Medicine(
                        name = medicineName,
                        price = medicinePrice,
                        quantity = medicineQuantity,
                        manufacturer = medicineManufacturer
                    )

                    intent.putExtra("newItem", newMedicine)
                    setResult(RESULT_OK, intent)

                    // Clear fields
                    medicineName = ""
                    medicinePrice = 0.0
                    medicineQuantity = 0
                    medicineManufacturer = ""

                    finish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Medicine")
            }
        }
    }
}