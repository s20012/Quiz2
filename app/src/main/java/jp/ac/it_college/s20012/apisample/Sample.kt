package jp.ac.it_college.s20012.apisample

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import jp.ac.it_college.s20012.apisample.databinding.ActivitySampleBinding

class Sample : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding
    private val helper = Database(this)
    companion object {
        const val TIME_LIMIT = 10000L
        const val TIMER_INTERVAL = 100L
        const val CHOICE_DELAY_TIME = 2000L
        const val TIME_UP_DELAY_TIME = 1500L
        var count = 0
        var s = 1
        var ok = 0
        var ans = ""
        var text = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readData(s)

        TimeLeftCountdown()
        binding.button.setOnClickListener {
            val id = binding.radioGroup.checkedRadioButtonId
                if (id == -1) {
                    AlertDialog.Builder(this)
                        .setTitle("ちょっと待って！")
                        .setMessage("ボタンが押されてないよ")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    count++
                    text = findViewById<RadioButton>(id).text as String
                    if (text == ans) {
                        AlertDialog.Builder(this)
                            .setTitle("正解！")
                            .setPositiveButton("次へ") {_, _ ->
                                ok++
                                next()
                            }
                            .show()

                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("不正解...")
                            .setPositiveButton("次へ") {_, _ ->
                                next()
                            }
                            .show()
                    }
            }


        }
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

    /**
     * 制限時間をカウントダウンするタイマー
     */
    inner class TimeLeftCountdown : CountDownTimer(TIME_LIMIT, TIMER_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            animateToProgress(millisUntilFinished.toInt())
        }

        override fun onFinish() {
            animateToProgress(0)

        }

        /**
         * API Level 24 であれば、ProgressBar 自体にアニメーションのパラメータがありますが
         * 今回は 23 なので、ObjectAnimator を使って実装
         */
        private fun animateToProgress(progress: Int) {
            val anim = ObjectAnimator.ofInt(binding.timeLeftBar, "progress", progress)
            anim.duration = TIMER_INTERVAL
            anim.start()
        }
    }


    private fun next() {
        if(count == 10) {
            val intent = Intent(this, Result::class.java)
            startActivity(intent)
            finish()
        } else {
            s = (0..74).random()
            binding.radioGroup.clearCheck()
            binding.radioE.visibility = View.VISIBLE
            binding.radioF.visibility = View.VISIBLE
            readData(s)
        }
    }
}