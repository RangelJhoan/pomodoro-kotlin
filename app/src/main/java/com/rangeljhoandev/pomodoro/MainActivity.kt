package com.rangeljhoandev.pomodoro

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.rangeljhoandev.pomodoro.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Indica si la cuenta regresiva está ejecutándose o no (se inicializa el estado en DETENIDO)
    private var isWorking = false

    // Indica cuál es el tiempo para hacer la cuenta regresiva (se inicializa en 25 minutos -POMODORO-)
    private var timeCountDown = EPomodoroState.POMODORO.time

    // Indica cuál es el tipo de cuenta regresiva del pomodoro (se inicializa en Pomodoro)
    private var currentPomodoroState = EPomodoroState.POMODORO

    private lateinit var soundPool: SoundPool
    private var soundClick = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpOnClickListeners()

        // Se inicializa la barra de estado con el color definido para el tipo POMODORO
        window.statusBarColor = ContextCompat.getColor(this, EPomodoroState.POMODORO.color)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundClick = soundPool.load(this, R.raw.click, 1)
    }

    /**
     * Establecemos los eventos onClick de los elementos UI
     *
     * @author RangelJhoanDev
     * @since 27/09/2024
     */
    private fun setUpOnClickListeners() {
        binding.tvPomodoro.setOnClickListener {
            updateUI(EPomodoroState.POMODORO)
        }

        binding.tvShortBreak.setOnClickListener {
            updateUI(EPomodoroState.SHORT_BREAK)
        }

        binding.tvLongBreak.setOnClickListener {
            updateUI(EPomodoroState.LONG_BREAK)
        }

        binding.btnStartStopPomodoro.setOnClickListener {
            soundPool.play(soundClick, 1f, 1f, 0, 0, 1f)
            toggleTimer()
        }
    }

    /**
     *  Método que maneja el cambio de estado de la cuenta regresiva y lo que esto implica
     *
     * @author RangelJhoanDev
     * @since 27/09/2024
     */
    private fun toggleTimer() {
        // Cambiamos al valor opuesto a la variable isWoking
        isWorking = !isWorking

        // Cambiamos al valor opuesto al texto del botón
        binding.btnStartStopPomodoro.text =
            if (isWorking) getString(R.string.stop).uppercase() else getString(R.string.start).uppercase()

        // Si se activa la cuenta regresiva, comenzamos a disminuir los segundos
        if (isWorking) {
            lifecycleScope.launch {
                // Si hay segundos para disminuir y la cuenta regresiva está activa, disminuimos los segundos
                while (timeCountDown >= 0) {
                    // Hacemos la pausa por segundo de la cuenta regresiva
                    delay(1000)

                    // En caso que ya haya entrado al ciclo y hayan detenido la cuenta regresiva, no afectamos el tiempo
                    if (!isWorking) {
                        break
                    }
                    // Disminuimos el valor al tiempo de la cuenta regresiva
                    timeCountDown--
                    // Actualizamos la UI con el nuevo tiempo
                    updateTimerText()
                }

                // Si no hay más segundos para disminuir, reiniciamos el tiempo
                if (timeCountDown < 0) {
                    resetTimer()
                }
            }
        }
    }

    /**
     * Método que detiene la cuenta regresiva y actualiza la UI para reiniciar parámetros
     *
     * @author RangelJhoanDev
     * @since 27/09/2024
     */
    private fun resetTimer() {
        isWorking = !isWorking
        updateUI(currentPomodoroState)
        binding.btnStartStopPomodoro.text = getString(R.string.start).uppercase()
    }

    /**
     * Método que actualiza el tiempo en pantalla
     *
     * @author RangelJhoanDev
     * @since 27/09/2024
     */
    private fun updateTimerText() {
        val minutes = (timeCountDown / 60).toString().padStart(2, '0')
        val seconds = (timeCountDown % 60).toString().padStart(2, '0')
        binding.tvTime.text = "$minutes:$seconds"
    }

    /**
     * Método que actualiza los componentes de la UI según el tipo de cuenta regresiva
     *
     * @author RangelJhoanDev
     * @since 27/09/2024
     */
    private fun updateUI(ePomodoroState: EPomodoroState) {
        currentPomodoroState = ePomodoroState

        binding.tvPomodoro.background = null
        binding.tvShortBreak.background = null
        binding.tvLongBreak.background = null

        var timeText = ""
        when (ePomodoroState) {
            EPomodoroState.POMODORO -> {
                timeText = "25:00"
                binding.tvPomodoro.background =
                    AppCompatResources.getDrawable(this, R.drawable.rounded_border)
            }

            EPomodoroState.SHORT_BREAK -> {
                timeText = "00:30"
                binding.tvShortBreak.background =
                    AppCompatResources.getDrawable(this, R.drawable.rounded_border)
            }

            EPomodoroState.LONG_BREAK -> {
                timeText = "01:00"
                binding.tvLongBreak.background =
                    AppCompatResources.getDrawable(this, R.drawable.rounded_border)
            }
        }

        window.statusBarColor = ContextCompat.getColor(this, currentPomodoroState.color)
        binding.clMainActivity.setBackgroundColor(
            ContextCompat.getColor(
                this,
                currentPomodoroState.color
            )
        )

        timeCountDown = currentPomodoroState.time
        binding.tvTime.text = timeText
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    /**
     * Enumerado con los diferentes tipos de cuenta regresiva y sus respectivas propiedades
     *
     * @author RangelJhoanDev
     * @since 27/09/2024
     */
    enum class EPomodoroState(val color: Int, val time: Int) {
        POMODORO(R.color.light_yellow, 1500),
        SHORT_BREAK(R.color.light_green, 30),
        LONG_BREAK(R.color.light_pink, 60)
    }

}