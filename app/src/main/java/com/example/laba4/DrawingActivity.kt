package com.example.laba4

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.laba4.ui.theme.Laba4Theme
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.KFunction0

class DrawingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val buttonNamesRow1 = arrayOf(
                stringResource(R.string.rect),
                stringResource(R.string.circle),
                stringResource(R.string.romb),
            )

            val buttonNamesRow2 = arrayOf(
                stringResource(R.string.red),
                stringResource(R.string.green),
                stringResource(R.string.blue),
            )

            val buttonNamesRow3 = arrayOf(
                stringResource(R.string.surname),
                stringResource(R.string.sterka)
            )

            val buttonNamesRow4 = arrayOf(
                "Сплошная",
                "Пунктирная"
            )

            val buttonNamesUnder = arrayOf(
                stringResource(R.string.image),
                stringResource(R.string.save)
            )

            val myView: MyGraphView = MyGraphView(applicationContext)
            val strokeWidth = remember { mutableStateOf(12f) }

            Laba4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val permission: String = Manifest.permission.READ_EXTERNAL_STORAGE
                    val grant = ContextCompat.checkSelfPermission(context, permission)
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        val permission_list = arrayOfNulls<String>(1)
                        permission_list[0] = permission
                        ActivityCompat.requestPermissions(context as Activity, permission_list, 1)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        MakeTopButtons(buttonNamesRow1, myView.funcArrayRow1, myView)
                        Spacer(modifier = Modifier.height(8.dp))
                        MakeTopButtons(buttonNamesRow2, myView.funcArrayRow2, myView)
                        Spacer(modifier = Modifier.height(8.dp))
                        MakeTopButtons(buttonNamesRow3, myView.funcArrayRow3, myView)
                        Spacer(modifier = Modifier.height(8.dp))
                        MakeTopButtons(buttonNamesRow4, myView.funcArrayRow4, myView)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Толщина: ${strokeWidth.value.toInt()}",
                                fontSize = 14.sp,
                                modifier = Modifier.width(80.dp)
                            )
                            Slider(
                                value = strokeWidth.value,
                                onValueChange = { newValue ->
                                    strokeWidth.value = newValue
                                    myView.setStrokeWidth(newValue)
                                },
                                valueRange = 2f..50f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AndroidView(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            factory = { myView }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        MakeTopButtons(buttonNamesUnder, myView.funcArrayRow5, myView)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MakeTopButtons(buttonNames: Array<String>, funcArray: Array<KFunction0<Unit>>, myView: MyGraphView) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        buttonNames.forEachIndexed { index, buttonName ->
            Button(
                onClick = {
                    funcArray[index]()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = buttonName,
                    maxLines = 1
                )
            }
        }
    }
}

class MyGraphView(context: Context?) : View(context) {
    private lateinit var path: Path
    private var mPaint: Paint = Paint()
    private var mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var currentColor = Color.GREEN
    private var isEraserMode = false
    private var savedStrokeWidth = 12f
    private var currentLineStyle = LineStyle.SOLID

    enum class LineStyle {
        SOLID,      // Сплошная линия
        DASHED      // Пунктирная линия
    }

    init {
        setupPaint()
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.color = currentColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 12F
        updateLineStyle()
    }

