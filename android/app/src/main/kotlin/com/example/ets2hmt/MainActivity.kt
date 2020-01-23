package com.example.ets2hmt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Bundle

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import io.flutter.plugin.common.MethodChannel
import java.lang.reflect.Type
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread


enum class VoiceCommand(val command: String) {
    SETTINGS("show settings"),

    THROTTLE("throttle"),
    BRAKE("brake"),
    REVERSE("reverse"),
    STEERING_LEFT("steer left"),
    STEERING_RIGHT("steer right"),
    SHIFT_UP("shift up"),
    SHIFT_DOWN("shift down"),
    SHIFT_TO_NEUTRAL("neutral"),
    SHIFT_UP_HINT("shift up hint"),
    SHIFT_DOWN_HINT("shift down hint"),
    GEARBOX_SWITCH_AUTOMATIC_SEQUENTIAL("switch gearbox mode"),
    START_ENGINE("start engine"),
    STOP_ENGINE("stop engine"),
    START_ENGINE_ELECTRICITY("start electricity"),
    STOP_ENGINE_ELECTRICITY("stop electricity"),
    PARKING_BRAKE("parking brake"),
    ENGINE_BRAKE_TOGGLE("toggle engine brake"),
    ENGINE_BRAKE_INCREASE("increase engine brake"),
    ENGINE_BRAKE_DECREASE("decrease engine brake"),
    TRAILER_BRAKE("trailer brake"),
    RETARDER_INCREASE("increase retarder"),
    RETARDER_DECREASE("decrease retarder"),
    LIFT_AXLE("lift axle"),
    DROP_AXLE("drop axle"),
    LIFT_TRAILER_AXLE("lift trailer axle"),
    DROP_TRAILER_AXLE("drop trailer axle"),
    DIFFERENTIAL_LOCK("lock differential"),
    LEFT_TURN_INDICATOR("left blinker"),
    RIGHT_TURN_INDICATOR("right blinker"),
    HAZARD_WARNING("hazard warning"),
    LIGHT_MODES("switch lights"),
    HIGH_BEAM_HEADLIGHTS("high beam"),
    BEACON("beacon"),
    HORN("horn"),
    AIR_HORN("air horn"),
    LIGHT_HORN("light horn"),
    WIPERS("wipers"),
    WIPERS_BACK("wipers back"),
    CRUISE_CONTROL("cruise control"),
    CRUISE_CONTROL_SPEED_INCREASE("increase cruise control"),
    CRUISE_CONTROL_SPEED_DECREASE("decrease cruise control"),
    CRUISE_CONTROL_RESUME("resume cruise control"),
    DASHBOARD_DISPLAY_MODE("dashboard mode"),
    DASHBOARD_MAP_MODE("map mode"),
    SHOW_SIDE_MIRRORS("show side mirrors"),
    HIDE_SIDE_MIRRORS("hide side mirrors"),
    ROUTE_ADVISOR_MODES("route advisor mode"),
    TRUCK_ADJUSTMENT("truck adjustment"),
    ROUTE_ADVISOR_MOUSE_CONTROL("route advisor mouse control"),
    ROUTE_ADVISOR_NAVIGATION_PAGE("route advisor navigation page"),
    ROUTE_ADVISOR_JOB_INFO_PAGE("route advisor job info page"),
    ROUTE_ADVISOR_DIAGNOSTICS_PAGE("route advisor diagnostics page"),
    ROUTE_ADVISOR_INFO_PAGE("route advisor info page"),
    ROUTE_ADVISOR_NEXT_PAGE("route advisor next page"),
    ROUTE_ADVISOR_PREVIOUS_PAGE("route advisor previous page"),
    ASSISTANT_ACTION_1("assistant action one"),
    ASSISTANT_ACTION_2("assistant action two"),
    ASSISTANT_ACTION_3("assistant action three"),
    ASSISTANT_ACTION_4("assistant action four"),
    ASSISTANT_ACTION_5("assistant action five"),
    NEXT_CAMERA("next camera"),
    INTERIOR_CAMERA("interior camera"),
    CHASING_CAMERA("chasing camera"),
    TOP_DOWN_CAMERA("top down camera"),
    ROOF_CAMERA("roof camera"),
    LEAN_OUT_CAMERA("lean out camera"),
    BUMPER_CAMERA("bumper camera"),
    ON_WHEEL_CAMERA("on-wheel camera"),
    DRIVE_BY_CAMERA("drive-by camera"),
    ROTATE_CAMERA("rotate camera"),
    ZOOM_INTERIOR_CAMERA("zoom interior camera"),
    LOOK_LEFT("look left"),
    LOOK_RIGHT("look right"),
    INTERIOR_LOOK_FORWARD("interior look forward"),
    INTERIOR_LOOK_UP_RIGHT("interior look up right"),
    INTERIOR_LOOK_UP_LEFT("interior look up left"),
    INTERIOR_LOOK_RIGHT("interior look right"),
    INTERIOR_LOOK_LEFT("interior look left"),
    INTERIOR_LOOK_UP_MIDDLE("interior look up middle"),
    STEERING_BASED_CAMERA_ROTATION("steering camera"),
    BLINKER_BASED_CAMERA_ROTATION("blinker camera"),
    ACTIVATE("activate"),
    ATTACH_TRAILER("attach trailer"),
    DETACH_TRAILER("detach trailer"),
    SHOW_MENU("show menu"),
    HIDE_MENU("hide menu"),
    QUICK_SAVE("quick save"),
    QUICK_LOAD("quick load"),
    AUDIO_PLAYER("audio player"),
    AUDIO_PLAYER_NEXT_FAVORITE("next favorite"),
    AUDIO_PLAYER_PREVIOUS_FAVORITE("previous favorite"),
    AUDIO_PLAYER_VOLUME_UP("increase volume"),
    AUDIO_PLAYER_VOLUME_DOWN("decrease volume"),
    WORLD_MAP_SHOW("show map"),
    WORLD_MAP_HIDE("hide map"),
    GARAGE_MANAGER("garage manager"),
    SCREENSHOT("take screenshot"),
    RESET_HEAD("reset head"),
    PAUSE_EXTENDED_VIEW("pause extended view");

