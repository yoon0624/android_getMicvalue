package com.example.yoon.mictest_debug

// 금요일에는 AudioDecord 라이브러리를 이용해서 코드를 짜보자 ..
// 그리고 rx이용해서 mediaRecord의 변수변화로 옵저버블생성이 안되나..?ㅠㅠㅠㅠㅠㅠㅠㅠㅠㅠㅠ
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.*
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
//    var dataBuffer: FloatArray? = null
//    lateinit var dataflow: TarsosDSPAudioInputStream    // Rx랑 다른건가??
//    var dispatcher: AudioDispatcher = AudioDispatcher(dataflow, 1024, 0)

    private val REQUEST_RECORD_AUDIO_PERMISSIONS = 200
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    lateinit var mediaRecorder: MediaRecorder //= MediaRecorder()

    private val mFileName: String = "/dev/null"
    var check: Boolean = false
    var mHandler: Handler? = null
    var interval: Long = 200    // 타이머

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //원래 함수에서 상속받는다
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //request Code에 따라서 케이스를 나누어 설정한다
        //여기서는 케이스가 녹음 권한 하나이기 때문에 하나만 있으면 된다
        when (requestCode) {
        // 녹음 권한일 때
            REQUEST_RECORD_AUDIO_PERMISSIONS ->

                //권한이 설정이 되었으면 두개가 같으므로 permissionToRecordAccepted에 true가 들어오게 된다
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        //만약 권한 설정이 되지 않았다면 끝낸다
        if (!permissionToRecordAccepted)
            finish()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var intent2 = getIntent()
        interval = intent2.extras.getInt("interval").toLong()

        Log.i("YoonTest:", "onCreate" +interval)
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSIONS)
        //var audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE)
        //var player: MediaPlayer = MediaPlayer.create(this, R.raw.ddok)
        setContentView(R.layout.activity_main)
        Log.i("YoonTest:", "onCreate_setContentView")
        init()
        Log.i("YoonTest", "Amplitude: " + mediaRecorder)
    }

    override fun onResume() {
        super.onResume()
        mHandler!!.sendEmptyMessageDelayed(0, interval)
    }

    override fun onStop() {
        super.onStop()
        Log.i("YoonTest:", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mediaRecorder!=null)
            mediaRecorder.release()
    }

    fun init() {
        // 미디어 리코더 실행
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setOutputFile(mFileName)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        mediaRecorder.prepare()
        mediaRecorder.start()
        check = true
        tv_state.text = "Recording..."
        //var b: Double = calc(getAmplitude())
        mHandler = object : Handler() { // 기능 동작 핸들러
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                var a: Int = getAmplitude()
                btn_record.setOnClickListener {
                    if (check) {
                        stopRec()
                        tv_state.text = "Stop"
//                player.start()  // 지정된 파일 실행

                    } else {
                        startRec()
                        tv_state.text = "Recording..."
                        //recording data
                        //Log.i("YoonTest","getNoiseLevelstop")
                        //array.add(mediaRecorder.maxAmplitude)
                    }
                }
                btn_move.setOnClickListener {
                    if(interval <1000)
                        interval = interval +100
                    else
                        interval = 100
                    tv_interval.text = interval.toString()
                }

                tv_result.text = a.toString()
                tv_interval.text = "Timer(ms): "+ interval.toString()
//                tv_convert.text = calc(a).toString()
//                tv_stack_convert.text = b.toString()

                Log.i("YoonTest", "handleMessage::" + a)
                if(check == true) {
                    mHandler!!.sendEmptyMessageDelayed(0, interval)  //딜레이 ms
                }
            }
        }
    }

    public fun startRec() {
//        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setOutputFile(mFileName)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        //미디어 레코드 객체 생성
        Log.i("YoonTest Main", "StartRec")
        try {
            Log.i("YoonTest Main:", "startRec_try")
            mediaRecorder.prepare()
            mediaRecorder.start()
            Log.i("YoonTest", "Amplitude: " + mediaRecorder.maxAmplitude)
            tv_result.text = getAmplitude().toString()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            check = true
            mHandler!!.sendEmptyMessage(0)
        }
    }
    fun stopRec() {
        Log.i("YoonTest", "Amplitude: " + mediaRecorder.maxAmplitude)
        Log.i("YoonTest Main::", "StopRec")
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
        } catch(e: Exception){
            e.printStackTrace()
        }
        finally {
            check = false
        }

    }
    fun getAmplitude(): Int {
        var value: Int = mediaRecorder.getMaxAmplitude()
        return value
    }

   /* fun calc(value: Int): Double{// getAmplitude값을 변환시켜보자?
        //근데 전압계를 달면 또 값이 변해서 측정이어려움..
        var result: Double = (value/32767.0)*1000 + 15
        return result
    }*/
}