    private fun updateLineStyle() {
        when (currentLineStyle) {
            LineStyle.SOLID -> {
                mPaint.pathEffect = null
            }
            LineStyle.DASHED -> {
                mPaint.pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 0f)
            }
        }
        invalidate()
    }

    fun setSolidLine() {
        currentLineStyle = LineStyle.SOLID
        updateLineStyle()
        Toast.makeText(context, "Сплошная линия", Toast.LENGTH_SHORT).show()
    }

    fun setDashedLine() {
        currentLineStyle = LineStyle.DASHED
        updateLineStyle()
        Toast.makeText(context, "Пунктирная линия", Toast.LENGTH_SHORT).show()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap!!)
            mCanvas?.drawColor(Color.WHITE)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, mBitmapPaint)
        }
    }

    fun setStrokeWidth(width: Float) {
        mPaint.strokeWidth = width
        if (!isEraserMode) {
            savedStrokeWidth = width
        }
        Toast.makeText(context, "Толщина: ${width.toInt()}", Toast.LENGTH_SHORT).show()
    }

    fun setRedColor() {
        currentColor = Color.RED
        isEraserMode = false
        mPaint.color = currentColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = savedStrokeWidth
        updateLineStyle()
        Toast.makeText(context, "Красный цвет", Toast.LENGTH_SHORT).show()
    }

    fun setGreenColor() {
        currentColor = Color.GREEN
        isEraserMode = false
        mPaint.color = currentColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = savedStrokeWidth
        updateLineStyle()
        Toast.makeText(context, "Зеленый цвет", Toast.LENGTH_SHORT).show()
    }

    fun setBlueColor() {
        currentColor = Color.BLUE
        isEraserMode = false
        mPaint.color = currentColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = savedStrokeWidth
        updateLineStyle()
        Toast.makeText(context, "Синий цвет", Toast.LENGTH_SHORT).show()
    }

    fun drawCircle() {
        val savedPathEffect = mPaint.pathEffect
        mPaint.pathEffect = null
        mCanvas?.drawCircle(400f, 400f, 100f, mPaint)
        mPaint.pathEffect = savedPathEffect
        invalidate()
        Toast.makeText(context, "Круг", Toast.LENGTH_SHORT).show()
    }

    fun drawSquare() {
        val savedPathEffect = mPaint.pathEffect
        mPaint.pathEffect = null
        mCanvas?.drawRect(300f, 300f, 500f, 500f, mPaint)
        mPaint.pathEffect = savedPathEffect
        invalidate()
        Toast.makeText(context, "Прямоугольник", Toast.LENGTH_SHORT).show()
    }

    fun drawRhombus() {
        val savedPathEffect = mPaint.pathEffect
        mPaint.pathEffect = null
        val rhombusPath = Path().apply {
            moveTo(400f, 300f)
            lineTo(500f, 400f)
            lineTo(400f, 500f)
            lineTo(300f, 400f)
            close()
        }
        mCanvas?.drawPath(rhombusPath, mPaint)
        mPaint.pathEffect = savedPathEffect
        invalidate()
        Toast.makeText(context, "Ромб", Toast.LENGTH_SHORT).show()
    }

    fun drawName() {
        val textPaint = Paint().apply {
            color = currentColor
            textSize = 60f
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        mCanvas?.drawText("Рамиль Овчиев 52", 350f, 400f, textPaint)
        invalidate()
        Toast.makeText(context, "Имя добавлено", Toast.LENGTH_SHORT).show()
    }

    fun useEraser() {
        isEraserMode = true
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.STROKE
        mPaint.pathEffect = null
        Toast.makeText(context, "Ластик", Toast.LENGTH_SHORT).show()
    }

    fun drawFace() {
        val destPath: String = context.getExternalFilesDir(null)!!.absolutePath
        val mBitmapFromSdcard = BitmapFactory.decodeFile(destPath + "/face.png")
        if (mBitmapFromSdcard != null) {
            val scaledBitmap = Bitmap.createScaledBitmap(
                mBitmapFromSdcard, 150, 150, true)
            mCanvas?.drawBitmap(scaledBitmap, 100f, 100f, mPaint)
            invalidate()
            Toast.makeText(context, "Изображение", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Файл face.png не найден", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSaveClick() {
        try {
            val destPath: String = context.getExternalFilesDir(null)!!.absolutePath
            val file = File(destPath, "myDrawing.PNG")
            val outStream = FileOutputStream(file)
            mBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()
            Toast.makeText(context, "Сохранено в: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Массивы функций для каждого ряда кнопок
    val funcArrayRow1 = arrayOf(
        ::drawSquare,
        ::drawCircle,
        ::drawRhombus
    )

    val funcArrayRow2 = arrayOf(
        ::setRedColor,
        ::setGreenColor,
        ::setBlueColor
    )

    val funcArrayRow3 = arrayOf(
        ::drawName,
        ::useEraser
    )

    val funcArrayRow4 = arrayOf(
        ::setSolidLine,
        ::setDashedLine
    )

    val funcArrayRow5 = arrayOf(
        ::drawFace,
        ::onSaveClick
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path()
                path.moveTo(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                mCanvas?.drawPath(path, mPaint)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(event.x, event.y)
                mCanvas?.drawPath(path, mPaint)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}