    companion object {
        private val map = values().associateBy(VoiceCommand::command)
        fun getByCommand(command: String) = map[command]
    }
}

class MainActivity : FlutterActivity(), SensorEventListener {
    private val CHANNEL = "skyggedans.com/ets2hmt"

    private val ACTION_OVERRIDE_COMMANDS = "com.realwear.wearhf.intent.action.OVERRIDE_COMMANDS"
    private val ACTION_SPEECH_EVENT = "com.realwear.wearhf.intent.action.SPEECH_EVENT"
    private val ACTION_RESTORE_COMMANDS = "com.realwear.wearhf.intent.action.RESTORE_COMMANDS"
    private val EXTRA_SOURCE_PACKAGE = "com.realwear.wearhf.intent.extra.SOURCE_PACKAGE"
    private val EXTRA_COMMANDS = "com.realwear.wearhf.intent.extra.COMMANDS"
    private val EXTRA_RESULT = "command"

    private val SEND_RAW: Byte = 0x01
    private val SEND_ORIENTATION: Byte = 0x02
    private val SEND_NONE: Byte = 0x00

    private var acc = floatArrayOf(0f, 0f, 0f)
    private var mag = floatArrayOf(0f, 0f, 0f)
    private var gyr = floatArrayOf(0f, 0f, 0f)
    private var imu = floatArrayOf(0f, 0f, 0f)
    private var voiceCommand: String = ""

    private var rotationVector = FloatArray(3)
    private val rotationMatrix = FloatArray(16)

    private lateinit var sensorManager: SensorManager
    private lateinit var wifiManager: WifiManager
    private lateinit var handler: Handler
    private lateinit var channel: MethodChannel

    private var imuSocket: DatagramSocket? = null
    private var voiceSocket: DatagramSocket? = null
    private var deviceIndex: Byte = 0
    private var sendOrientation: Boolean = true
    private var sendRawData: Boolean = true
    private var sampleRate: Int = 0
    private var imuWorker: Thread? = null
    private var voiceWorker: Thread? = null

    private var wifiLock: WifiManager.WifiLock? = null
    private val imuLock = java.lang.Object()
    private val imuPacket = DatagramPacket(byteArrayOf(), 0)
    private val voiceLock = java.lang.Object()
    private val voicePacket = DatagramPacket(byteArrayOf(), 0)

