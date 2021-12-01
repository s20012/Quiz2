package jp.ac.it_college.s20012.apisample

import android.animation.ObjectAnimator
import android.annotation.SuppressLint

import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
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
        var num = 1
        var ok = 0
        var ans = ""
        var text = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        question = intent.getIntExtra("NUM", 0)
        readData(num)
        if(count == 0) {
            timeLeftCountdown.start()
        }
            binding.button.setOnClickListener {
                Log.d("TIME", totalElapsedTime.toString())
                val id = binding.radioGroup.checkedRadioButtonId
                if (id == -1) {
                    Toast.makeText(this ,"ボタンが押されてないよ!", Toast.LENGTH_SHORT).show()
                } else {
                    timeLeftCountdown.cancel()
                    text = findViewById<RadioButton>(id).text as String
                    if (text == ans) {
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

    }

    override fun onDestroy() {
        super.onDestroy()
        timeLeftCountdown.cancel()

    }


    //読み込み
    @SuppressLint("Recycle", "SetTextI18n")
    private fun readData(i: Int) {
        val dbb = helper.readableDatabase
        val sql = """
                SELECT * FROM Test1
                WHERE id = $i
            """.trimIndent()
        val cursor = dbb.rawQuery(sql, null)
        while (cursor.moveToNext()) {
            val quiz = cursor.let {
                val index = it.getColumnIndex("question")
                it.getString(index)
            }
            val note = cursor.let {
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

            ans = correctAnswer1
            binding.textView3.text = "第${count + 1}問"
            binding.textView.text = quiz

            val random1 = listOf(
                correctAnswer1,
                correctAnswer2,
                incorrectAnswer2,
                incorrectAnswer3
            ).shuffled()

            val random2 = listOf(
                correctAnswer1,
                correctAnswer2,
                incorrectAnswer2,
                incorrectAnswer3,
                incorrectAnswer4,
                incorrectAnswer5
            ).shuffled()

            when(note) {
                "1" -> {
                    binding.radioA.text = random1[0]
                    binding.radioB.text = random1[1]
                    binding.radioC.text = random1[2]
                    binding.radioD.text = random1[3]
                    binding.radioE.visibility = View.GONE
                    binding.radioF.visibility = View.GONE


                }
                else -> {
                    binding.radioA.text = random2[0]
                    binding.radioB.text = random2[1]
                    binding.radioC.text = random2[2]
                    binding.radioD.text = random2[3]
                    binding.radioE.text = random2[4]
                    binding.radioF.text = random2[5]
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
                    readData(num)
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
            num = (0..74).random()
            binding.radioGroup.clearCheck()
            binding.radioE.visibility = View.VISIBLE
            binding.radioF.visibility = View.VISIBLE
            readData(num)
            timeLeftCountdown.start()
            startTime = SystemClock.elapsedRealtime()
        }
    }
}


