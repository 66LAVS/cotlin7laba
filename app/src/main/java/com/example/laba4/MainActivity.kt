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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas

import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import androidx.compose.ui.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import coil.compose.rememberImagePainter
import com.example.laba4.ui.theme.Laba4Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.Serializable


//создаем дата-класс для представления языка программирования
data class Car(
    val name: String,
    val year: Int,
    val liter: Double,
    val number: String,
    var picture: String = R.drawable.no_picture.toString()
) : Serializable

class ItemViewModel : ViewModel() {

    private var langList = mutableStateListOf(
        Car("Priora", 2010, 1.6, "А123ВС777", R.drawable.priora.toString()),
        Car("Ford", 2015, 2.0, "У456ОР123", R.drawable.ford.toString()),
        Car("BMW", 2020, 3.0, "О777ТТ197", R.drawable.bmw.toString()),
        Car("Audi", 2018, 2.0, "Е321КХ750", R.drawable.audi.toString()),
        Car("Honda", 2012, 1.8, "М555АВ198", R.drawable.honda.toString()),
        Car("Mercedes", 2022, 3.5, "В999СТ178", R.drawable.merc.toString()),
        Car("Hyundai", 2019, 2.4, "С258ОК777"),
        Car("Kia", 2021, 1.6, "Н333СА750"),
        Car("Lada", 2017, 1.6, "Х123ХХ123")
    )

    //добавляем объект, который будет отвечать за изменения в созданном списке
    private val _langListFlow = MutableStateFlow(langList)

    //и геттер для него, который его возвращает
    val langListFlow: StateFlow<List<Car>> get() = _langListFlow

    fun clearList() { //метод для очистки списка, понадобится в лаб.раб.№5
        langList.clear()
    }

    fun changeImage(index: Int, value: String) {
        langList[index] = langList[index].copy(picture = value)
    }

    fun addLangToHead(lang: Car) { //метод для добавления нового языка в начало списка
        langList.add(0, lang)
    }

    fun addLangToEnd(lang: Car) { //метод для добавления нового языка в конец списка
        langList.add(lang)
    }


    fun removeItem(item: Car) { //метод для удаления элемента из списка
        val index = langList.indexOf(item)
        langList.remove(langList[index])
    }

}


class MainActivity : ComponentActivity() {

    private val viewModel = ItemViewModel() //модель данных нашего списка

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = LangsDbHelper(this)  //создаем объект класса LangsDbHelper
        if (savedInstanceState != null && savedInstanceState.containsKey("langs")) {
            val tempLangArray = savedInstanceState.getSerializable("langs") as ArrayList<Car>
            viewModel.clearList()
            tempLangArray.forEach {
                viewModel.addLangToEnd(it)
            }
            Toast.makeText(this, "From saved", Toast.LENGTH_SHORT).show()
        } else {
            // Первый запуск или после полного закрытия
            Toast.makeText(this, "From create", Toast.LENGTH_SHORT).show()
            if (dbHelper!!.isEmpty()) {  //если БД пустая
                println("DB is emty")
                var tempLangArray = ArrayList<Car>()//временный ArrayList для сохранения данных
                viewModel.langListFlow.value.forEach {//переносим данные из нашего основного массива
                    tempLangArray.add(it)
                }
                dbHelper!!.addArrayToDB(tempLangArray) //заносим в БД наш массив
                dbHelper!!.printDB()  //и выводим в консоль для проверки
            } else {  //иначе, если в БД есть записи
                println("DB has records")
                dbHelper!!.printDB()   //выводим записи в консоль для проверки
                val tempLangArray = dbHelper!!.getLangsArray()  //берем записи из БД в виде массива
                viewModel.clearList() //очищаем нашу модель данных
                tempLangArray.forEach {//и в цикле по массиву переносим данные в нашу модель
                    viewModel.addLangToEnd(it)
                }
            }
        }

        setContent {
            val lazyListState = rememberLazyListState() //объект для сохранения состояния списка
            Laba4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            //Тут мы заполняем экран
                            MakeAppBar(
                                viewModel, lazyListState, dbHelper!!
                            )
                        }
                    }
                }
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show() //сообщение для отслеживания
        var tempLangArray = ArrayList<Car>() //временный ArrayList для сохранения данных
        viewModel.langListFlow.value.forEach {//переносим данные из нашего основного массива
            tempLangArray.add(it)
        }
        outState.putSerializable("langs", tempLangArray) //помещаем созданный массив в хранилище
