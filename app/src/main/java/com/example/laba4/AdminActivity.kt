package com.example.laba4

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import coil.compose.rememberImagePainter
import com.example.laba4.ui.theme.Laba4Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.UUID

// Data class for Medicines
data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var price: Double,
    var quantity: Int,
    var manufacturer: String,
    var picture: String = R.drawable.no_picture.toString()
) : Serializable

class ItemViewModel : ViewModel() {

    private var medicineList = mutableStateListOf(
        Medicine(name = "Paracetamol", price = 150.0, quantity = 50, manufacturer = "PharmaCo", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Aspirin", price = 200.0, quantity = 30, manufacturer = "MedLife", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Ibuprofen", price = 250.0, quantity = 40, manufacturer = "HealthPlus", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Amoxicillin", price = 350.0, quantity = 20, manufacturer = "BioMed", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Omeprazole", price = 180.0, quantity = 60, manufacturer = "GastroCare", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Loratadine", price = 120.0, quantity = 45, manufacturer = "AllergyRelief", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Metformin", price = 280.0, quantity = 35, manufacturer = "DiabetesCare", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Atorvastatin", price = 400.0, quantity = 25, manufacturer = "HeartHealth", picture = R.drawable.no_picture.toString()),
        Medicine(name = "Vitamin D", price = 90.0, quantity = 100, manufacturer = "NutriLife", picture = R.drawable.no_picture.toString())
    )

    private val _medicineListFlow = MutableStateFlow(medicineList)
    val medicineListFlow: StateFlow<List<Medicine>> get() = _medicineListFlow

    fun clearList() {
        medicineList.clear()
    }

    fun changeImage(index: Int, value: String) {
        medicineList[index] = medicineList[index].copy(picture = value)
    }

    fun updateMedicine(index: Int, medicine: Medicine) {
        medicineList[index] = medicine
    }

    fun addMedicineToHead(medicine: Medicine) {
        medicineList.add(0, medicine)
    }

    fun addMedicineToEnd(medicine: Medicine) {
        medicineList.add(medicine)
    }

    fun removeItem(item: Medicine) {
        medicineList.remove(item)
    }
}

class AdminActivity : ComponentActivity() {

    private val viewModel = ItemViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = MedicinesDbHelper(this)

