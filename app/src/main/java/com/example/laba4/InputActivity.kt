package com.example.laba4

import androidx.activity.ComponentActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardType
import com.example.laba4.Car
import com.example.laba4.ItemViewModel
import com.example.laba4.ui.theme.Laba4Theme
import kotlinx.coroutines.launch

//cоздаем интерфейс
class InputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Laba4Theme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MakeInputPart()//наша функция по созданию интерфейса для ввода нового языка
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeInputPart() {
        var langName by remember {
            mutableStateOf("")
        }
        var langYear by remember {
            mutableStateOf(0)
        }
        var liter by remember {
            mutableStateOf(0.0)
        }
        var number by remember {
            mutableStateOf("")
        }

        val scope = rememberCoroutineScope()

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
                    value = langName,
                    onValueChange = { newText ->
                        langName = newText
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    label = { Text("Название") },
                    modifier = Modifier.weight(2f)
                )

                TextField(
                    value = langYear.toString(),
                    onValueChange = { newText ->
                        langYear = newText.toIntOrNull() ?: 0
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Год создания") },
                    modifier = Modifier.weight(2f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = liter.toString(),
                    onValueChange = { newText ->
                        liter = if (newText != "") newText.toDouble() else 0.0
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Движок") },
                    modifier = Modifier.weight(2f)
                )

                TextField(
                    value = number.toString(),
                    onValueChange = { newText ->
                        number = if (newText != "") newText.toString() else ""
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Номер") },
                    modifier = Modifier.weight(2f)
                )

                Button(
                    onClick = {
                        println("added $langName $langYear $liter $number")
                        val intent = Intent()
                        //создаем новый язык с введенными параметрами
                        val newLang = Car(langName, langYear,liter,number)

                        intent.putExtra("newItem", newLang)
                        //вставляем намерение в результат текущего окна
                        setResult(RESULT_OK, intent);
                        langName = ""
                        langYear = 0
                        liter = 0.0
                        number = ""
                        finish()  //и закрываем текущее окно

                    },
                    modifier = Modifier.weight(3f)
                ) {
                    Text("Add")
                }
        }
    }}}