//и даем ему метку langs, по ней потом его и найдем
        super.onSaveInstanceState(outState)  //вызов метода базового класса
    }

    class LangsDbHelper(context: Context) : //наш класс для работы с БД, наследуется от
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) { //стандартного класса
        companion object { // тут прописываем переменные для БД
            private val DATABASE_NAME = "LANGS" //имя БД
            private val DATABASE_VERSION = 2 // версия
            val TABLE_NAME = "langs_table" // имя таблицы
            val ID_COL = "id" // переменная для поля id
            val NAME_COl = "lang_name" // переменная для поля lang_name
            val YEAR_COL = "year" // переменная для поля year

            val LITER_COL = "liter" // переменная для поля year

            val NUMBER_COL = "number" // переменная для поля year
            val PICTURE_COL = "picture" // переменная для поля picture
        }

        override fun onCreate(db: SQLiteDatabase) { //метод для создания таблицы через SQL-запрос
            val query =
                ("CREATE TABLE " + TABLE_NAME + " (" + ID_COL + " INTEGER PRIMARY KEY autoincrement, " + NAME_COl + " TEXT, " + YEAR_COL + " INTEGER, " + LITER_COL + " REAL, " + NUMBER_COL + " TEXT, " + PICTURE_COL + " TEXT)")

            db.execSQL(query) // выполняем SQL-запрос
        }

        override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {//метод для обновления БД
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
            onCreate(db)
        }

        fun getCurcor(): Cursor { // метод для получения всех записей таблицы БД в виде курсора
            val db = this.readableDatabase // получаем ссылку на БД только для чтения
            return db.rawQuery("SELECT * FROM " + TABLE_NAME, null) //возвращаем курсор в виде
        }      //результата выборки всех записей из нашей таблицы

        fun isEmpty(): Boolean { //метод для проверки БД на отсутствие записей
            val cursor = getCurcor()  //получаем курсор таблицы БД с записями
            return !cursor!!.moveToFirst()  //и возвращаем результат перехода к первой записи,
        } //инвертируя его, т.е. если нет записей, cursor!!.moveToFirst() вернет false, отрицание его

        //даст true
        fun printDB() {  //метод для печати БД в консоль
            val cursor = getCurcor()  //получаем курсор БД
            if (!isEmpty()) { //если БД не пустая
                cursor!!.moveToFirst()  //переходим к первой записи
                val nameColIndex = cursor.getColumnIndex(NAME_COl) //получаем индексы для колонок
                val yearColIndex = cursor.getColumnIndex(YEAR_COL) //с нужными данными
                val pictureColIndex = cursor.getColumnIndex(PICTURE_COL)
                do {  //цикл по всем записям
                    print("${cursor.getString(nameColIndex)} ") //печатаем данные поля с именем
                    print("${cursor.getString(yearColIndex)} ") //поля с годом
                    println("${cursor.getString(pictureColIndex)} ") //поля с картинкой
                } while (cursor.moveToNext())  //пока есть записи
            } else println("DB is empty") //иначе печатаем, что БД пустая
        }

        fun addArrayToDB(progLangs: ArrayList<Car>) { //метод для добавления целого массива в БД
            progLangs.forEach { //цикл по всем элементам массива
                addLang(it) //добавляем элемент массива в БД
            }
        }


        fun addLang(lang: Car) { // метод для добавления языка в БД
            val values = ContentValues() // объект для создания значений, которые вставим в БД
            values.put(NAME_COl, lang.name)
            values.put(YEAR_COL, lang.year)
            values.put(LITER_COL, lang.liter)
            values.put(NUMBER_COL, lang.number)
            values.put(PICTURE_COL, lang.picture)

            val db = this.writableDatabase //получаем ссылку для записи в БД
            db.insert(TABLE_NAME, null, values) // вставляем все значения в БД в нашу таблицу
            db.close() // закрываем БД (для записи)
        }

        fun deleteLang(lang: Car) { // метод для добавления языка в БД
            val db = this.writableDatabase //получаем ссылку для записи в БД
            ContentValues() // объект для изменения записи
            db.delete(
                TABLE_NAME, "$NUMBER_COL = ?",

                arrayOf(lang.number)
            )
            db.close() // закрываем БД (для записи)
        }

        fun changeImgForLang(name: String, img: String) { // метод для изменения картинки для языка
            val db = this.writableDatabase //получаем ссылку для записи в БД
            val values = ContentValues() // объект для изменения записи
            values.put(PICTURE_COL, img) // вставляем новую картинку
//и делаем запрос в БД на изменение поля с нужным названием в нашей таблице
            db.update(TABLE_NAME, values, NAME_COl + " = '$name'", null)
            db.close() // закрываем БД (для записи)
        }

        fun getLangsArray(): ArrayList<Car> { // метод для получения данных из таблицы в виде
            //массива
            var progsArray = ArrayList<Car>() //массив, в который запишем данные
            val cursor = getCurcor() //получаем курсор таблицы БД
            if (!isEmpty()) { //если БД не пустая
                cursor!!.moveToFirst() //переходим к первой записи
                val nameColIndex = cursor.getColumnIndex(NAME_COl) //получаем индексы для колонок
                val yearColIndex = cursor.getColumnIndex(YEAR_COL) //с нужными данными
                val pictureColIndex = cursor.getColumnIndex(PICTURE_COL)
                val literColIndex = cursor.getColumnIndex(LITER_COL)
                val numberColIndex = cursor.getColumnIndex(NUMBER_COL)
                do {  //цикл по всем записям
                    val name = cursor.getString(nameColIndex)  //получаем данные полей
                    val year = cursor.getString(yearColIndex).toInt() //и записываем их в переменные
                    val picture = cursor.getString(pictureColIndex) //ТУТ!
                    val liter = cursor.getDouble(literColIndex)
                    val number = cursor.getString(numberColIndex)
                    progsArray.add(Car(name, year, liter, number, picture))
                } while (cursor.moveToNext())  //пока есть записи
            } else println("DB is empty") //иначе пишем, что БД пустая
            return progsArray  //возвращаем созданный массив
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MakeAppBar(model: ItemViewModel, lazyListState: LazyListState, dbHelper: LangsDbHelper) {
        var mDisplayMenu by remember { mutableStateOf(false) }
        val mContext = LocalContext.current // контекст нашего приложения
        val openDialog = remember { mutableStateOf(false) } //объект для состояния дочернего окна

        val scope = rememberCoroutineScope()
        val startForResult = //переменная-объект класса ManagedActivityResultLauncher,
//ей присваиваем результат вызова метода rememberLauncherForActivityResult
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {//то берем объект из его данных
                    val newLang = result.data?.getSerializableExtra("newItem") as Car
                    println("new lang name = ${newLang.name}") //вывод для отладки
                    model.addLangToHead(newLang)
                    dbHelper.addLang(newLang) //добавляем новый язык в БД ТУТ после inputa
                    scope.launch {  //прокручиваем список, чтобы был виден добавленный элемент
                        lazyListState.scrollToItem(0)
                    }
                }
            }
        val drawerStateObj = rememberDrawerState(initialValue = DrawerValue.Closed)
        if (openDialog.value) //если дочернее окно вызвано, то запускаем функцию для его создания
            MakeAlertDialog(context = mContext, dialogTitle = "About", openDialog = openDialog)
        TopAppBar( //создаем верхнюю панель нашего приложения, в нем будет меню
            title = { Text("Супер тачки") }, //заголовок в верхней панели
            actions = { //здесь разные действия можно прописать, например, иконку для меню
//нужно в build.gradle.kts(Module) добавить в секцию dependencies строку
// implementation("androidx.compose.material:material-icons-core")
                IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) { //создаем иконку
                    Text("☰") // Текст вместо иконки  //в виде трех вертикальных точек
                } //в методе onClick прописано изменение объекта для хранения состояния меню
                DropdownMenu( //создаем меню
                    expanded = mDisplayMenu, //признак, открыто оно или нет
                    onDismissRequest = { mDisplayMenu = false } //при закрытии меню устанавливаем
                    //соответствующее значение объекту mDisplayMenu
                ) {
                    DropdownMenuItem( //создаем пункт меню для вызова информации о программе (About)
                        text = { Text(text = "About") }, //его текст
                        onClick = {  //и обработчик нажатия на него
                            //всплывающее сообщение с названием пункта
                            Toast.makeText(mContext, "About", Toast.LENGTH_SHORT).show()
                            mDisplayMenu =
                                !mDisplayMenu //меняем параметр, отвечающий за состояние меню,
                            openDialog.value =
                                true //и параметр, отвечающий за состояние дочернего окна,
                        }    //в котором выводим  доп. информацию
                    )
                    //создаем второй пункт меню для вызова окна, в которое перенесли ввод нового языка
                    DropdownMenuItem(text = { Text(text = "Добавить машину") }, onClick = {
                        Toast.makeText(mContext, "Add car", Toast.LENGTH_SHORT).show()
                        val newAct = Intent(mContext, InputActivity::class.java)
                        startForResult.launch(newAct) //запускаем новое окно и ждем от него данные
                        mDisplayMenu = !mDisplayMenu
                    })
                }
            }, navigationIcon = { //описываем левую кнопку с навигацией (три горизонтальных полоски)
                IconButton(
                    //кнопка с иконкой
                    onClick = { //при нажатии на нее будет раскрываться или закрываться меню
                        scope.launch {
                            if (drawerStateObj.isClosed) drawerStateObj.open() //для открытия
                            else drawerStateObj.close() //для закрытия
                        }
                    },
                ) {
                    Icon( //для самой иконки
                        Icons.Rounded.Menu, //берем изображение из системных ресурсов
                        contentDescription = "" //можно добавить описание
                    )
                }
            })
        ModalNavigationDrawer( //это само боковое левое меню
            drawerState = drawerStateObj, //параметр, отвечающий за раскрытие меню, связываем с нашим объектом
            drawerContent = { //содержимое меню
                ModalDrawerSheet { //лист с меню
                    Spacer(Modifier.height(12.dp)) //отступ
                    NavigationDrawerItem( //пункт меню
                        icon = {
                            Icon(
                                Icons.Default.Star, contentDescription = null
                            )
                        }, //иконка для него
                        label = { Text("Drawing") }, //текст для него
                        selected = false, //выбран или нет (актуально, когда несколько эл-ов)
                        onClick = { //обработчик нажатия
                            scope.launch { drawerStateObj.close() } //закрываем меню
                            val newAct =
                                Intent(mContext, DrawingActivity::class.java)//создаем намерение

                            mContext.startActivity(newAct) //и запускаем новое активити (описано ниже)
                        }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            },
            content = { //а здесь содержимое нашего приложения, сюда переносим вызов метода MakeList
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MakeList(viewModel = model, lazyListState, dbHelper)
                }
            })
    }

    @Composable
    fun MakeList(viewModel: ItemViewModel, lazyListState: LazyListState, dbHelper: LangsDbHelper) {
        val langListState = viewModel.langListFlow.collectAsState()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(Color.White),
            state = lazyListState
        ) {
            items(items = viewModel.langListFlow.value, key = { it.name }) { item ->
                ListRow(item, langListState.value, viewModel, dbHelper)
            }
        }
    }

    @Composable
    fun MakeAlertDialog(context: Context, dialogTitle: String, openDialog: MutableState<Boolean>) {
//создаем переменную, в ней будет сохраняться текст, полученный из строковых ресурсов для выбранного языка
        var strValue = remember { mutableStateOf("") } //для получения значения строки из ресурсов
//получаем id нужной строки из ресурсов через имя в dialogTitle
        val strId = context.resources.getIdentifier(dialogTitle, "string", context.packageName)
//секция try..catch нужна для обработки ошибки Resources.NotFoundException – отсутствие искомого ресурса
        try { //если такой ресурс есть (т.е. его id не равен 0), то берем само значение этого ресурса
            if (strId != 0) strValue.value = context.getString(strId)
        } catch (e: Resources.NotFoundException) {
            //если произошла ошибка Resources.NotFoundException, то ничего не делаем
        }
        AlertDialog( // создаем AlertDialog
            onDismissRequest = { openDialog.value = false },//действия при закрытии окна
            title = { Text(text = dialogTitle) }, //заголовок окна
            text = { Text(text = strValue.value, fontSize = 20.sp) },//содержимое окна
            confirmButton = { //кнопка Ok, которая будет закрывать окно
                Button(onClick = { openDialog.value = false }) { Text(text = "OK") }
            })
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ListRow(
        model: Car, langListState: List<Car>, viewModel: ItemViewModel, dbHelper: LangsDbHelper
    ) {
        val context = LocalContext.current
        val openDialog = remember { mutableStateOf(false) }
        var langSelected = remember { mutableStateOf("") }
        if (openDialog.value) MakeAlertDialog(
            context,
            langSelected.value,
            openDialog
        ) //ПОЯВЛЯЕТСЯ ДИАЛОГ
        var mDisplayMenu by remember { mutableStateOf(false) }
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
                if (res.data?.data != null) { //если действительно выбран файд!!
                    println("image uri = ${res.data?.data}")
                    val imgURI = res.data?.data!!

                    context.contentResolver.takePersistableUriPermission(
                        imgURI, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val index = langListState.indexOf(model)
                    viewModel.changeImage(index, imgURI.toString())
                    dbHelper!!.changeImgForLang(model.name, imgURI.toString())//меняем картинку в БД
                }
            }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color.Red))
                .combinedClickable(onClick = { //ЕСЛИ МЫ КЛИКАЕМ НА ROW
                    println("item = ${model.name}")
                    langSelected.value =
                        model.name //сохраняем имя языка, чтобы вставить в заголовок
// AlertDialog
                    Toast.makeText(context, "item = ${model.name}", Toast.LENGTH_LONG).show()
                    openDialog.value = true
                }, onLongClick = {
                    mDisplayMenu = true
                })//присваиваем признаку открытия дочернего окна true
        ) {
            Row(
                modifier = Modifier.weight(1f), // ← меняем fillMaxSize на weight
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column() {
                    Text(
                        text = model.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 18.dp),
                        color = Color.Black
                    )
                    Text(
                        text = model.year.toString(),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = FontStyle.Italic,
                        color = Color.Black

                    )
                }
                Column {
                    Text(
                        text = model.liter.toString() + "Л",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = FontStyle.Italic,
                        color = Color.Black

                    )
                    Text(
                        text = model.number.toString(),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = FontStyle.Italic,
                        color = Color.Black

                    )
                }


                DropdownMenu(
                    expanded = mDisplayMenu, onDismissRequest = { mDisplayMenu = false }) {
                    DropdownMenuItem(text = {

                        Text(
                            text = "Поменять картинку", fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )

                    }, onClick = {
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
                    DropdownMenuItem(text = {
                        Text(
                            text = "Удалить тачку", fontSize = 20.sp,
                        )

                    }, onClick = {
                        mDisplayMenu = !mDisplayMenu
                        viewModel.removeItem(model)
                        dbHelper.deleteLang(model)
                    }

                    )
                }
            }
            Image(
                painter = if (pictureIsInt(model.picture)) painterResource(model.picture.toInt())
                else rememberImagePainter(model.picture),
                contentDescription = "",
                contentScale = ContentScale.Crop, //КАРТИКНИ ОДНОГО РАЗМЕРА
                modifier = Modifier.size(75.dp)
            )

        }
    }

    fun pictureIsInt(picture: String): Boolean { //ф-ия для проверки источника изображения
// переменной data присваиваем результат блока try … catch
        var data = try { //пробуем перевести строку с ресурсом картинки в число, т.к. внутренние
            picture.toInt()  //ресурсы приложения хранятся в виде числового id
        } catch (e: NumberFormatException) {  //если строка не переводится в число, то значит это
            null  //изображение из внешних ресурсов и присваиваем null
        } //в результате data будет равна либо picture.toInt(), либо null
        return data != null  //результат ф-ии зависит от значения переменной data
    }

    @Composable
    fun MakeInputPart(model: ItemViewModel, lazyListState: LazyListState) {
        var langName by remember { //объект для работы с текстом, для названия языка
            mutableStateOf("")  //его начальное значение
        }//в функцию mutableStateOf() в качестве параметра передается отслеживаемое значение
        var langYear by remember { //объект для работы с текстом, для года создания языка
            mutableStateOf(0) //его начальное значение
        }
        var liter by remember { //объект для работы с текстом, для года создания языка
            mutableStateOf(0.0) //его начальное значение
        }
        var number by remember { //объект для работы с текстом, для года создания языка
            mutableStateOf("") //его начальное значение
        }

        val scope =
            rememberCoroutineScope() //объект для прокручивания списка при вставке нового эл-та
        Row(
            //ряд для расположения эл-ов
            verticalAlignment = Alignment.CenterVertically, //центруем по вертикали
            horizontalArrangement = Arrangement.spacedBy(10.dp), //и добавляем отступы между эл-ми
        ) {


            TextField( //текстовое поле для ввода имени языка
                value = langName, //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText ->  //обработчик ввода значений в поле
                    langName = newText  //все изменения сохраняем в наш объект
                }, textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp  //увеличиваем шрифт
                ), label = { Text("Название") }, //это надпись в текстовом поле
                modifier = Modifier.weight(2f)//это вес колонки.Нужен для распределения долей в ряду.
//Контейнер Row позволяет назначить вложенным компонентам ширину в соответствии с их весом.
//Поэтому полям с данными назначаем вес 2, кнопке вес 1, получается сумма
// всех весов будет 5, и для полей с весом 2 будет выделяться по 2/5 от всей ширины ряда, для
//кнопки с весом 1 будет выделяться 1/5 от всей ширины ряда
            )

            TextField( //текстовое поле для ввода года создания языка
                value = langYear.toString(), //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText ->  //обработчик ввода значений в поле
//т.к. newText (измененный текст) – это строка, а langYear – целое, то нужно преобразовывать
                    langYear = if (newText != "") newText.toInt() else 0  //в нужный формат
                },                    //с учетом возможной пустой строки
                textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp  //увеличиваем шрифт
                ),
                //и меняем тип допустимых символов для ввода – только цифры
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Год создания") },
                modifier = Modifier.weight(2f) //назначаем вес поля


