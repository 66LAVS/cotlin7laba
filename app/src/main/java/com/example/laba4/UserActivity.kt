package com.example.laba4

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import coil.compose.rememberImagePainter
import com.example.laba4.ui.theme.Laba4Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable
import java.util.UUID

// Data class for Medicines (same as before)
data class MedicineUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val quantity: Int,
    val manufacturer: String,
    var picture: String = R.drawable.no_picture.toString()
) : Serializable

class UserViewModel : ViewModel() {

    private var medicineList = mutableStateListOf(
        MedicineUser(name = "Paracetamol", price = 150.0, quantity = 50, manufacturer = "PharmaCo", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Aspirin", price = 200.0, quantity = 30, manufacturer = "MedLife", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Ibuprofen", price = 250.0, quantity = 40, manufacturer = "HealthPlus", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Amoxicillin", price = 350.0, quantity = 20, manufacturer = "BioMed", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Omeprazole", price = 180.0, quantity = 60, manufacturer = "GastroCare", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Loratadine", price = 120.0, quantity = 45, manufacturer = "AllergyRelief", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Metformin", price = 280.0, quantity = 35, manufacturer = "DiabetesCare", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Atorvastatin", price = 400.0, quantity = 25, manufacturer = "HeartHealth", picture = R.drawable.no_picture.toString()),
        MedicineUser(name = "Vitamin D", price = 90.0, quantity = 100, manufacturer = "NutriLife", picture = R.drawable.no_picture.toString())
    )

    private val _medicineListFlow = MutableStateFlow(medicineList)
    val medicineListFlow: StateFlow<List<MedicineUser>> get() = _medicineListFlow

    fun clearList() {
        medicineList.clear()
    }

    fun addMedicineToEnd(medicine: MedicineUser) {
        medicineList.add(medicine)
    }
}

class UserActivity : ComponentActivity() {

    private val viewModel = UserViewModel()
    private val PREFS_NAME = "UserPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = UserMedicinesDbHelper(this)

        if (savedInstanceState != null && savedInstanceState.containsKey("medicines")) {
            val tempMedicineArray = savedInstanceState.getSerializable("medicines") as ArrayList<MedicineUser>
            viewModel.clearList()
            tempMedicineArray.forEach {
                viewModel.addMedicineToEnd(it)
            }
            Toast.makeText(this, "From saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loading medicines...", Toast.LENGTH_SHORT).show()
            if (dbHelper.isEmpty()) {
                println("DB is empty")
                var tempMedicineArray = ArrayList<MedicineUser>()
                viewModel.medicineListFlow.value.forEach {
                    tempMedicineArray.add(it)
                }
                dbHelper.addArrayToDB(tempMedicineArray)
                dbHelper.printDB()
            } else {
                println("DB has records")
                dbHelper.printDB()
                val tempMedicineArray = dbHelper.getMedicinesArray()
                viewModel.clearList()
                tempMedicineArray.forEach {
                    viewModel.addMedicineToEnd(it)
                }
            }
        }

        setContent {
            val lazyListState = rememberLazyListState()
            Laba4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) {
                        MakeUserAppBar(viewModel, lazyListState, dbHelper)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var tempMedicineArray = ArrayList<MedicineUser>()
        viewModel.medicineListFlow.value.forEach {
            tempMedicineArray.add(it)
        }
        outState.putSerializable("medicines", tempMedicineArray)
        super.onSaveInstanceState(outState)
    }

    class UserMedicinesDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            private val DATABASE_NAME = "MEDICINES_USER"
            private val DATABASE_VERSION = 1
            val TABLE_NAME = "medicines_table"
            val ID_COL = "id"
            val NAME_COL = "medicine_name"
            val PRICE_COL = "price"
            val QUANTITY_COL = "quantity"
            val MANUFACTURER_COL = "manufacturer"
            val PICTURE_COL = "picture"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val query = ("CREATE TABLE " + TABLE_NAME + " (" +
                    ID_COL + " TEXT PRIMARY KEY, " +
                    NAME_COL + " TEXT, " +
                    PRICE_COL + " REAL, " +
                    QUANTITY_COL + " INTEGER, " +
                    MANUFACTURER_COL + " TEXT, " +
                    PICTURE_COL + " TEXT)")
            db.execSQL(query)
        }

