package com.southernsunrise.timedown

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.media.MediaPlayer
import androidx.annotation.RawRes


class MainActivity() : AppCompatActivity() {

    lateinit var countDownTimer: CountDownTimer
    lateinit var startPauseButton: Button
    lateinit var resetButton: ImageButton
    lateinit var timeChangeButton: ImageButton
    lateinit var textViewCountDown: TextView
    lateinit var nPickerHour: NumberPicker
    lateinit var nPickerMinute: NumberPicker
    lateinit var nPickerSecond: NumberPicker
    lateinit var confirmButton: FloatingActionButton
    var timerRunning: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        var timeLeftInMillis: Long = 0

        nPickerHour = findViewById(R.id.hour_number_picker)
        nPickerMinute = findViewById(R.id.minute_number_picker)
        nPickerSecond = findViewById(R.id.second_number_picker)
        confirmButton = findViewById(R.id.confirm_button)
        timeChangeButton = findViewById(R.id.time_change_button)


        nPickerHour.minValue = 0
        nPickerHour.maxValue = 99
        nPickerMinute.minValue = 0
        nPickerMinute.maxValue = 59
        nPickerSecond.minValue = 0
        nPickerSecond.maxValue = 59

        // button which will take numbers from number picker and give a value to timeLeftInMillis
        confirmButton.setOnClickListener {
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.INVISIBLE
            timeLeftInMillis =
                ((nPickerHour.value * 3600000) + (nPickerMinute.value * 60000) + nPickerSecond.value * 1000).toLong()
            updateCountDownText(timeLeftInMillis)
        }


        startPauseButton = findViewById(R.id.start_pause_button)
        resetButton = findViewById(R.id.reset_button)

        textViewCountDown = findViewById(R.id.countDown_text)


        startPauseButton.setOnClickListener {

            resetButton.visibility = View.VISIBLE
            if (timerRunning) {
                countDownTimer.cancel()
                timerRunning = false
                startPauseButton.text = "START"
            } else {
                startPauseButton.text = "PAUSE"
                timerRunning = true
                countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // every second update timeLeftInMillis to millisUntilFinished and update countdown textView
                        timeLeftInMillis = millisUntilFinished
                        updateCountDownText(timeLeftInMillis)
                    }

                    override fun onFinish() {
                        val alarm: MediaPlayer = MediaPlayer.create(
                            baseContext,
                            R.raw.alarmsound
                        )

                        startPauseButton.visibility = View.INVISIBLE
                        timerRunning = false
                        startPauseButton.text = "START"
                        alarm.start()
                        alarm.isLooping = true
                        timeChangeButton.visibility = View.INVISIBLE
                        resetButton.setImageResource(R.drawable.ic_stop)

                       // on finish resetButton will stop the alarm and reset timer value
                        resetButton.setOnClickListener {
                            startPauseButton.text = "START"
                            timerRunning = false
                            startPauseButton.visibility = View.VISIBLE
                            timeChangeButton.visibility = View.VISIBLE
                            resetButton.visibility = View.INVISIBLE
                            resetButton.setImageResource(R.drawable.ic_replay)
                            countDownTimer.cancel()
                            timeLeftInMillis =
                                ((nPickerHour.value * 3600000) + (nPickerMinute.value * 60000) + nPickerSecond.value * 1000).toLong()

                            updateCountDownText(timeLeftInMillis)
                            alarm.stop()
                        }


                    }

                }
                countDownTimer.start()
            }
        }

        // button which will display number pickers to choose new value for the timer
        timeChangeButton.setOnClickListener {
            if (timerRunning) {
                countDownTimer.cancel()
                timerRunning = false
                startPauseButton.text = "START"
            }
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.INVISIBLE
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE

        }

        // button which will reset timer value and update countdown textview
        resetButton.setOnClickListener {
            resetButton.visibility = View.INVISIBLE
            if (timerRunning) {
                countDownTimer.cancel()
                timerRunning = false
                startPauseButton.text = "START"
            }

            timeLeftInMillis =
                ((nPickerHour.value * 3600000) + (nPickerMinute.value * 60000) + nPickerSecond.value * 1000).toLong()

            updateCountDownText(timeLeftInMillis)
        }

        updateCountDownText(timeLeftInMillis)
    }

    // method for updating countdown textview
    private fun updateCountDownText(timeLeftInMillis: Long) {
        val hours: Int = (timeLeftInMillis / 3600000).toInt()
        val minutes: Int = ((timeLeftInMillis / 60000) % 60).toInt()
        val seconds: Int = ((timeLeftInMillis / 1000) % 60 % 60).toInt()
        val timeLeftFormatted: String =
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        textViewCountDown.text = timeLeftFormatted
    }

}
