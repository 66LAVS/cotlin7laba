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

    private var dancerList = mutableStateListOf<DancerUser>()

    private val _dancerListFlow = MutableStateFlow(dancerList)
    val dancerListFlow: StateFlow<List<DancerUser>> get() = _dancerListFlow

    fun clearList() {
        dancerList.clear()
    }

    fun addDancerToEnd(dancer: DancerUser) {
        dancerList.add(dancer)
    }

    fun setDancers(dancers: List<DancerUser>) {
        dancerList.clear()
        dancerList.addAll(dancers)
    }
}

class UserActivity : ComponentActivity() {

    private val viewModel = UserViewModel()
    private val PREFS_NAME = "UserPrefs"

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
                        MakeUserAppBar(viewModel, lazyListState, dbHelper)
                    }
                }
            }
        }
    }

    private fun loadDancersFromDb(dbHelper: DancersDbHelper) {
        if (dbHelper.isEmpty()) {
            val defaultDancers = listOf(
                DancerUser(name = "Алина", surname = "Рахматулина", group = "7202", role = "Обычный танцор"),
                DancerUser(name = "Аиша", surname = "Ибрагимова", group = "9505", role = "Обычный танцор"),
                DancerUser(name = "Рамиль", surname = "Овчиева", group = "3302", role = "Обычный танцор"),
                DancerUser(name = "Руслан", surname = "Абдуризэев", group = "7777", role = "Обычный танцор"),
                DancerUser(name = "Вадим", surname = "Демиров", group = "2222", role = "Обычный танцор"),
                DancerUser(name = "Кадир", surname = "Тагиров", group = "1234", role = "Обычный танцор")
            )
            dbHelper.addArrayToDB(ArrayList(defaultDancers.map {
                Dancer(it.id, it.name, it.surname, it.group, it.role, it.picture)
            }))
            viewModel.setDancers(defaultDancers)
        } else {
            val tempDancerArray = dbHelper.getDancersArray()
            viewModel.clearList()
            tempDancerArray.forEach {
                viewModel.addDancerToEnd(DancerUser(it.id, it.name, it.surname, it.group, it.role, it.picture))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val dbHelper = DancersDbHelper(this)
        loadDancersFromDb(dbHelper)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var tempDancerArray = ArrayList<DancerUser>()
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
            }
            return dancersArray
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeUserAppBar(model: UserViewModel, lazyListState: LazyListState, dbHelper: DancersDbHelper) {
        val mContext = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }

        if (openDialog.value)
            MakeUserAlertDialog(context = mContext, dialogTitle = "Информация о танцоре", openDialog = openDialog)

        TopAppBar(
            title = { Text("Танцоры", fontSize = 20.sp) },
            actions = {
                Button(
                    onClick = {
                        val prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()

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
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MakeUserList(viewModel = model, lazyListState, dbHelper)
        }
    }

    @Composable
    fun MakeUserList(viewModel: UserViewModel, lazyListState: LazyListState, dbHelper: DancersDbHelper) {
        val dancerListState = viewModel.dancerListFlow.collectAsState()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text = dialogTitle, fontSize = 20.sp) },
            text = { Text(text = "Информация о танцоре", fontSize = 18.sp) },
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
        dbHelper: DancersDbHelper
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
                        dancerSelected.value = "${model.name} ${model.surname}\nГруппа: ${model.group}\nРоль: ${model.role}"
                        openDialog.value = true
                    },
                    onLongClick = {}
                )
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${model.name} ${model.surname}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = "Группа: ${model.group}",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.DarkGray,
                    maxLines = 1
                )
                Text(
                    text = "Роль: ${model.role}",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }

            Image(
                painter = if (pictureIsInt(model.picture)) painterResource(model.picture.toInt())
                else rememberImagePainter(model.picture),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp)
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