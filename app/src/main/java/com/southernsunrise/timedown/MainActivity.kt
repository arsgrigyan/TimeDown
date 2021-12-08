package com.southernsunrise.timedown

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.media.MediaPlayer
import android.os.Build
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import kotlin.concurrent.timer


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
    lateinit var progressBar: ProgressBar
    var timerRunning: Boolean = false
    var alarmPlaying: Boolean = false
    private var mainContainerVisible: Boolean = false
    private var pickerVisible: Boolean = true
    var timeLeftInMillis: Long = 0
    var wholeTimerValueInMillis: Long = 0
    lateinit var alarm: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null

        progressBar = findViewById<ProgressBar>(R.id.progress)
        progressBar.progress = 100

        nPickerHour = findViewById(R.id.hour_number_picker)
        nPickerMinute = findViewById(R.id.minute_number_picker)
        nPickerSecond = findViewById(R.id.second_number_picker)
        confirmButton = findViewById(R.id.confirm_button)
        timeChangeButton = findViewById(R.id.time_change_button)
        // infoButton = findViewById(R.id.btn_info)

        nPickerHour.minValue = 0
        nPickerHour.maxValue = 99
        nPickerMinute.minValue = 0
        nPickerMinute.maxValue = 59
        nPickerSecond.minValue = 0
        nPickerSecond.maxValue = 59


        //button which will take number pickers' values and update countDown textview
        confirmButton.setOnClickListener {
            if ((nPickerHour.value != nPickerHour.minValue) || (nPickerMinute.value != nPickerMinute.minValue) || (nPickerSecond.value != nPickerSecond.minValue)) {
                findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.VISIBLE
                mainContainerVisible = true
                findViewById<LinearLayout>(R.id.picker_layout).visibility = View.INVISIBLE
                pickerVisible = false
                progressBar.progress = 100
                wholeTimerValueInMillis =
                    ((nPickerHour.value * 3600000) + (nPickerMinute.value * 60000) + nPickerSecond.value * 1000).toLong()
                timeLeftInMillis = wholeTimerValueInMillis
                updateCountDownText(timeLeftInMillis)
            } else {
                Toast.makeText(this, "Choose valid time", Toast.LENGTH_SHORT).show()
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
            mainContainerVisible = false
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.VISIBLE
            pickerVisible = true
            resetButton.visibility = View.INVISIBLE
            progressBar.progress = 100

        }


        startPauseButton = findViewById(R.id.start_pause_button)
        resetButton = findViewById(R.id.reset_button)
        textViewCountDown = findViewById(R.id.countDown_text)


        startPauseButton.setOnClickListener {

            if (timerRunning) {
                countDownTimer.cancel()
                timerRunning = false
                startPauseButton.text = "START"
            } else {
                startPauseButton.text = "PAUSE"
                timerRunning = true
                startTimer()
                resetButton.visibility = View.VISIBLE

            }

        }

        // button which will reset timer value stop alarm and update countdown textview
        resetButton.setOnClickListener {
            if (timerRunning) {
                countDownTimer.cancel()
            } else if (alarmPlaying) {
                alarm.stop()
                startPauseButton.visibility = View.VISIBLE
                timeChangeButton.visibility = View.VISIBLE
                resetButton.setImageResource(R.drawable.ic_replay)
                alarmPlaying = false
            }

            resetButton.visibility = View.INVISIBLE
            startPauseButton.text = "START"
            timerRunning = false
            progressBar.progress = 100
            timeLeftInMillis = wholeTimerValueInMillis
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

    // method for updating progressbar's progress
    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateProgressBar() {
        progressBar.setProgress(
            (timeLeftInMillis * 100 / wholeTimerValueInMillis).toInt(),
            true
        )
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onTick(millisUntilFinished: Long) {
                // every second update timeLeftInMillis to millisUntilFinished update countdown textView and update progress bar's progress
                timeLeftInMillis = millisUntilFinished
                updateProgressBar()
                updateCountDownText(timeLeftInMillis)

            }

            override fun onFinish() {
                alarm = MediaPlayer.create(
                    baseContext,
                    R.raw.alarmsound
                )
                alarm.start()
                progressBar.progress = 0
                startPauseButton.visibility = View.INVISIBLE
                timerRunning = false
                alarmPlaying = true
                startPauseButton.text = "START"
                alarm.isLooping = true
                timeChangeButton.visibility = View.INVISIBLE
                resetButton.setImageResource(R.drawable.ic_stop)


            }

        }
        countDownTimer.start()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                if (timerRunning || alarmPlaying) {

                } else {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    // saving activity state with shared preferences onStop
    override fun onStop() {
        super.onStop()
        val sharedPrefs: SharedPreferences = getSharedPreferences("save_state", 0)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        if (alarmPlaying) {
            timeLeftInMillis = wholeTimerValueInMillis
        }
        editor.putLong("wholeTimerValueInMillis", wholeTimerValueInMillis)
        editor.putBoolean("timerRunning", timerRunning)
        editor.putLong("timeLeftInMillis", timeLeftInMillis)
        editor.putInt("hourPicker", nPickerHour.value)
        editor.putInt("minutePicker", nPickerMinute.value)
        editor.putInt("secondPicker", nPickerSecond.value)
        editor.putBoolean("mainContainerVisible", mainContainerVisible)
        editor.putBoolean("pickerVisible", pickerVisible)
        editor.putBoolean("alarmPlaying", alarmPlaying)
        editor.putBoolean("resetButtonVisible", resetButton.isVisible)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        val sharedPrefs: SharedPreferences = getSharedPreferences("save_state", 0)
        wholeTimerValueInMillis = sharedPrefs.getLong("wholeTimerValueInMillis", 0)
        timerRunning = sharedPrefs.getBoolean("timerRunning", false)
        timeLeftInMillis = sharedPrefs.getLong("timeLeftInMillis", 0)
        nPickerHour.value = sharedPrefs.getInt("hourPicker", 0)
        nPickerMinute.value = sharedPrefs.getInt("minutePicker", 0)
        nPickerSecond.value = sharedPrefs.getInt("secondPicker", 0)
        mainContainerVisible = sharedPrefs.getBoolean("mainContainerVisible", false)
        pickerVisible = sharedPrefs.getBoolean("pickerVisible", false)
        alarmPlaying = sharedPrefs.getBoolean("alarmPlaying", false)
        alarmPlaying = sharedPrefs.getBoolean("alarmPlaying", false)

        resetButton.isVisible = sharedPrefs.getBoolean("resetButtonVisible", false)
        updateCountDownText(timeLeftInMillis)
        if (mainContainerVisible) {
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.INVISIBLE
            updateProgressBar()
        }
        if (pickerVisible) {
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.INVISIBLE
        }

    }


    // saving activity state on orientation change
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (alarmPlaying) {
            alarm.stop()
            timeLeftInMillis = wholeTimerValueInMillis
            progressBar.progress = 100
        }
        outState.putBoolean("timerRunning", timerRunning)
        outState.putLong("timeLeftInMillis", timeLeftInMillis)
        outState.putLong("wholeTimerValueInMillis", wholeTimerValueInMillis)
        outState.putInt("hourPicker", nPickerHour.value)
        outState.putInt("minutePicker", nPickerMinute.value)
        outState.putInt("secondPicker", nPickerSecond.value)
        outState.putBoolean("mainContainerVisible", mainContainerVisible)
        outState.putBoolean("pickerVisible", pickerVisible)
        outState.putBoolean("alarmPlaying", alarmPlaying)
        if (timerRunning) {
            countDownTimer.cancel()
            timerRunning = false
        }


    }

    // getting saved variables' values and giving them to corresponding variables, changing picker and main container visibilities
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        timerRunning = savedInstanceState.getBoolean("timerRunning")
        wholeTimerValueInMillis = savedInstanceState.getLong("wholeTimerValueInMillis")
        timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis")
        nPickerHour.value = savedInstanceState.getInt("hourPicker")
        nPickerMinute.value = savedInstanceState.getInt("minutePicker")
        nPickerSecond.value = savedInstanceState.getInt("secondPicker")
        mainContainerVisible = savedInstanceState.getBoolean("mainContainerVisible")
        alarmPlaying = savedInstanceState.getBoolean("alarmPlaying")
        pickerVisible = savedInstanceState.getBoolean("pickerVisible")
        updateCountDownText(timeLeftInMillis)

        resetButton.visibility = View.VISIBLE

        if (alarmPlaying) {
            updateProgressBar()
        }

        if (timerRunning) {
            startTimer()

            startPauseButton.text = "PAUSE"
            timerRunning = true
        }
        if (timeLeftInMillis == wholeTimerValueInMillis) {
            resetButton.visibility = View.INVISIBLE
        }
        when (mainContainerVisible) {
            true -> {
                findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.VISIBLE
            }
            else -> findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.INVISIBLE

        }
        when (pickerVisible) {
            true -> {
                findViewById<LinearLayout>(R.id.picker_layout).visibility = View.VISIBLE
            }
            else -> findViewById<LinearLayout>(R.id.picker_layout).visibility = View.INVISIBLE
        }

    }


}



