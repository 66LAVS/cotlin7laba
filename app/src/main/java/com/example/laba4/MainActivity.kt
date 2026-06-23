package com.example.laba4

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laba4.ui.theme.Laba4Theme

class MainActivity : ComponentActivity() {

    private val PREFS_NAME = "UserPrefs"
    private val KEY_IS_ADMIN = "isAdmin"
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Обработка системной кнопки "Назад" - закрываем приложение только если нет других активностей
        onBackPressedDispatcher.addCallback(this) {
            finishAffinity() // Закрываем все активности
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false)
        val username = prefs.getString(KEY_USERNAME, "")
        val password = prefs.getString(KEY_PASSWORD, "")

        // Автовход если есть сохраненные данные
        if (username != null && password != null && username.isNotEmpty() && password.isNotEmpty()) {
            if (isAdmin) {
                startActivity(Intent(this, AdminActivity::class.java))
                // НЕ закрываем MainActivity
                return
            } else {
                startActivity(Intent(this, UserActivity::class.java))
                // НЕ закрываем MainActivity
                return
            }
        }

        setContent {
            Laba4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // При возврате из UserActivity обновляем состояние
        // Очищаем сохраненные данные если нужно
    }

    @Composable
    fun LoginScreen() {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isRegistering by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegistering) "Create Account" else "Medicine App Login",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                textStyle = TextStyle(fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                textStyle = TextStyle(fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (isRegistering) {
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Button(
                onClick = {
                    if (isRegistering) {
                        if (username == "admin123") {
                            Toast.makeText(context, "Username 'admin123' is reserved for admin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (username.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val dbHelper = UsersDbHelper(context)
                        if (dbHelper.userExists(username)) {
                            Toast.makeText(context, "Username already exists!", Toast.LENGTH_SHORT).show()
                            dbHelper.close()
                            return@Button
                        }

                        dbHelper.addUser(username, password, false)
                        dbHelper.close()

                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString(KEY_USERNAME, username)
                            putString(KEY_PASSWORD, password)
                            putBoolean(KEY_IS_ADMIN, false)
                            apply()
                        }

                        Toast.makeText(context, "Registration Successful!", Toast.LENGTH_LONG).show()

                        val intent = Intent(context, UserActivity::class.java)
                        context.startActivity(intent)
                        // НЕ закрываем MainActivity

                    } else {
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val savedUsername = prefs.getString(KEY_USERNAME, "")
                        val savedPassword = prefs.getString(KEY_PASSWORD, "")
                        val isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false)

                        if (username == "admin123" && password == "admin123") {
                            Toast.makeText(context, "Admin Login Successful!", Toast.LENGTH_SHORT).show()
                            prefs.edit().apply {
                                putString(KEY_USERNAME, username)
                                putString(KEY_PASSWORD, password)
                                putBoolean(KEY_IS_ADMIN, true)
                                apply()
                            }
                            val intent = Intent(context, AdminActivity::class.java)
                            context.startActivity(intent)
                            // НЕ закрываем MainActivity
                            return@Button
                        }

                        if (savedUsername != null && savedPassword != null &&
                            username == savedUsername && password == savedPassword && !isAdmin) {
                            Toast.makeText(context, "User Login Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, UserActivity::class.java)
                            context.startActivity(intent)
                            // НЕ закрываем MainActivity
                            return@Button
                        }

                        Toast.makeText(context, "Invalid username or password!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isRegistering) "Register" else "Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { isRegistering = !isRegistering },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isRegistering) "Already have an account? Login"
                        else "Don't have an account? Register",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isRegistering) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👤 Register as a regular user to view medicines",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    class UsersDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            private val DATABASE_NAME = "USERS_DB"
            private val DATABASE_VERSION = 1
            val TABLE_NAME = "users_table"
            val ID_COL = "id"
            val USERNAME_COL = "username"
            val PASSWORD_COL = "password"
            val IS_ADMIN_COL = "is_admin"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val query = ("CREATE TABLE " + TABLE_NAME + " (" +
                    ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USERNAME_COL + " TEXT UNIQUE, " +
                    PASSWORD_COL + " TEXT, " +
                    IS_ADMIN_COL + " INTEGER)")
            db.execSQL(query)
        }

        override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
            onCreate(db)
        }

        fun addUser(username: String, password: String, isAdmin: Boolean) {
            try {
                val values = ContentValues()
                values.put(USERNAME_COL, username)
                values.put(PASSWORD_COL, password)
                values.put(IS_ADMIN_COL, if (isAdmin) 1 else 0)

                val db = this.writableDatabase
                db.insert(TABLE_NAME, null, values)
                db.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun userExists(username: String): Boolean {
            val db = this.readableDatabase
            val cursor: Cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $USERNAME_COL = ?",
                arrayOf(username)
            )
            val exists = cursor.count > 0
            cursor.close()
            db.close()
            return exists
        }

        fun getUser(username: String): Cursor? {
            val db = this.readableDatabase
            return db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $USERNAME_COL = ?",
                arrayOf(username)
            )
        }
    }
}