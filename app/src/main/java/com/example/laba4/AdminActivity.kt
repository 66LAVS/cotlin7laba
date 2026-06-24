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

data class Dancer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val surname: String,
    val group: String,
    val role: String,
    var picture: String = R.drawable.no_picture.toString()
) : Serializable

class ItemViewModel : ViewModel() {

    private var dancerList = mutableStateListOf<Dancer>()

    private val _dancerListFlow = MutableStateFlow(dancerList)
    val dancerListFlow: StateFlow<List<Dancer>> get() = _dancerListFlow

    fun clearList() {
        dancerList.clear()
    }

    fun setDancers(dancers: List<Dancer>) {
        dancerList.clear()
        dancerList.addAll(dancers)
    }

    fun changeImage(index: Int, value: String) {
        dancerList[index] = dancerList[index].copy(picture = value)
    }

    fun updateDancer(index: Int, name: String, surname: String, group: String, role: String) {
        dancerList[index] = dancerList[index].copy(
            name = name,
            surname = surname,
            group = group,
            role = role
        )
    }

    fun addDancerToHead(dancer: Dancer) {
        dancerList.add(0, dancer)
    }

    fun addDancerToEnd(dancer: Dancer) {
        dancerList.add(dancer)
    }

    fun removeItem(item: Dancer) {
        dancerList.remove(item)
    }
}

class AdminActivity : ComponentActivity() {

    private val viewModel = ItemViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DancersDbHelper(this)

        loadDancersFromDb(dbHelper)

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

    private fun loadDancersFromDb(dbHelper: DancersDbHelper) {
        if (dbHelper.isEmpty()) {
            val defaultDancers = listOf(
                Dancer(name = "Анна", surname = "Иванова", group = "Группа А", role = "Солист"),
                Dancer(name = "Михаил", surname = "Петров", group = "Группа Б", role = "Обычный танцор"),
                Dancer(name = "Елена", surname = "Сидорова", group = "Группа А", role = "Руководитель"),
                Dancer(name = "Дмитрий", surname = "Козлов", group = "Группа В", role = "Солист"),
                Dancer(name = "Ольга", surname = "Смирнова", group = "Группа Б", role = "Обычный танцор"),
                Dancer(name = "Алексей", surname = "Федоров", group = "Группа А", role = "Обычный танцор"),
                Dancer(name = "Мария", surname = "Волкова", group = "Группа В", role = "Руководитель"),
                Dancer(name = "Игорь", surname = "Морозов", group = "Группа Б", role = "Солист"),
                Dancer(name = "Наталья", surname = "Павлова", group = "Группа А", role = "Обычный танцор"),
                Dancer(name = "Алина", surname = "Рахматулина", group = "7202", role = "Обычный танцор"),
                Dancer(name = "Аиша", surname = "Ибрагимова", group = "9505", role = "Обычный танцор"),
                Dancer(name = "Рамиль", surname = "Овчиева", group = "3302", role = "Обычный танцор"),
                Dancer(name = "Руслан", surname = "Абдуризэев", group = "7777", role = "Обычный танцор"),
                Dancer(name = "Вадим", surname = "Демиров", group = "2222", role = "Обычный танцор"),
                Dancer(name = "Кадир", surname = "Тагиров", group = "1234", role = "Обычный танцор")
            )
            dbHelper.addArrayToDB(ArrayList(defaultDancers))
            viewModel.setDancers(defaultDancers)
        } else {
            val tempDancerArray = dbHelper.getDancersArray()
            viewModel.setDancers(tempDancerArray)
        }
    }

    private fun refreshDancersFromDb(dbHelper: DancersDbHelper) {
        val tempDancerArray = dbHelper.getDancersArray()
        viewModel.setDancers(tempDancerArray)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var tempDancerArray = ArrayList<Dancer>()
        viewModel.dancerListFlow.value.forEach {
            tempDancerArray.add(it)
        }
        outState.putSerializable("dancers", tempDancerArray)
        super.onSaveInstanceState(outState)
    }

