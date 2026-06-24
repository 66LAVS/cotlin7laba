package com.example.laba4

import android.app.Activity
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

data class DancerUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val surname: String,
    val group: String,
    val role: String,
    var picture: String = R.drawable.no_picture.toString()
) : Serializable

class UserViewModel : ViewModel() {

    private var dancerList = mutableStateListOf(
        DancerUser(name = "Анна", surname = "Иванова", group = "Группа А", role = "Солист", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Михаил", surname = "Петров", group = "Группа Б", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Елена", surname = "Сидорова", group = "Группа А", role = "Руководитель", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Дмитрий", surname = "Козлов", group = "Группа В", role = "Солист", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Ольга", surname = "Смирнова", group = "Группа Б", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Алексей", surname = "Федоров", group = "Группа А", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Мария", surname = "Волкова", group = "Группа В", role = "Руководитель", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Игорь", surname = "Морозов", group = "Группа Б", role = "Солист", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Наталья", surname = "Павлова", group = "Группа А", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Алина", surname = "Рахматулина", group = "7202", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Аиша", surname = "Ибрагимова", group = "9505", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Рамиль", surname = "Овчиева", group = "3302", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Руслан", surname = "Абдуризэев", group = "7777", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Вадим", surname = "Демиров", group = "2222", role = "Обычный танцор", picture = R.drawable.no_picture.toString()),
        DancerUser(name = "Кадир", surname = "Тагиров", group = "1234", role = "Обычный танцор", picture = R.drawable.no_picture.toString())
    )

    private val _dancerListFlow = MutableStateFlow(dancerList)
    val dancerListFlow: StateFlow<List<DancerUser>> get() = _dancerListFlow

    fun clearList() {
        dancerList.clear()
    }

    fun addDancerToEnd(dancer: DancerUser) {
        dancerList.add(dancer)
    }
}

class UserActivity : ComponentActivity() {

    private val viewModel = UserViewModel()
    private val PREFS_NAME = "UserPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = UserDancersDbHelper(this)

        if (savedInstanceState != null && savedInstanceState.containsKey("dancers")) {
            val tempDancerArray = savedInstanceState.getSerializable("dancers") as ArrayList<DancerUser>
            viewModel.clearList()
            tempDancerArray.forEach {
                viewModel.addDancerToEnd(it)
            }
            Toast.makeText(this, "From saved", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loading dancers...", Toast.LENGTH_SHORT).show()
            if (dbHelper.isEmpty()) {
                println("DB is empty")
                var tempDancerArray = ArrayList<DancerUser>()
                viewModel.dancerListFlow.value.forEach {
                    tempDancerArray.add(it)
                }
                dbHelper.addArrayToDB(tempDancerArray)
                dbHelper.printDB()
            } else {
                println("DB has records")
                dbHelper.printDB()
                val tempDancerArray = dbHelper.getDancersArray()
                viewModel.clearList()
                tempDancerArray.forEach {
                    viewModel.addDancerToEnd(it)
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
        var tempDancerArray = ArrayList<DancerUser>()
        viewModel.dancerListFlow.value.forEach {
            tempDancerArray.add(it)
        }
        outState.putSerializable("dancers", tempDancerArray)
        super.onSaveInstanceState(outState)
    }

    class UserDancersDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            private val DATABASE_NAME = "DANCERS_USER"
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

        fun addArrayToDB(dancers: ArrayList<DancerUser>) {
            dancers.forEach {
                addDancer(it)
            }
        }

        fun addDancer(dancer: DancerUser) {
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

        fun getDancersArray(): ArrayList<DancerUser> {
            var dancersArray = ArrayList<DancerUser>()
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
                    dancersArray.add(DancerUser(id, name, surname, group, role, picture))
                } while (cursor.moveToNext())
            } else println("DB is empty")
            return dancersArray
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeUserAppBar(model: UserViewModel, lazyListState: LazyListState, dbHelper: UserDancersDbHelper) {
        val mContext = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }

        if (openDialog.value)
            MakeUserAlertDialog(context = mContext, dialogTitle = "Dancer Details", openDialog = openDialog)

        TopAppBar(
            title = { Text("Dancers Catalog") },
            actions = {
                Button(
                    onClick = {
                        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()

                        Toast.makeText(mContext, "Logged out successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(mContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        mContext.startActivity(intent)
                        (mContext as? Activity)?.finish()
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
    fun MakeUserList(viewModel: UserViewModel, lazyListState: LazyListState, dbHelper: UserDancersDbHelper) {
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
                UserListRow(item, dancerListState.value, viewModel, dbHelper)
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
        model: DancerUser,
        dancerListState: List<DancerUser>,
        viewModel: UserViewModel,
        dbHelper: UserDancersDbHelper
    ) {
        val context = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        var dancerSelected = remember { mutableStateOf("") }

        if (openDialog.value) MakeUserAlertDialog(context, dancerSelected.value, openDialog)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Blue))
                .padding(8.dp)
                .combinedClickable(
                    onClick = {
                        println("Viewing dancer: ${model.name} ${model.surname}")
                        dancerSelected.value = "${model.name} ${model.surname}"
                        Toast.makeText(context, "Viewing: ${model.name} ${model.surname}", Toast.LENGTH_SHORT).show()
                        openDialog.value = true
                    },
                    onLongClick = {
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