        override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
            onCreate(db)
        }

        fun getCursor(): Cursor {
            val db = this.readableDatabase
            return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)
        }

        fun isEmpty(): Boolean {
            val cursor = getCursor()
            return !cursor.moveToFirst()
        }

        fun printDB() {
            val cursor = getCursor()
            if (!isEmpty()) {
                cursor.moveToFirst()
                val nameColIndex = cursor.getColumnIndex(NAME_COL)
                val priceColIndex = cursor.getColumnIndex(PRICE_COL)
                do {
                    print("${cursor.getString(nameColIndex)} ")
                    print("${cursor.getDouble(priceColIndex)} ")
                } while (cursor.moveToNext())
            } else println("DB is empty")
        }

        fun addArrayToDB(medicines: ArrayList<MedicineUser>) {
            medicines.forEach {
                addMedicine(it)
            }
        }

        fun addMedicine(medicine: MedicineUser) {
            val values = ContentValues()
            values.put(ID_COL, medicine.id)
            values.put(NAME_COL, medicine.name)
            values.put(PRICE_COL, medicine.price)
            values.put(QUANTITY_COL, medicine.quantity)
            values.put(MANUFACTURER_COL, medicine.manufacturer)
            values.put(PICTURE_COL, medicine.picture)

            val db = this.writableDatabase
            db.insert(TABLE_NAME, null, values)
            db.close()
        }

        fun getMedicinesArray(): ArrayList<MedicineUser> {
            var medicinesArray = ArrayList<MedicineUser>()
            val cursor = getCursor()
            if (!isEmpty()) {
                cursor.moveToFirst()
                val idColIndex = cursor.getColumnIndex(ID_COL)
                val nameColIndex = cursor.getColumnIndex(NAME_COL)
                val priceColIndex = cursor.getColumnIndex(PRICE_COL)
                val quantityColIndex = cursor.getColumnIndex(QUANTITY_COL)
                val manufacturerColIndex = cursor.getColumnIndex(MANUFACTURER_COL)
                val pictureColIndex = cursor.getColumnIndex(PICTURE_COL)

                do {
                    val id = cursor.getString(idColIndex)
                    val name = cursor.getString(nameColIndex)
                    val price = cursor.getDouble(priceColIndex)
                    val quantity = cursor.getInt(quantityColIndex)
                    val manufacturer = cursor.getString(manufacturerColIndex)
                    val picture = cursor.getString(pictureColIndex)
                    medicinesArray.add(MedicineUser(id, name, price, quantity, manufacturer, picture))
                } while (cursor.moveToNext())
            } else println("DB is empty")
            return medicinesArray
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeUserAppBar(model: UserViewModel, lazyListState: LazyListState, dbHelper: UserMedicinesDbHelper) {
        val mContext = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }

        if (openDialog.value)
            MakeUserAlertDialog(context = mContext, dialogTitle = "Medicine Details", openDialog = openDialog)

        // User-friendly TopAppBar with logout button
        TopAppBar(
            title = { Text("Medicines Catalog") },
            actions = {
                // Кнопка выхода
                Button(
                    onClick = {
                        // Очищаем SharedPreferences
                        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()

                        // Показываем сообщение
                        Toast.makeText(mContext, "Logged out successfully", Toast.LENGTH_SHORT).show()

                        // Переходим на экран логина
                        val intent = Intent(mContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        mContext.startActivity(intent)

                        // Закрываем текущую активность
                        (mContext as? android.app.Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Выйти", fontSize = 14.sp)
                }
            }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MakeUserList(viewModel = model, lazyListState, dbHelper)
        }
    }

    @Composable
    fun MakeUserList(viewModel: UserViewModel, lazyListState: LazyListState, dbHelper: UserMedicinesDbHelper) {
        val medicineListState = viewModel.medicineListFlow.collectAsState()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(Color.White),
            state = lazyListState
        ) {
            items(
                items = viewModel.medicineListFlow.value,
                key = { it.id }
            ) { item ->
                UserListRow(item, medicineListState.value, viewModel, dbHelper)
            }
        }
    }

    @Composable
    fun MakeUserAlertDialog(context: Context, dialogTitle: String, openDialog: MutableState<Boolean>) {
        var strValue = remember { mutableStateOf("") }
        val strId = context.resources.getIdentifier(dialogTitle, "string", context.packageName)
        try {
            if (strId != 0) strValue.value = context.getString(strId)
        } catch (e: Resources.NotFoundException) {
            // Handle missing resource
        }
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text = dialogTitle) },
            text = { Text(text = strValue.value, fontSize = 20.sp) },
            confirmButton = {
                Button(onClick = { openDialog.value = false }) { Text(text = "OK") }
            }
        )
    }

    @Composable
    fun UserListRow(
        model: MedicineUser,
        medicineListState: List<MedicineUser>,
        viewModel: UserViewModel,
        dbHelper: UserMedicinesDbHelper
    ) {
        val context = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        var medicineSelected = remember { mutableStateOf("") }

        if (openDialog.value) MakeUserAlertDialog(context, medicineSelected.value, openDialog)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Blue)) // Blue border for user view
                .padding(8.dp)
                .combinedClickable(
                    onClick = {
                        println("Viewing item: ${model.name}")
                        medicineSelected.value = model.name
                        Toast.makeText(context, "Viewing: ${model.name}", Toast.LENGTH_SHORT).show()
                        openDialog.value = true
                    },
                    onLongClick = {
                        // No action on long click - view only
                        Toast.makeText(context, "View only mode", Toast.LENGTH_SHORT).show()
                    }
                )
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = model.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 18.dp),
                        color = Color.Black
                    )
                    Text(
                        text = "Price: $${model.price}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = FontStyle.Italic,
                        color = Color.Black
                    )
                }
                Column {
                    Text(
                        text = "Qty: ${model.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = FontStyle.Italic,
                        color = Color.Black
                    )
                    Text(
                        text = model.manufacturer,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = FontStyle.Italic,
                        color = Color.Black
                    )
                }
            }
            Image(
                painter = if (pictureIsInt(model.picture)) painterResource(model.picture.toInt())
                else rememberImagePainter(model.picture),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(75.dp)
            )
        }
    }

    fun pictureIsInt(picture: String): Boolean {
        var data = try {
            picture.toInt()
        } catch (e: NumberFormatException) {
            null
        }
        return data != null
    }
}