    class DancersDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            private val DATABASE_NAME = "DANCERS"
            private val DATABASE_VERSION = 1
            val TABLE_NAME = "dancers_table"
            val ID_COL = "id"
            val NAME_COL = "dancer_name"
            val SURNAME_COL = "dancer_surname"
            val GROUP_COL = "dancer_group"
            val ROLE_COL = "dancer_role"
            val PICTURE_COL = "picture"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val query = ("CREATE TABLE " + TABLE_NAME + " (" +
                    ID_COL + " TEXT PRIMARY KEY, " +
                    NAME_COL + " TEXT, " +
                    SURNAME_COL + " TEXT, " +
                    GROUP_COL + " TEXT, " +
                    ROLE_COL + " TEXT, " +
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
                val surnameColIndex = cursor.getColumnIndex(SURNAME_COL)
                do {
                    print("${cursor.getString(nameColIndex)} ")
                    print("${cursor.getString(surnameColIndex)} ")
                } while (cursor.moveToNext())
            } else println("DB is empty")
        }

        fun addArrayToDB(dancers: ArrayList<Dancer>) {
            dancers.forEach {
                addDancer(it)
            }
        }

        fun addDancer(dancer: Dancer) {
            val values = ContentValues()
            values.put(ID_COL, dancer.id)
            values.put(NAME_COL, dancer.name)
            values.put(SURNAME_COL, dancer.surname)
            values.put(GROUP_COL, dancer.group)
            values.put(ROLE_COL, dancer.role)
            values.put(PICTURE_COL, dancer.picture)

            val db = this.writableDatabase
            db.insert(TABLE_NAME, null, values)
            db.close()
        }

        fun deleteDancer(dancer: Dancer) {
            val db = this.writableDatabase
            db.delete(TABLE_NAME, "$ID_COL = ?", arrayOf(dancer.id))
            db.close()
        }

        fun updateDancer(dancer: Dancer) {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(NAME_COL, dancer.name)
            values.put(SURNAME_COL, dancer.surname)
            values.put(GROUP_COL, dancer.group)
            values.put(ROLE_COL, dancer.role)
            values.put(PICTURE_COL, dancer.picture)

            db.update(TABLE_NAME, values, "$ID_COL = ?", arrayOf(dancer.id))
            db.close()
        }

        fun changeImgForDancer(name: String, surname: String, img: String) {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(PICTURE_COL, img)
            db.update(TABLE_NAME, values, "$NAME_COL = ? AND $SURNAME_COL = ?", arrayOf(name, surname))
            db.close()
        }

        fun getDancersArray(): ArrayList<Dancer> {
            var dancersArray = ArrayList<Dancer>()
            val cursor = getCursor()
            if (!isEmpty()) {
                cursor.moveToFirst()
                val idColIndex = cursor.getColumnIndex(ID_COL)
                val nameColIndex = cursor.getColumnIndex(NAME_COL)
                val surnameColIndex = cursor.getColumnIndex(SURNAME_COL)
                val groupColIndex = cursor.getColumnIndex(GROUP_COL)
                val roleColIndex = cursor.getColumnIndex(ROLE_COL)
                val pictureColIndex = cursor.getColumnIndex(PICTURE_COL)

                do {
                    val id = cursor.getString(idColIndex)
                    val name = cursor.getString(nameColIndex)
                    val surname = cursor.getString(surnameColIndex)
                    val group = cursor.getString(groupColIndex)
                    val role = cursor.getString(roleColIndex)
                    val picture = cursor.getString(pictureColIndex)
                    dancersArray.add(Dancer(id, name, surname, group, role, picture))
                } while (cursor.moveToNext())
            } else println("DB is empty")
            return dancersArray
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeAppBar(model: ItemViewModel, lazyListState: LazyListState, dbHelper: DancersDbHelper) {
        var mDisplayMenu by remember { mutableStateOf(false) }
        val mContext = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val startForResult =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val newDancer = result.data?.getSerializableExtra("newItem") as Dancer
                    model.addDancerToHead(newDancer)
                    dbHelper.addDancer(newDancer)
                    scope.launch {
                        lazyListState.scrollToItem(0)
                    }
                    refreshDancersFromDb(dbHelper)
                }
            }

        if (openDialog.value)
            MakeAlertDialog(context = mContext, dialogTitle = "About", openDialog = openDialog)

        TopAppBar(
            title = { Text("Админ: Танцоры") },
            actions = {
                IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                    Text("☰")
                }
                DropdownMenu(
                    expanded = mDisplayMenu,
                    onDismissRequest = { mDisplayMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "О нас") },
                        onClick = {
                            mDisplayMenu = !mDisplayMenu
                            openDialog.value = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Добавить танцора") },
                        onClick = {
                            val newAct = Intent(mContext, InputActivity::class.java)
                            startForResult.launch(newAct)
                            mDisplayMenu = !mDisplayMenu
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Выйти") },
                        onClick = {
                            val prefs = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            prefs.edit().clear().apply()

                            val intent = Intent(mContext, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            mContext.startActivity(intent)
                            (mContext as? Activity)?.finish()
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
    fun MakeList(viewModel: ItemViewModel, lazyListState: LazyListState, dbHelper: DancersDbHelper) {
        val dancerListState = viewModel.dancerListFlow.collectAsState()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(Color.White),
            state = lazyListState
        ) {
            items(
                items = viewModel.dancerListFlow.value,
                key = { it.id }
            ) { item ->
                ListRow(item, dancerListState.value, viewModel, dbHelper)
            }
        }
    }

    @Composable
    fun MakeAlertDialog(context: Context, dialogTitle: String, openDialog: MutableState<Boolean>) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text = dialogTitle) },
            text = { Text(text = "Приложение для управления танцорами", fontSize = 20.sp) },
            confirmButton = {
                Button(onClick = { openDialog.value = false }) { Text(text = "OK") }
            }
        )
    }

    @Composable
    fun EditDancerDialog(
        context: Context,
        dancer: Dancer,
        onDismiss: () -> Unit,
        onSave: (String, String, String, String) -> Unit
    ) {
        var name by remember { mutableStateOf(dancer.name) }
        var surname by remember { mutableStateOf(dancer.surname) }
        var group by remember { mutableStateOf(dancer.group) }
        var role by remember { mutableStateOf(dancer.role) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Редактировать танцора", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                    TextField(
                        value = surname,
                        onValueChange = { surname = it },
                        label = { Text("Фамилия") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                    TextField(
                        value = group,
                        onValueChange = { group = it },
                        label = { Text("Группа") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                    TextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Роль") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotEmpty() && surname.isNotEmpty() && group.isNotEmpty() && role.isNotEmpty()) {
                            onSave(name, surname, group, role)
                        } else {
                            Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ListRow(
        model: Dancer,
        dancerListState: List<Dancer>,
        viewModel: ItemViewModel,
        dbHelper: DancersDbHelper
    ) {
        val context = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        var dancerSelected = remember { mutableStateOf("") }

        var showEditDialog by remember { mutableStateOf(false) }

        if (openDialog.value) MakeAlertDialog(context, dancerSelected.value, openDialog)

        var mDisplayMenu by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            if (res.data?.data != null) {
                val imgURI = res.data?.data!!

                context.contentResolver.takePersistableUriPermission(
                    imgURI, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val index = dancerListState.indexOf(model)
                viewModel.changeImage(index, imgURI.toString())
                dbHelper.changeImgForDancer(model.name, model.surname, imgURI.toString())
                refreshDancersFromDb(dbHelper)
            }
        }

        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Red))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            dancerSelected.value = "${model.name} ${model.surname}"
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
                            text = "${model.name} ${model.surname}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 18.dp),
                            color = Color.Black
                        )
                        Text(
                            text = "Группа: ${model.group}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(10.dp),
                            fontStyle = FontStyle.Italic,
                            color = Color.Black
                        )
                    }
                    Column {
                        Text(
                            text = "Роль: ${model.role}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
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

            DropdownMenu(
                expanded = mDisplayMenu,
                onDismissRequest = { mDisplayMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("✏️ Редактировать", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        mDisplayMenu = !mDisplayMenu
                        showEditDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("🖼️ Сменить фото", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
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
                    text = { Text("🗑️ Удалить", fontSize = 20.sp) },
                    onClick = {
                        mDisplayMenu = !mDisplayMenu
                        viewModel.removeItem(model)
                        dbHelper.deleteDancer(model)
                        refreshDancersFromDb(dbHelper)
                    }
                )
            }

            if (showEditDialog) {
                EditDancerDialog(
                    context = context,
                    dancer = model,
                    onDismiss = { showEditDialog = false },
                    onSave = { name, surname, group, role ->
                        val index = dancerListState.indexOf(model)
                        viewModel.updateDancer(index, name, surname, group, role)
                        val updatedDancer = dancerListState[index]
                        dbHelper.updateDancer(updatedDancer)
                        showEditDialog = false
                        Toast.makeText(context, "Танцор обновлен!", Toast.LENGTH_SHORT).show()
                        refreshDancersFromDb(dbHelper)
                    }
                )
            }
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
        var dancerName by remember { mutableStateOf("") }
        var dancerSurname by remember { mutableStateOf("") }
        var dancerGroup by remember { mutableStateOf("") }
        var dancerRole by remember { mutableStateOf("") }

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
                    value = dancerName,
                    onValueChange = { newText -> dancerName = newText },
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Имя") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = dancerSurname,
                    onValueChange = { newText -> dancerSurname = newText },
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Фамилия") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = dancerGroup,
                    onValueChange = { newText -> dancerGroup = newText },
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Группа") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = dancerRole,
                    onValueChange = { newText -> dancerRole = newText },
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Роль") },
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = {
                    val newDancer = Dancer(
                        name = dancerName,
                        surname = dancerSurname,
                        group = dancerGroup,
                        role = dancerRole
                    )
                    model.addDancerToHead(newDancer)
                    scope.launch {
                        lazyListState.scrollToItem(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Dancer")
            }
        }
    }
}