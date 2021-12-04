package jp.ac.it_college.s20012.apisample

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import jp.ac.it_college.s20012.apisample.databinding.ActivitySampleBinding


class Sample : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding
    private val helper = Database(this)
    private var timeLeftCountdown = TimeLeftCountdown()
    private var startTime = 0L
    private var totalElapsedTime = 0L
    private var question = 0

    companion object {
        const val TIME_LIMIT = 10000L
        const val TIMER_INTERVAL = 100L
        var count = 0
        var ok = 0
        var note = ""
        var answers1 = ""
        var answers2 = ""
        var text = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        question = intent.getIntExtra("NUM", 0)


        if(count == 0) {
            readData()
            timeLeftCountdown.start()
        }

            binding.button.setOnClickListener {
                if(note == "1") {
                    radioAnswers()
                } else {
                    checkAnswers()
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        timeLeftCountdown.cancel()
    }

    private fun radioAnswers() {
        val id = binding.radioGroup.checkedRadioButtonId
        if (id == -1) {
            Toast.makeText(this ,"ボタンが押されてないよ!", Toast.LENGTH_SHORT).show()
        } else {
            timeLeftCountdown.cancel()
            text = findViewById<RadioButton>(id).text as String
            if (text == answers1) {
                AlertDialog.Builder(this)
                    .setTitle("正解！")
                    .setPositiveButton("次へ") { _, _ ->
                        ok++
                        next()
                    }
                    .show()

            } else {
                AlertDialog.Builder(this)
                    .setTitle("不正解...")
                    .setPositiveButton("次へ") { _, _ ->
                        next()
                    }
                    .show()
            }
        }
    }

    private fun checkAnswers() {
        val list = arrayListOf<String>()
        val s = binding.checkGroup.childCount
        for(i in 0..s) {
            val v = binding.checkGroup.getChildAt(i)
            if(v is CheckBox) {
                if (v.isChecked) list.add(v.text.toString())
            }
        }
        if(list.size > 2 || list.size <= 1) {
            Toast.makeText(this, "２つ選択してね", Toast.LENGTH_SHORT).show()
        } else {
            timeLeftCountdown.cancel()
            if (answers1 in list && answers2 in list) {
                AlertDialog.Builder(this)
                    .setTitle("正解！")
                    .setPositiveButton("次へ") { _, _ ->
                        ok++
                        next()
                    }
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("不正解...")
                    .setPositiveButton("次へ") { _, _ ->
                        next()
                    }
                    .show()
            }
        }
    }

    //読み込み
    @SuppressLint("Recycle", "SetTextI18n")
    private fun readData() {
        val db = helper.readableDatabase
        val sql = """
                SELECT * FROM Test1
                WHERE id = ${(0..149).random()}
            """.trimIndent()
        val cursor = db.rawQuery(sql, null)
        while (cursor.moveToNext()) {
            val quiz = cursor.let {
                val index = it.getColumnIndex("question")
                it.getString(index)
            }
            note = cursor.let {
                val index = it.getColumnIndex("answers")
                it.getString(index)
            }
            val correctAnswer1 = cursor.let {
                val index = it.getColumnIndex("choices1")
                it.getString(index)
            }
            val correctAnswer2 = cursor.let {
                val index = it.getColumnIndex("choices2")
                it.getString(index)
            }
            val incorrectAnswer2 = cursor.let {
                val index = it.getColumnIndex("choices3")
                it.getString(index)
            }
            val incorrectAnswer3 = cursor.let {
                val index = it.getColumnIndex("choices4")
                it.getString(index)
            }
            val incorrectAnswer4 = cursor.let {
                val index = it.getColumnIndex("choices5")
                it.getString(index)
            }
            val incorrectAnswer5 = cursor.let {
                val index = it.getColumnIndex("choices6")
                it.getString(index)
            }


            answers1 = correctAnswer1
            answers2 = correctAnswer2
            binding.questionNumber.text = "第${count + 1}問"
            binding.question.text = quiz

            val random1 = mutableListOf(
                correctAnswer1,
                correctAnswer2,
                incorrectAnswer2,
                incorrectAnswer3
            ).shuffled()

            val random2 = mutableListOf(
                correctAnswer1,
                correctAnswer2,
                incorrectAnswer2,
                incorrectAnswer3,
                incorrectAnswer4,
                incorrectAnswer5
            )
            random2.removeAll(listOf(""))
            println(random2.size)
            random2.shuffle()

            when (note) {
                "1" -> {
                    binding.checkGroup.visibility = View.GONE

                    binding.radioA.text = random1[0]
                    binding.radioB.text = random1[1]
                    binding.radioC.text = random1[2]
                    binding.radioD.text = random1[3]
                }
                else -> {
                    binding.radioGroup.visibility = View.GONE

                    if(random2.size == 5) {
                        binding.checkA.text = random2[0]
                        binding.checkB.text = random2[1]
                        binding.checkC.text = random2[2]
                        binding.checkD.text = random2[3]
                        binding.checkE.text = random2[4]

                        binding.checkF.visibility = View.GONE
                    } else {
                        binding.checkF.visibility = View.VISIBLE

                        binding.checkA.text = random2[0]
                        binding.checkB.text = random2[1]
                        binding.checkC.text = random2[2]
                        binding.checkD.text = random2[3]
                        binding.checkE.text = random2[4]
                        binding.checkF.text = random2[5]
                    }


                }
            }
        }

    }

    inner class TimeLeftCountdown : CountDownTimer(TIME_LIMIT, TIMER_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            animateToProgress(millisUntilFinished.toInt())
        }

        override fun onFinish() {
            totalElapsedTime += TIME_LIMIT
            animateToProgress(0)
            AlertDialog.Builder(this@Sample)
                .setTitle("時間切れ...")
                .setPositiveButton("次へ") { _, _ ->
                    readData()
                    next()
                }
                .show()

        }

        private fun animateToProgress(progress: Int) {
            val anim = ObjectAnimator.ofInt(binding.timeLeftBar, "progress", progress)
            anim.duration = TIMER_INTERVAL
            anim.start()
        }
    }

    private fun next() {
        count++
        if(count == question) {
            count = 0
            val intent = Intent(this, Result::class.java)
                intent.apply {
                    putExtra("ANSWER", ok)
                    putExtra("NUM", question)
                }
            ok = 0
            startActivity(intent)
            finish()

        } else {
            timeLeftCountdown.cancel()
            binding.timeLeftBar.progress = 10000
            binding.radioGroup.clearCheck()
            val s = binding.checkGroup.childCount
            for(i in 0..s) {
                val v = binding.checkGroup.getChildAt(i)
                if(v is CheckBox) {
                    if (v.isChecked) v.isChecked = false
                }
            }
            binding.radioGroup.visibility = View.VISIBLE
            binding.checkGroup.visibility = View.VISIBLE

            readData()
            timeLeftCountdown.start()
            startTime = SystemClock.elapsedRealtime()
        }
    }
}