        if (savedInstanceState != null && savedInstanceState.containsKey("medicines")) {
            val tempMedicineArray = savedInstanceState.getSerializable("medicines") as ArrayList<Medicine>
            viewModel.clearList()
            tempMedicineArray.forEach {
                viewModel.addMedicineToEnd(it)
            }
            Toast.makeText(this, "From saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "From create", Toast.LENGTH_SHORT).show()
            if (dbHelper.isEmpty()) {
                println("DB is empty")
                var tempMedicineArray = ArrayList<Medicine>()
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
                        MakeAppBar(viewModel, lazyListState, dbHelper)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show()
        var tempMedicineArray = ArrayList<Medicine>()
        viewModel.medicineListFlow.value.forEach {
            tempMedicineArray.add(it)
        }
        outState.putSerializable("medicines", tempMedicineArray)
        super.onSaveInstanceState(outState)
    }

    class MedicinesDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            private val DATABASE_NAME = "MEDICINES"
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

        fun addArrayToDB(medicines: ArrayList<Medicine>) {
            medicines.forEach {
                addMedicine(it)
            }
        }

        fun addMedicine(medicine: Medicine) {
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

        fun updateMedicine(medicine: Medicine) {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(NAME_COL, medicine.name)
            values.put(PRICE_COL, medicine.price)
            values.put(QUANTITY_COL, medicine.quantity)
            values.put(MANUFACTURER_COL, medicine.manufacturer)
            values.put(PICTURE_COL, medicine.picture)

            db.update(TABLE_NAME, values, "$ID_COL = ?", arrayOf(medicine.id))
            db.close()
        }

        fun deleteMedicine(medicine: Medicine) {
            val db = this.writableDatabase
            db.delete(TABLE_NAME, "$ID_COL = ?", arrayOf(medicine.id))
            db.close()
        }

        fun changeImgForMedicine(name: String, img: String) {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(PICTURE_COL, img)
            db.update(TABLE_NAME, values, "$NAME_COL = ?", arrayOf(name))
            db.close()
        }

        fun getMedicinesArray(): ArrayList<Medicine> {
            var medicinesArray = ArrayList<Medicine>()
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
                    medicinesArray.add(Medicine(id, name, price, quantity, manufacturer, picture))
                } while (cursor.moveToNext())
            } else println("DB is empty")
            return medicinesArray
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeAppBar(model: ItemViewModel, lazyListState: LazyListState, dbHelper: MedicinesDbHelper) {
        var mDisplayMenu by remember { mutableStateOf(false) }
        val mContext = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val startForResult =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val newMedicine = result.data?.getSerializableExtra("newItem") as Medicine
                    println("new medicine name = ${newMedicine.name}")
                    model.addMedicineToHead(newMedicine)
                    dbHelper.addMedicine(newMedicine)
                    scope.launch {
                        lazyListState.scrollToItem(0)
                    }
                }
            }

        if (openDialog.value)
            MakeAlertDialog(context = mContext, dialogTitle = "About", openDialog = openDialog)

        TopAppBar(
            title = { Text("Medicines App") },
            actions = {
                IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                    Text("☰")
                }
                DropdownMenu(
                    expanded = mDisplayMenu,
                    onDismissRequest = { mDisplayMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "About") },
                        onClick = {
                            Toast.makeText(mContext, "About", Toast.LENGTH_SHORT).show()
                            mDisplayMenu = !mDisplayMenu
                            openDialog.value = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Add Medicine") },
                        onClick = {
                            Toast.makeText(mContext, "Add Medicine", Toast.LENGTH_SHORT).show()
                            val newAct = Intent(mContext, InputActivity::class.java)
                            startForResult.launch(newAct)
                            mDisplayMenu = !mDisplayMenu
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Logout") },
                        onClick = {
                            Toast.makeText(mContext, "Logging out...", Toast.LENGTH_SHORT).show()
                            val prefs = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            prefs.edit().clear().apply()

                            val intent = Intent(mContext, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            mContext.startActivity(intent)
                        }
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MakeList(viewModel = model, lazyListState, dbHelper)
        }
    }

    @Composable
    fun MakeList(viewModel: ItemViewModel, lazyListState: LazyListState, dbHelper: MedicinesDbHelper) {
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
                ListRow(item, medicineListState.value, viewModel, dbHelper)
            }
        }
    }

    @Composable
    fun MakeAlertDialog(context: Context, dialogTitle: String, openDialog: MutableState<Boolean>) {
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
    fun EditMedicineDialog(
        medicine: Medicine,
        index: Int,
        viewModel: ItemViewModel,
        dbHelper: MedicinesDbHelper,
        context: Context,
        onDismiss: () -> Unit
    ) {
        var name by remember { mutableStateOf(medicine.name) }
        var price by remember { mutableStateOf(medicine.price.toString()) }
        var quantity by remember { mutableStateOf(medicine.quantity.toString()) }
        var manufacturer by remember { mutableStateOf(medicine.manufacturer) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Medicine", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = { Text("Manufacturer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceDouble = price.toDoubleOrNull() ?: 0.0
                        val quantityInt = quantity.toIntOrNull() ?: 0

                        if (name.isNotEmpty() && priceDouble > 0 && quantityInt > 0 && manufacturer.isNotEmpty()) {
                            val updatedMedicine = medicine.copy(
                                name = name,
                                price = priceDouble,
                                quantity = quantityInt,
                                manufacturer = manufacturer
                            )
                            viewModel.updateMedicine(index, updatedMedicine)
                            dbHelper.updateMedicine(updatedMedicine)
                            Toast.makeText(context, "Medicine updated!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Please fill all fields correctly!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ListRow(
        model: Medicine,
        medicineListState: List<Medicine>,
        viewModel: ItemViewModel,
        dbHelper: MedicinesDbHelper
    ) {
        val context = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        val openEditDialog = remember { mutableStateOf(false) }
        var medicineSelected = remember { mutableStateOf("") }
        val index = medicineListState.indexOf(model)

        if (openDialog.value) MakeAlertDialog(context, medicineSelected.value, openDialog)

        if (openEditDialog.value) {
            EditMedicineDialog(
                medicine = model,
                index = index,
                viewModel = viewModel,
                dbHelper = dbHelper,
                context = context,
                onDismiss = { openEditDialog.value = false }
            )
        }

        var mDisplayMenu by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            if (res.data?.data != null) {
                println("image uri = ${res.data?.data}")
                val imgURI = res.data?.data!!

                context.contentResolver.takePersistableUriPermission(
                    imgURI, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val index = medicineListState.indexOf(model)
                viewModel.changeImage(index, imgURI.toString())
                dbHelper.changeImgForMedicine(model.name, imgURI.toString())
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Red))
                .combinedClickable(
                    onClick = {
                        println("item = ${model.name}")
                        medicineSelected.value = model.name
                        Toast.makeText(context, "item = ${model.name}", Toast.LENGTH_LONG).show()
                        openDialog.value = true
                    },
                    onLongClick = {
                        mDisplayMenu = true
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

                DropdownMenu(
                    expanded = mDisplayMenu,
                    onDismissRequest = { mDisplayMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Edit Medicine",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        onClick = {
                            mDisplayMenu = !mDisplayMenu
                            openEditDialog.value = true
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Change Picture",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        onClick = {
                            mDisplayMenu = !mDisplayMenu
                            val permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
                            val grant = ContextCompat.checkSelfPermission(context, permission)
                            if (grant != PackageManager.PERMISSION_GRANTED) {
                                val permission_list = arrayOfNulls<String>(1)
                                permission_list[0] = permission
                                ActivityCompat.requestPermissions(
                                    context as Activity, permission_list, 1
                                )
                            }
                            val intent = Intent(
                                Intent.ACTION_OPEN_DOCUMENT,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            ).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                            launcher.launch(intent)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete Medicine",
                                fontSize = 20.sp,
                            )
                        },
                        onClick = {
                            mDisplayMenu = !mDisplayMenu
                            viewModel.removeItem(model)
                            dbHelper.deleteMedicine(model)
                        }
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

    @Composable
    fun MakeInputPart(model: ItemViewModel, lazyListState: LazyListState) {
        var medicineName by remember { mutableStateOf("") }
        var medicinePrice by remember { mutableStateOf(0.0) }
        var medicineQuantity by remember { mutableStateOf(0) }
        var medicineManufacturer by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = medicineName,
                    onValueChange = { newText -> medicineName = newText },
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Medicine Name") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = if (medicinePrice == 0.0) "" else medicinePrice.toString(),
                    onValueChange = { newText ->
                        medicinePrice = if (newText != "") newText.toDoubleOrNull() ?: 0.0 else 0.0
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = if (medicineQuantity == 0) "" else medicineQuantity.toString(),
                    onValueChange = { newText ->
                        medicineQuantity = if (newText != "") newText.toIntOrNull() ?: 0 else 0
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = medicineManufacturer,
                    onValueChange = { newText -> medicineManufacturer = newText },
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text("Manufacturer") },
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = {
                    println("added $medicineName $medicinePrice $medicineQuantity $medicineManufacturer")
                    val newMedicine = Medicine(
                        name = medicineName,
                        price = medicinePrice,
                        quantity = medicineQuantity,
                        manufacturer = medicineManufacturer
                    )
                    model.addMedicineToHead(newMedicine)
                    scope.launch {
                        lazyListState.scrollToItem(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Medicine")
            }
        }
    }
}