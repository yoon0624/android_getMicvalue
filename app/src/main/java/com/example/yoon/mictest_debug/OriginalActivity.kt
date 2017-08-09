package com.example.yoon.mictest_debug

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_original.*

class OriginalActivity : AppCompatActivity() {


    //미디어 레코더를 사용한다 마이크 사용을 위해
    private var mediaRecorder: MediaRecorder? = null

    //파일을 저장할 경로를 선택 여기서 /dev/null의 경우는 저장을 하지 않는다
    private val mFileName = "/dev/null"

    //현재 녹음중인 상태 표시 녹음 중이면 1, 아니면 0
    private var check = 0
    private val playCheck = 1

    //미디어 세션을 통해서도 제어할 수 있다고 했지만 실질적으로 사용하지는 않았다
    //private MediaSession mediaSession = null;

    //레코드 권한의 상태를 저장할 변수이다
    private var permissionToRecordAccepted = false

    //권한 설정을 위해 필요한 manifest를 permissions라는 문자열 변수에 저장한다
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    //현재 음악이 재생중이면 1, 정지상태이면 0
    private var controlHandler = 0


    //핸들러를 통해서 지속적인 마이크 모니터링을 하기 때문에 핸들러를 이용한다
    lateinit internal var mHandler: Handler
    internal var outHandler: Handler? = null

    lateinit internal var result: TextView


    lateinit internal var r: EarphoneButtonReceiver

    //권한요청에 대한 결과를 리턴한다.
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


    //브로드캐스트 리시버를 설정하는 부분이다 브로드캐스트를 설정해야 이벤트가 일어났을때 이벤트를 받을 수 있다
    //상속받는다
    inner class EarphoneButtonReceiver : BroadcastReceiver() {


        //onReceive를 오버라이드하여 새롭게 설정한다
        override fun onReceive(context: Context, intent: Intent) {

            abortBroadcast()

            val checkButton = findViewById(R.id.checkButton) as TextView
            val intentAction = intent.action

            //액션이 미디어 버튼에서 일어난게 아니라면 종료한다
            if (Intent.ACTION_MEDIA_BUTTON != intentAction) {
                //               if(!Intent.ACTION_HEADSET_PLUG.equals(intentAction))
                return
            }

            //EXTRA KEY EVENT에서 일어나는 키를 받아서 event에 저장한다
            //val event = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent ?: return
            //이벤트가 일어나지 않았으면 종료한다

            // abortBroadcast();
        }
    }


    //녹음을 하는 본격적인 부분이다 이 함수 하나로 녹음과 멈춤을 모두 하기 때문에 함수는 이걸로 모두 통제한다
    fun startRecording() {

        //result라는 텍스트뷰를 가져와서 저장한다
        val result = findViewById(R.id.result) as TextView

        //0이므로 녹음중이 아니면서 mediaRecorder가 비어있을때 -> 그냥 녹음중이 아닐때이다
        if (check == 0 && mediaRecorder == null) {

            //미디어레코드를 객체로 생성하여 저장하고
            mediaRecorder = MediaRecorder()

            //오디오를 가져올 곳을 마이크로 지정한다. 이외의 것들도 있는데 각각 성질이 다르다
            //MIC, VOICE_COMMUNICATION 등이 있다 각자 다른 수치를 표시한다
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)

            //파일을 만들게 되면 어떤식으로 출력할지 출력 형태를 지정한다 여기의 경우에는 3gp파일로 출력하게 만들었다
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            //파일 이름을 지정한다
            mediaRecorder!!.setOutputFile(mFileName)

            //오디오 인코더를 AMR_NB로 지정했다 정확한건 모르지만 이것에 따라서 음질저하가 일어나기도 한다고 한다
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)


