package com.southernsunrise.timedown

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.RingtoneManager.TYPE_ALARM
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.os.Build
import android.view.*
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import kotlin.concurrent.timer


class MainActivity : AppCompatActivity() {

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
    private var mainCounterScreenVisible: Boolean = false
    private var pickerVisible: Boolean = true
    var wholeTimerValueInMillis: Long = 0
    var timeLeftInMillis: Long = wholeTimerValueInMillis

    lateinit var alarmManager: AlarmManager
    lateinit var pendingIntent: PendingIntent
    lateinit var alarmRingtone: Ringtone

    lateinit var sharedPrefs: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null

        //getting saved wholeTimerValueInMillis's value after last app closing and giving it to timeLeftInMills, saving timeLeftInMillis's value with shared prefs |
        // This way, timer's value of that closing moment won't be saved and timer will be reloaded
        sharedPrefs =
            getSharedPreferences("shared_pref", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        mainCounterScreenVisible = sharedPrefs.getBoolean("mainCounterScreenVisible", false)
        pickerVisible = sharedPrefs.getBoolean("pickerVisible", true)
        wholeTimerValueInMillis =
            sharedPrefs.getLong("wholeTimerValueInMillis", wholeTimerValueInMillis)
        timeLeftInMillis = wholeTimerValueInMillis
        editor.putLong("timeLeftInMillis", timeLeftInMillis)
        editor.apply()


        // declaring the alarm ringtone
        alarmRingtone = RingtoneManager.getRingtone(
            applicationContext,
            RingtoneManager.getDefaultUri(TYPE_ALARM)
        )

        progressBar = findViewById(R.id.progress)
        progressBar.progress = 100

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


        //button which will take number pickers' values and update countDown textview
        confirmButton.setOnClickListener {
            if ((nPickerHour.value != nPickerHour.minValue) || (nPickerMinute.value != nPickerMinute.minValue) || (nPickerSecond.value != nPickerSecond.minValue)) {
                findViewById<LinearLayout>(R.id.picker_layout).visibility = View.INVISIBLE
                pickerVisible = false
                findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.VISIBLE
                mainCounterScreenVisible = true
                progressBar.progress = 100
                wholeTimerValueInMillis =
                    ((nPickerHour.value * 3600000) + (nPickerMinute.value * 60000) + nPickerSecond.value * 1000).toLong()
                timeLeftInMillis = wholeTimerValueInMillis
                updateCountDownText(timeLeftInMillis)
                mainCounterScreenVisible = true
                pickerVisible = false
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
                mainCounterScreenVisible = false
                pickerVisible = true
                cancelAlarm()
            }
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.INVISIBLE
            mainCounterScreenVisible = false
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.VISIBLE
            pickerVisible = true
            resetButton.visibility = View.INVISIBLE
            progressBar.progress = 100

        }


        startPauseButton = findViewById(R.id.start_pause_button)
        resetButton = findViewById(R.id.reset_button)
        textViewCountDown = findViewById(R.id.countDown_text)


        startPauseButton.setOnClickListener {
            resetButton.setOnClickListener {

                countDownTimer.cancel()
                cancelAlarm()
                timerRunning = false

                resetButton.visibility = View.INVISIBLE
                startPauseButton.text = "START"
                progressBar.progress = 100
                timeLeftInMillis = wholeTimerValueInMillis
                updateCountDownText(timeLeftInMillis)
            }

            if (timerRunning) {
                countDownTimer.cancel()
                timerRunning = false
                startPauseButton.text = "START"
                cancelAlarm()
            } else {
                startTimer()
                setAlarm()
                startPauseButton.text = "PAUSE"
                timerRunning = true
                resetButton.visibility = View.VISIBLE

            }


        }

        updateCountDownText(timeLeftInMillis)
        createNotificationChannel()

    }