    @Volatile
    internal var running: Boolean = false

    private val asrBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == ACTION_SPEECH_EVENT) {
                synchronized(voiceLock) {
                    val asrCommand = intent.getStringExtra(EXTRA_RESULT)

                    channel.invokeMethod("onSpeechEvent", mapOf("command" to asrCommand))

                    try {
                        voiceCommand = VoiceCommand.getByCommand(asrCommand)!!.name
                    } catch (e: Throwable) {
                        Log.i("Info", "Unknown speech command " + asrCommand + " ignoring")
                    }

                    voiceLock.notifyAll()
                }
            }

            sendCommands()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        GeneratedPluginRegistrant.registerWith(this)
        channel = MethodChannel(flutterView, CHANNEL)

        channel.setMethodCallHandler { call, result ->
            if (call.method == "start") {
                val ip: String = call.argument<String>("targetIp") ?: "192.168.1.1"
                val openTrackPort: Int = call.argument<Int>("openTrackPort") ?: 5555
                val voiceControlPort: Int = call.argument<Int>("voiceControlPort") ?: 5555
                val deviceIndex: Byte = call.argument<Int>("deviceIndex")?.toByte() ?: 0
                val sampleRate: Int = call.argument<Int>("sampleRate") ?: 0
                val sendOrientation: Boolean = call.argument<Boolean>("sendOrientation") ?: true
                val sendRawData: Boolean = call.argument<Boolean>("sendRawData") ?: true

                start(ip, openTrackPort, voiceControlPort, deviceIndex, sampleRate, sendOrientation, sendRawData)
                result.success(null)
            } else if (call.method == "stop") {
                stop()
                result.success(null)
            } else {
                result.notImplemented()
            }
        }

        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        handler = Handler()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            View.SYSTEM_UI_FLAG_FULLSCREEN
            View.SYSTEM_UI_FLAG_IMMERSIVE
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
        unregisterReceiver(asrBroadcastReceiver);
        restoreCommands()
    }

    override fun onResume() {
        super.onResume()

        registerSensors()
        registerReceiver(asrBroadcastReceiver, IntentFilter(ACTION_SPEECH_EVENT))

        handler.postDelayed(Runnable { sendCommands() }, 300)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        synchronized(imuLock) {
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> System.arraycopy(sensorEvent.values, 0, acc, 0, 3)
                Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(sensorEvent.values, 0, mag, 0, 3)
                Sensor.TYPE_GYROSCOPE -> System.arraycopy(sensorEvent.values, 0, gyr, 0, 3)
                Sensor.TYPE_ROTATION_VECTOR -> System.arraycopy(sensorEvent.values, 0, rotationVector, 0, 3)
            }

            if (sendOrientation) {
                val rotationMatrixTransformed = FloatArray(16)

                SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X, rotationMatrixTransformed)
                SensorManager.getOrientation(rotationMatrixTransformed, imu)
            }

            imuLock.notifyAll()
        }
    }

    private fun registerSensors() {
        sensorManager.unregisterListener(this)

        if (sendRawData) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    sampleRate)

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    sampleRate)

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    sampleRate)
        }

        if (sendOrientation) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    sampleRate)
        }
    }

    private fun start(ip: String, openTrackPort: Int, voiceControlPort: Int, deviceIndex: Byte, sampleRate: Int, sendOrientation: Boolean, sendRawData: Boolean) {
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "ETS2HMT_WIFI_LOCK")
        running = true

        startImu(ip, openTrackPort, deviceIndex, sampleRate, sendOrientation, sendRawData)
        startVoice(ip, voiceControlPort)

        wifiLock?.acquire()
    }

    private fun stop() {
        if (wifiLock != null) {
            wifiLock?.release()
            wifiLock = null
        }

        running = false

        stopImu()
        stopVoice()
    }

    private fun startImu(ip: String, port: Int, deviceIndex: Byte, sampleRate: Int, sendOrientation: Boolean, sendRawData: Boolean) {
        this.deviceIndex = deviceIndex
        this.sampleRate = sampleRate
        this.sendOrientation = sendOrientation
        this.sendRawData = sendRawData

        imuWorker = thread {
            imuSocket = DatagramSocket()

            try {
                imuPacket.address = InetAddress.getByName(ip)
                imuPacket.port = port
            } catch (e: Exception) {
                Log.e("Error", "Can't create endpoint " + e.message + " " + ip + ":" + port)
                //running = false
            }

            while (running) {
                synchronized(imuLock) {
                    imuLock.wait()
                    sendImu()
                }
            }

            imuSocket?.disconnect()
        }
    }

    private fun stopImu() {
        synchronized(imuLock) {
            imuLock.notifyAll()
        }

        if (imuWorker != null) {
            imuWorker?.join()
        }
    }

    private fun startVoice(ip: String, port: Int) {
        voiceWorker = thread {
            voiceSocket = DatagramSocket()

            try {
                voicePacket.address = InetAddress.getByName(ip)
                voicePacket.port = port
            } catch (e: Exception) {
                Log.e("Error", "Can't create endpoint " + e.message + " " + ip + ":" + port)
                //running = false
            }

            while (running) {
                synchronized(voiceLock) {
                    voiceLock.wait()

                    val commandArray = voiceCommand.toByteArray()
                    voicePacket.setData(commandArray, 0, commandArray.size)
                    voiceSocket?.send(voicePacket)
                }
            }

            voiceSocket?.disconnect()
        }

        sendCommands()
    }

    private fun stopVoice() {
        synchronized(voiceLock) {
            voiceLock.notifyAll()
        }

        if (voiceWorker != null) {
            voiceWorker?.join()
        }

        restoreCommands()
    }

    private fun sendCommands() {
        val asrIntent = Intent(ACTION_OVERRIDE_COMMANDS)

        asrIntent.putExtra(EXTRA_SOURCE_PACKAGE, this.packageName)
        asrIntent.putExtra(EXTRA_COMMANDS, VoiceCommand.values().map { voiceCommand -> voiceCommand.command }.joinToString(separator = "|"))
        //asrIntent.putExtra(EXTRA_COMMANDS, "start engine|show settings|light modes|high beam headlights|beacon|horn|air horn|light horn")

        this.sendBroadcast(asrIntent)
    }

    private fun restoreCommands() {
        val intent = Intent(ACTION_RESTORE_COMMANDS)

        intent.putExtra(EXTRA_SOURCE_PACKAGE, packageName)
        sendBroadcast(intent)
    }

    private fun getFlagByte(raw: Boolean, orientation: Boolean): Byte {
        return ((if (raw) SEND_RAW else SEND_NONE).toInt() or
                (if (orientation) SEND_ORIENTATION else SEND_NONE).toInt()).toByte()
    }

    private fun putFloat(f: Float, pos: Int, buf: ByteArray): Int {
        var posTmp = pos
        val tmpF = java.lang.Float.floatToIntBits(f)

        buf[posTmp++] = (tmpF shr 0).toByte()
        buf[posTmp++] = (tmpF shr 8).toByte()
        buf[posTmp++] = (tmpF shr 16).toByte()
        buf[posTmp++] = (tmpF shr 24).toByte()

        return posTmp
    }

    private fun sendImu() {
        val buf = ByteArray(50)
        var pos = 0

        buf[pos++] = deviceIndex
        buf[pos++] = getFlagByte(sendRawData, sendOrientation)

        if (sendRawData) {
            //Acc
            pos = putFloat(acc[0], pos, buf)
            pos = putFloat(acc[1], pos, buf)
            pos = putFloat(acc[2], pos, buf)

            //Gyro
            pos = putFloat(gyr[0], pos, buf)
            pos = putFloat(gyr[1], pos, buf)
            pos = putFloat(gyr[2], pos, buf)

            //Mag
            pos = putFloat(mag[0], pos, buf)
            pos = putFloat(mag[1], pos, buf)
            pos = putFloat(mag[2], pos, buf)
        }

        if (sendOrientation) {
            pos = putFloat(imu[0], pos, buf)
            pos = putFloat(imu[1], pos, buf)
            pos = putFloat(imu[2], pos, buf)
        }

        imuPacket.setData(buf, 0, pos)
        imuSocket?.send(imuPacket)
    }
}