            try {
                //녹음을 준비시키고
                mediaRecorder!!.prepare()
                //여기서 시작한다
                mediaRecorder!!.start()
            } catch (e: Exception) {
                //에러가 혹시나 일어나게 되면 에러를 표시하게 해준다
                e.printStackTrace()
            } finally {
                //에러가 일어나더라도 녹음을 시작했기 때문에 check=1로 해주었다 사실 이부분은 finally없이 try밑에 두는것이 맞을 것 같다
                //이렇게 두면 시작하기전에 오류가 나더라도 체크가1로 바뀌어서 녹음중이라고 인식하여 오류가 발생할 것이다
                check = 1
            }
        }
    }

    fun stopRecording() {

        //result라는 텍스트뷰를 가져와서 저장한다
        val result = findViewById(R.id.result) as TextView
        try {
            //녹음을 멈추고
            mediaRecorder!!.stop()
            //리셋한다
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
            //객체를 비워준다 이 조건이 성립하지 않으면 녹음이 시작하지 않게 설정되었다
            mediaRecorder = null

            //위에서 했던것처럼 오류 발생시 프린트 해주고 최종적으로 체크를 0 으로 바꿔준다
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            check = 0
            //그리고 그 텍스트뷰의 텍스트를 finish로 바꿔준다.
            //result.setText("Finish");
        }
    }

    //이부분이 실질적인 마이크 측정 수치를 받아오는 함수이다
    //레코더가 녹음 중 일때만
    //마이크로 들어오는 파형?을 받아온다
    //녹음 중이 아닐때는 0을 리턴한다
    //아마 종료 전에 이부분이 활동을 하고 있어서 finish로 멈추지 않고 0으로 끝나는 것 같다.
    val amplitude: Int
        get() {
            if (mediaRecorder != null) {
                return mediaRecorder!!.maxAmplitude
            } else
                return 0
        }

    //실질적인 뷰를 보여주기 위한 곳이다 원래 존재하는 함수이다 기본적으로
    override fun onCreate(savedInstanceState: Bundle?) {
        //상속받아서 넣어준다
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        btn_toMain.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun init() {
        result = findViewById(R.id.result) as TextView


        //이어폰 리시버를 만들어서 r이라는 객체에 저장한다
        r = EarphoneButtonReceiver()

        //media버튼을 이용하는 인텐트 필터를 만들어서 필터라는 변수에 저장한다
        val filter = IntentFilter(Intent.ACTION_MEDIA_BUTTON)

        //filter의 우선순위를 최고로 만들어준다
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY + 1

        //        filter2.setPriority(100000000);
        //        registerReceiver(r2,filter2);

        //리시버를 등록해준다 이걸 등록함으로써 이제 이벤트 발생에 대해서 인지할 수 있다
        registerReceiver(r, filter)


        //권한을 요청하는 부분이다
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSIONS)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

//        val player: MediaPlayer
//        player = MediaPlayer.create(this, R.raw.ddok)
//

        //핸들러를 객체로 생성하여
        mHandler = object : Handler() {
            //메시지를 인자로 받는 함수를 이용하여
            override fun handleMessage(msg: Message) {
                //위에서 파형을 수치로 받아온것을 문자열로 바꿔 a에 저장한다
                val a = Integer.toString(amplitude)

                //result를 텍스트뷰로 지정하고 연결하여
                result = findViewById(R.id.result) as TextView

                //볼륨쪽을 가져온다 버튼을
                val checkValue = findViewById(R.id.checkButton) as TextView

                //result칸이 텍스트뷰의 텍스트를 a로 계속해서 받아온다
                //result.setText(a);


                //audioManager
                if (!audioManager.isMusicActive && controlHandler == 1) {
                    controlHandler = 0
                    result.text = "no music "
//                    player.start()

                } else if (audioManager.isMusicActive && controlHandler == 0) {
                    controlHandler = 1
                    result.text = "play music "
                } else {

                }
                checkValue.text = a

                //아무것도 오지않을땐 0을 보내고 100이므로 0.1초마다 한번씩 갱신한다
                mHandler.sendEmptyMessageDelayed(0, 100)
            }
        }

        //다끝나고 나면 0을 보내게 된다 이부분을 안하게 되면 끝났을때 안보내려나?
        //이상하게 이부분을 주석처리하고 실행하면 아예 실행이 제대로 되지 않는다 수치자체가 표시가 되지 않는다.
        mHandler.sendEmptyMessage(0)

        //레코드 버튼을 받아와서 recordButton의 객체로 저장한다
        val recordButton = findViewById(R.id.record) as Button

        //레코드 버튼을 눌렀을때 실행할 것들을 리스너를 통해서 정리하였다
        recordButton.setOnClickListener {
            if (check == 0) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(r)
    }

    //이 부분이 없어서 여태 실행이 되지 않았다
    // 이부분이 중요하다는 글이 없었는데 onKeyDown을 통해서 키를 눌러 주었을때 인식을 하게 해주는 부분이다
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //부모로 부터 상속을 받는다
        super.onKeyDown(keyCode, event)

        //result에 텍스트 뷰를 가져와서 객체로 저장한다
        val result = findViewById(R.id.checkButton) as TextView
        var context: String? = null
        var message: String? = null
        var ok = false

        //볼륨 다운 버튼이 눌리면

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //토스트로 띄워준다
            context = "volume down is pushed"
            message = "Volume Down!"
        } else if (event.keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            //띄워주고
            context = "headset is pushed"
            message = "headset Pushed"
        } else if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            //띄워주고
            Toast.makeText(this, "volume up is pushed", Toast.LENGTH_SHORT).show()
            //텍스트 변경
            result.text = "Volume Up!"
            return true
        } else {
            //오류 메시지를 띄워주고
            Toast.makeText(this, "Cant recognize yet", Toast.LENGTH_SHORT).show()
            //키코드에 대한 정보로 텍스트를 변경해준다
            result.text = "Something pushed : " + keyCode
            return true
        }//설정하지 않은 경우이다 예외라고 볼 수 있다
        //볼륨업 버튼이 눌리게 되면
        //같은 방식이다 헤드셋 훅 버튼이 눌렸을때이다

        if (context != null && message != null) {
            ok = showMessage(event, context, message)
        }
        return ok
    }

    private fun showMessage(event: KeyEvent, context: String, message: String): Boolean {
        Toast.makeText(this, context, Toast.LENGTH_SHORT).show()
        result.text = message
        return true
    }

    companion object {

        //오디오 레코드를 위한 권한을 요청하는 코드를 200으로 지정한다
        private val REQUEST_RECORD_AUDIO_PERMISSIONS = 200
    }

}