    // creating the notification channel
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "TimeDownReminderChannel"
            val description = "Timer state"
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TimeDown", name, importance)
            channel.description = description
            channel.setSound(null, null)


            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }

    }

    // method for setting alarm to get notification when countDownTimer is finished
    private fun setAlarm() {

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + timeLeftInMillis,
            pendingIntent
        )

    }

    // method to cancel the alarm
    private fun cancelAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        if (alarmManager == null) {
            alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        alarmManager.cancel(pendingIntent)
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

    // method which will be called when start button is clicked
    private fun startTimer() {

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {


            @RequiresApi(Build.VERSION_CODES.N)
            override fun onTick(millisUntilFinished: Long) {
                // every second update timeLeftInMillis to millisUntilFinished update countdown textView and update progress bar's progress
                timeLeftInMillis = millisUntilFinished
                updateCountDownText(timeLeftInMillis)
                updateProgressBar()

            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onFinish() {

                alarmRingtone.play()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    alarmRingtone.isLooping = true
                }

                alarmPlaying = true
                timerRunning = false
                updateCountDownText(0)
                progressBar.progress = 0
                timeLeftInMillis = wholeTimerValueInMillis
                startPauseButton.visibility = View.INVISIBLE
                timeChangeButton.visibility = View.INVISIBLE
                resetButton.setImageResource(R.drawable.ic_stop)
                resetButton.visibility = View.VISIBLE


                // setting onCLickListener on resetButton when countDownTimer is finished
                resetButton.setOnClickListener {
                    //alarmSound.stop()
                    alarmRingtone.stop()
                    startPauseButton.visibility = View.VISIBLE
                    timeChangeButton.visibility = View.VISIBLE
                    resetButton.visibility = View.INVISIBLE
                    resetButton.setImageResource(R.drawable.ic_replay)
                    timeLeftInMillis = wholeTimerValueInMillis
                    updateCountDownText(wholeTimerValueInMillis)
                    startPauseButton.text = "START"
                    updateProgressBar()
                    alarmPlaying = false
                    timerRunning = false

                }


            }

        }
        countDownTimer.start()
    }

    // saving necessary variables when app is stopped
    override fun onStop() {
        super.onStop()
        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putBoolean("mainCounterScreenVisible", mainCounterScreenVisible)
        editor.putBoolean("pickerVisible", pickerVisible)
        editor.putLong("wholeTimerValueInMillis", wholeTimerValueInMillis)
        editor.putBoolean("timerRunning", timerRunning)
        editor.putLong("timeLeftInMillis", timeLeftInMillis)
        editor.putInt("hourPicker", nPickerHour.value)
        editor.putInt("minutePicker", nPickerMinute.value)
        editor.putInt("secondPicker", nPickerSecond.value)
        editor.putBoolean("alarmPlaying", alarmPlaying)
        editor.apply()
    }

    // giving saved values to corresponding variables when app is resumed
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        mainCounterScreenVisible =
            sharedPrefs.getBoolean("mainContainerVisible", mainCounterScreenVisible)
        pickerVisible = sharedPrefs.getBoolean("pickerVisible", pickerVisible)
        wholeTimerValueInMillis = sharedPrefs.getLong("wholeTimerValueInMillis", 0)
        timerRunning = sharedPrefs.getBoolean("timerRunning", timerRunning)
        timeLeftInMillis = sharedPrefs.getLong("timeLeftInMillis", 0)
        nPickerHour.value = sharedPrefs.getInt("hourPicker", 0)
        nPickerMinute.value = sharedPrefs.getInt("minutePicker", 0)
        nPickerSecond.value = sharedPrefs.getInt("secondPicker", 0)
        alarmPlaying = sharedPrefs.getBoolean("alarmPlaying", false)


        // changing mainCounterScreen and picker visibilities
        if (mainCounterScreenVisible) {
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.INVISIBLE
        } else if (pickerVisible) {
            findViewById<LinearLayout>(R.id.main_counter_screen).visibility = View.INVISIBLE
            findViewById<LinearLayout>(R.id.picker_layout).visibility = View.VISIBLE
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}






