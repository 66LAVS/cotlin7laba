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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.laba4.Dancer
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
        var dancerName by remember {
            mutableStateOf("")
        }
        var dancerSurname by remember {
            mutableStateOf("")
        }
        var dancerGroup by remember {
            mutableStateOf("")
        }
        var dancerRole by remember {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = dancerName,
                    onValueChange = { newText ->
                        dancerName = newText
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    label = { Text("Имя") },
                    modifier = Modifier.weight(2f)
                )

                TextField(
                    value = dancerSurname,
                    onValueChange = { newText ->
                        dancerSurname = newText
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    label = { Text("Фамилия") },
                    modifier = Modifier.weight(2f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = dancerGroup,
                    onValueChange = { newText ->
                        dancerGroup = newText
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    label = { Text("Группа") },
                    modifier = Modifier.weight(2f)
                )

                TextField(
                    value = dancerRole,
                    onValueChange = { newText ->
                        dancerRole = newText
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    label = { Text("Роль (солист/обычный/руководитель)") },
                    modifier = Modifier.weight(2f)
                )

                Button(
                    onClick = {
                        println("added $dancerName $dancerSurname $dancerGroup $dancerRole")
                        val intent = Intent()
                        // Исправлено: передаем все 4 параметра в правильном порядке
                        val newDancer = Dancer(
                            name = dancerName,
                            surname = dancerSurname,
                            group = dancerGroup,
                            role = dancerRole
                        )

                        intent.putExtra("newItem", newDancer)
                        setResult(RESULT_OK, intent)
                        dancerName = ""
                        dancerSurname = ""
                        dancerGroup = ""
                        dancerRole = ""
                        finish()
                    },
                    modifier = Modifier.weight(3f)
                ) {
                    Text("Add")
                }
            }
        }
    }
}