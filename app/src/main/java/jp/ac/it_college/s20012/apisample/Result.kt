package jp.ac.it_college.s20012.apisample

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.ac.it_college.s20012.apisample.databinding.ActivityResultBinding


class Result : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val answer = intent.getIntExtra("ANSWER", 0)
        val time = intent.getLongExtra("TIME", 0)

        binding.total.text = "$answer / 10"
        val minutes = time / 10000 / 60
        val second = minutes / 1000 % 60
        binding.time.text = "$second:$minutes"


        binding.backTitle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }

}