//и добавляем в начало списка новый язык с нужными параметрами
            )
        }
        Row(
            //ряд для расположения эл-ов
            verticalAlignment = Alignment.CenterVertically, //центруем по вертикали
            horizontalArrangement = Arrangement.spacedBy(10.dp), //и добавляем отступы между эл-ми
        ) {
            TextField( //текстовое поле для ввода года создания языка
                value = liter.toString(), //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText ->  //обработчик ввода значений в поле
//т.к. newText (измененный текст) – это строка, а langYear – целое, то нужно преобразовывать
                    liter = if (newText != "") newText.toDouble() else 0.0 //в нужный формат
                },                    //с учетом возможной пустой строки
                textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp  //увеличиваем шрифт
                ),
                //и меняем тип допустимых символов для ввода – только цифры
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Движок") },
                modifier = Modifier.weight(2f) //назначаем вес поля

//и добавляем в начало списка новый язык с нужными параметрами
            )
            TextField( //текстовое поле для ввода года создания языка
                value = number.toString(), //связываем текст из поля с созданным ранее объектом
                onValueChange = { newText ->  //обработчик ввода значений в поле
//т.к. newText (измененный текст) – это строка, а langYear – целое, то нужно преобразовывать
                    number = if (newText != "") newText.toString() else "" //в нужный формат
                },                    //с учетом возможной пустой строки
                textStyle = TextStyle( //объект для изменения стиля текста
                    fontSize = 20.sp  //увеличиваем шрифт
                ),
                //и меняем тип допустимых символов для ввода – только цифры
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text("Номер") },
                modifier = Modifier.weight(2f) //назначаем вес поля

//и добавляем в начало списка новый язык с нужными параметрами
            )
            LocalContext.current
            Button( //ТУТ!!
                onClick = {
                    println("added $langName $langYear $liter $number")
                    Car(langName, langYear, liter, number)
                    model.addLangToHead(Car(langName, langYear, liter, number))

                    scope.launch {
                        lazyListState.scrollToItem(0)
                    }
                }, modifier = Modifier.weight(3f)
            ) {
                Text("Add")
            }
        }
    }
}
