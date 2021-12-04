package jp.ac.it_college.s20012.apisample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import jp.ac.it_college.s20012.apisample.databinding.ActivityMainBinding
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val helper = Database(this)
    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        communication()

        binding.numPicker.apply {
            maxValue = 50
            minValue = 10

        }

        binding.next.setOnClickListener {
            println(binding.numPicker.value)
            val intent = Intent(this, Sample::class.java)
            intent.putExtra("NUM", binding.numPicker.value)
            startActivity(intent)
            finish()
        }


    }

    //取得したAPIデータをデータベースに書き込み
    @SuppressWarnings("all")
    private fun communication() {
        val handler = HandlerCompat.createAsync(mainLooper)
        val executeService = Executors.newSingleThreadExecutor()

        //ここは別スレッド
        executeService.submit @WorkerThread {
            var result = ""
            val url = URL("https://script.google.com/macros/s/AKfycbznWpk2m8q6lbLWSS6qaz3uS6j3L4zPwv7CqDEiC433YOgAdaFekGJmjoAO60quMg6l/exec?f=data")
            val con = url.openConnection() as? HttpURLConnection
            con?.let {
                try {
                    it.connectTimeout = 15000
                    it.readTimeout = 15000
                    it.requestMethod = "GET"
                    it.connect()
                    val stream = it.inputStream
                    result = is2String(stream)

                    stream.close()
                } catch (e: SocketTimeoutException) {
                    Log.d("TAG", "通信タイムアウト", e)
                }
                it.disconnect()
            }
            //handler.post の中はUIスレッド

            handler.post @UiThread {
                val rootJSON = JSONArray(result)

                //データベース消去
                val db = helper.writableDatabase
                val sqlDelete = """
                DELETE FROM Test1
                """.trimIndent()
                var stmt = db.compileStatement(sqlDelete)
                stmt.executeUpdateDelete()

                for(i in 0..149) {
                    val name = rootJSON.getJSONObject(i) //問題指定
                    val question = name.getString("question")
                    val answers = name.getString("answers").toLong()
                    val choices = name.getJSONArray("choices")
                    val choices1 = choices[0] as String
                    val choices2 = choices[1] as String
                    val choices3 = choices[2] as String
                    val choices4 = choices[3] as String
                    val choices5 = choices[4] as String
                    val choices6 = choices[5] as String

                    val sqlInsert = """
                INSERT INTO Test1 (id, question, answers, choices1, choices2, choices3, choices4, choices5, choices6)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
                    stmt = db.compileStatement(sqlInsert)
                    stmt.bindLong(1, i.toLong())
                    stmt.bindString(2, question)
                    stmt.bindLong(3, answers)
                    stmt.bindString(4, choices1)
                    stmt.bindString(5, choices2)
                    stmt.bindString(6, choices3)
                    stmt.bindString(7, choices4)
                    stmt.bindString(8, choices5)
                    stmt.bindString(9, choices6)
                    stmt.executeInsert()
                }
            }
        }
    }

    private fun is2String(stream: InputStream): String {
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while(line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    override fun onDestroy()  {
        helper.close()
        super.onDestroy()
    }

}