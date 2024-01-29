package org.techtown.unsexception

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import org.techtown.unsexception.databinding.ActivityMainBinding
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tag: String = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()

        restartAfterCrash()
    }

    private fun initView() = with(binding) {
        option1Button.setOnClickListener { listOf("")[1] }
        option2Button.setOnClickListener { throw ArithmeticException("Cannot be divided by 0") }
        option3Button.setOnClickListener { throw NullPointerException("The variable is null") }
    }

    private fun saveExceptionToFile(exception: Throwable, tag: String) {
        val formatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss")
        val now = LocalDateTime.now().format(formatter)

        // [다운로드] 폴더
        val externalStorageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // [app_name] 폴더를 만들기 위한 File 객체 생성
        val appNameFolder = File(externalStorageDirectory, getString(R.string.app_name))
        // [app_name] 폴더가 없다면 생성
        if (!appNameFolder.exists()) appNameFolder.mkdirs()
        val file = File(appNameFolder, "${now}_$tag.txt")

        try {
            // StringWriter를 사용하여 예외 상세 내용을 문자열로 변환
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            exception.printStackTrace(printWriter)
            val exceptionString = stringWriter.toString()

            // 파일에 문자열 저장
            val writer = FileWriter(file)
            writer.write(exceptionString)
            writer.close()

            // 파일이 성공적으로 저장되었음을 알림
            AppData.debug(tag, "예외 내용이 파일에 저장되었습니다: ${file.path}")
        } catch (e: Exception) {
            // 파일 저장 중 예외가 발생한 경우 처리
            AppData.debug(tag, "예외 내용을 파일에 저장하는 중에 문제가 발생했습니다: ${e.message}")
        }
    }

    private fun restartAfterCrash() = Thread.setDefaultUncaughtExceptionHandler { _, e ->
        saveExceptionToFile(e, MainActivity().toString())
        Intent(this@MainActivity, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(this)
            exitProcess(0)
        }
    }
}