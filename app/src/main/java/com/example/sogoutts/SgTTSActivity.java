package com.example.sogoutts;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.sogou.tts.offline.TTSPlayer;
import com.sogou.tts.offline.listener.TTSPlayerListener;
import com.sogou.tts.offline.utils.Mode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class SgTTSActivity extends Activity
		implements
			OnClickListener,
			OnCheckedChangeListener {
	private static final String TAG = "SgTTSActivity";

	private EditText mVoiceSpeedEdt;// voice speed
	private EditText mSynthContentEdt; // synth content
	private EditText mLogEdt;// log
	private EditText mFilePathEdt;// file path
    private EditText mVoiceVolumeEdt;
    private EditText mVOicePitchEdt;
    private Switch mModeSwitch;

	// play
	private Button mPlayBtn;
	// pause
	private Button mPauseBtn;
	// resume
	private Button mResumeBtn;
	// stop
	private Button mStopBtn;
	// batch replay
	private Button mReplayBtn;
	// batch play next
	private Button mPlayNextBtn;
	private Button mAddBtn;
	private CheckBox mBatchCheckBox;

	private TTSPlayer mTTSPlayer; // player
	private DemoTTSPlayerListener ttsPlayerlistener;

	private BufferedReader batchBufferReader = null;
	private String curSynthContent = null;
	private Boolean isContinue = false;// only in batchMode

	private String[] chars;
	private ArrayList<Float> sylEndTime;
	BufferedWriter out = null;
//	private String[] snds = {"snd-f24.dat","snd-lhy.dat","snd-ybh.dat","snd-yzh.dat","snd-zj.dat","snd-zjc.dat","snd-zsh.dat"};
	private String[] speakers = {"中文男生","中文女生","英文男生","英文女生"};

	//private float curTime;
	private Spinner spinner1;
	private Spinner spinner2;
	private String mCurrentSnd = "snd-f24.dat";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sg_tts_layout);
			
		bindView();
		setListener();

		mTTSPlayer = new TTSPlayer(2);
		ttsPlayerlistener = new DemoTTSPlayerListener();


		init();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isContinue = false;
		if (mTTSPlayer != null) {
			mTTSPlayer.shutdown();
			mTTSPlayer = null;
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isContinue = true;
		//if (mTTSPlayer != null) {
		//	mTTSPlayer.resume();
		//}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.w("sogoutts", "on pause");
		isContinue = false;
		//if (mTTSPlayer != null) {
		//	mTTSPlayer.pause();
		//}
	}

	private void bindView() {
		// bind views
		mVoiceSpeedEdt = (EditText) findViewById(R.id.voice_speed_edt);
		mSynthContentEdt = (EditText) findViewById(R.id.synth_content_edt);
		mPlayBtn = (Button) findViewById(R.id.play_btn);
		mPauseBtn = (Button) findViewById(R.id.pause_btn);
		mResumeBtn = (Button) findViewById(R.id.resume_btn);
		mStopBtn = (Button) findViewById(R.id.stop_btn);
		mLogEdt = (EditText) findViewById(R.id.log_edt);
		mBatchCheckBox = (CheckBox) findViewById(R.id.batch_mode_cb);
		mReplayBtn = (Button) findViewById(R.id.replay_btn);
		mPlayNextBtn = (Button) findViewById(R.id.next_btn);
		mFilePathEdt = (EditText) findViewById(R.id.file_path_edt);
		mAddBtn = (Button) findViewById(R.id.add_btn);

        mVoiceVolumeEdt = findViewById(R.id.voice_volume_edt);
        mVOicePitchEdt = findViewById(R.id.voice_pitch_edt);
        mModeSwitch = findViewById(R.id.switch_mode);
		spinner1 = findViewById(R.id.spinner1);
		spinner2 = findViewById(R.id.spinner2);
		mModeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					spinner1.setVisibility(View.GONE);
					spinner2.setVisibility(View.VISIBLE);
				}else {
					spinner1.setVisibility(View.VISIBLE);
					spinner2.setVisibility(View.GONE);
				}
			}
		});



		if (spinner2 != null) {
			ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, speakers);
			spinner2.setAdapter(arrayAdapter2);
			spinner2.setSelection(0);
		}

	}

	private void setListener() {
		// set listener
		mPlayBtn.setOnClickListener(this);
		mPauseBtn.setOnClickListener(this);
		mResumeBtn.setOnClickListener(this);
		mStopBtn.setOnClickListener(this);
		mPlayNextBtn.setOnClickListener(this);
		mReplayBtn.setOnClickListener(this);
		mBatchCheckBox.setOnCheckedChangeListener(this);
		mAddBtn.setOnClickListener(this);
	}

	private void init() {
		// init data


		if ((mTTSPlayer.init(this, ttsPlayerlistener))<0){
//		if ((mTTSPlayer.init(this, "/sdcard/SogouTTS/newmodel/","dict.dat",snds[0], ttsPlayerlistener))<0) {
			showLog(R.string.synth_initialized_failure);
//			mPlayBtn.setClickable(false);
//			mPauseBtn.setClickable(false);
//			mResumeBtn.setClickable(false);
//			mStopBtn.setClickable(false);
		} else {
			// set TTSPlayer streamType
//			mTTSPlayer.addSound("/sdcard/SogouTTS/");
			mTTSPlayer.setStreamType(AudioManager.STREAM_MUSIC);
			showLog(R.string.player_initialized);
			mTTSPlayer.setWriteLog(false);
			//chars = new String[0];
		}


		sylEndTime = new ArrayList<Float>();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.play_btn :
//				doSynthStop();
				doSynthPlay();
//				String longtext = mSynthContentEdt.getText().toString();
//				mTTSPlayer.playOnline(longtext,TTSPlayer.QUEUE_FLUSH,"play");
				break;
			case R.id.pause_btn :
				doSynthPause();
				break;
			case R.id.resume_btn :
				doSynthResume();
				break;
			case R.id.stop_btn :
				doSynthStop();
				break;
			case R.id.next_btn :
				doSynthPlayNext();
				break;
			case R.id.replay_btn :
				doSynthReplay();
				break;
			case R.id.add_btn:
				doSynthAdd();
				break;
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			mSynthContentEdt.setEnabled(false);
			mReplayBtn.setEnabled(true);
			mPlayNextBtn.setEnabled(true);
		} else {
			mSynthContentEdt.setEnabled(true);
			mReplayBtn.setEnabled(false);
			mPlayNextBtn.setEnabled(false);
		}
	}

	public class DemoTTSPlayerListener implements TTSPlayerListener {
		@Override
		public void onStart(String identifier) {
			// TODO Auto-generated method stub
			Log.w("sogoutts", "onstart");
			showLog(identifier, R.string.state_playing);
		}

		@Override
		public void onEnd(String identifier,String text) {
			//Log.w("sogoutts", "onend");
			// TODO Auto-generated method stub
			showLog(identifier, R.string.state_idle);
			Log.w("sogoutts", text + "onend");
			/*if (mBatchCheckBox.isChecked() && isContinue && mTTSPlayer != null) {
				doSynthPlayNext();
			}*/
		}

		@Override
		public void onError(String identifier,int errCode) {
			// TODO Auto-generated method stub
			showLog(R.string.state_error, errCode);
		}

		@Override
		public void onPause(String identifier) {
			Log.w("sogoutts", "onpause");
			showLog(identifier, R.string.state_pause);
			// TODO Auto-generated method stub
			
		}



		@Override
		public void onSynEnd(String identifier,Float sumTime) {
			// TODO Auto-generated method stub
			Log.w("sogoutts", "syn end " + sumTime);
		}

        @Override
        public void onResume(String identifier) {
            Log.w("sogoutts", "onResume");
			showLog(identifier, R.string.state_resume);
        }
    }

    private void changeParams(){
		if (!mVoiceSpeedEdt.getText().toString().trim().equals("")) {
			try {
				int i=Integer.valueOf(mVoiceSpeedEdt.getText()
						.toString().trim());
				mTTSPlayer.setSpeed(i);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

        if (!mVoiceVolumeEdt.getText().toString().trim().equals("")) {
            try {
                int i=Integer.valueOf(mVoiceVolumeEdt.getText()
                        .toString().trim());
                mTTSPlayer.setVolume(i);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (!mVOicePitchEdt.getText().toString().trim().equals("")) {
            try {
                int i=Integer.valueOf(mVOicePitchEdt.getText()
                        .toString().trim());
                mTTSPlayer.setPitch(i);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        boolean mode = mModeSwitch.isChecked();
        if (mode){
            mMode = Mode.TYPE_ONLINE;
        }else {
            mMode = Mode.TYPE_OFFLINE;
        }

        switch (spinner2.getSelectedItemPosition()){
			case 0:
				mTTSPlayer.setLanguage(Locale.CHINA);
				mTTSPlayer.setSpeaker("Male");
				break;
			case 1:
				mTTSPlayer.setLanguage(Locale.CHINA);
				mTTSPlayer.setSpeaker("Female");

				break;
			case 2:
				mTTSPlayer.setLanguage(Locale.ENGLISH);
				mTTSPlayer.setSpeaker("Male");

				break;
			case 3:
				mTTSPlayer.setLanguage(Locale.ENGLISH);
				mTTSPlayer.setSpeaker("Female");

				break;
		}

	}

	private int mMode = Mode.TYPE_OFFLINE;

	private void doSynthAdd() {
		changeParams();

		String longtext = mSynthContentEdt.getText().toString();
			//curTime = 0;
			//longtext="失眠了不爱喝牛奶？七种食物让你能快速入睡                                                                                                                                         难受多喝水？失眠多喝奶？这种不走心的意见是不是已经听到吐！到底小伙伴能不能提些有建设性的意见呢？ 整晚辗转反侧不能入眠,确实是令人沮丧。 另外，缺乏足够睡眠也会导致不少健康问题：如腰围上涨、高血压和情绪变化。要解决问题, 其一是通过饮食来改善睡眠质素,但这方法却往往被忽视。 Tossing and turning is frustrating, and the lack of shut-eye can lead to such health problems as an expanding waistline, high blood pressure and an altered mood. Typically overlooked, one way to improve sleep is through diet. 1.香蕉 Bananas 香蕉含有丰富的钾质，可以舒缓不宁腿的症状和防止夜间腿抽筋。香蕉还能为身体提供镁，有助肌肉和神经放松，促进血液循环和消化。 They’re high in potassium, which may calm restless legs and help prevent nighttime leg cramps. Plus, bananas also provide magnesium, which helps relax muscles and nerves and promotes healthy circulation and digestion. 2.杏仁 Almonds 杏仁含有镁，可以帮助你进入更好的睡眠状态。它还能提供蛋白质，助你在睡眠时维持稳定的血糖水平。 Almonds contain magnesium and can help ease you into a better night’s sleep. They also provide protein, which can help you maintain a stable blood sugar level while you’re sleeping. 3.凉茶或不含咖啡因的绿茶 Herbal Tea or Decaffeinated Green Tea 专家指出，大多种不含咖啡因的茶也可以引起睡意。绿茶含有茶氨酸，可更容易让人入睡。 Experts say most varieties of decaf tea will encourage drowsiness. Green tea contains theanine, which may promote sleep. 4.燕麦粥 Oatmeal 它含有大量的钙，镁和钾，这些物质都能帮助你更快速地入睡。只是不要放太多糖，因为睡前吸收过量糖份会对睡眠产生反效果。 It packs plenty of calcium, magnesium and potassium, all of which may help make you fall asleep more quickly.Just go easy on the sugar -- too much before bed can have the opposite effect. 5.甘薯 Sweet potatoes 甘薯可提供丰富的钾质，它可以放松肌肉和神经，促进血液循环和消化。 Sweet potatoes are a good source of potassium, which relaxes muscles and nerves and aids circulation and digestion. 6.麦片 Cereal 一小碗低糖、全谷麦片可以作为睡眠时段的健康零食。 A small bowl of low-sugar, whole-grain cereal can be a healthy snack that sets the stage for sleep. 7.鲜奶 Milk（地球人都知道==） 钙可以直接生产褪黑素，有助维持体内的24小时醒睡周期。豆浆比牛奶更好吗？大豆产品可以令人睡得更快、更香，还可以帮助受失眠困扰的更年期妇女。 Calcium plays a direct role in the production of melatonin, which helps to maintain your body’s 24-hour sleep-wake cycle. Prefer soy milk to cow's milk? Soy products have been known to make people fall asleep faster and deeper. They may help insomnia in menopausal women. 现在大家应该知道哪些食物可以让你快速入睡了吧!";
			//longtext="失眠了不爱喝牛奶？七种食物让你能快速入睡  难受多喝水？失眠多喝奶？这种不走心的意见是不是已经听到吐！到底小伙伴能不能提些有建设性的意见呢？ 整晚辗转反侧不能入眠,确实是令人沮丧。 另外，缺乏足够睡眠也会导致不少健康问题：如腰围上涨、高血压和情绪变化。要解决问题, 其一是通过饮食来改善睡眠质素,但这方法却往往被忽视。 Tossing and turning is frustrating, and the lack of shut-eye can lead to such health problems as an expanding waistline, high blood pressure and an altered mood. Typically overlooked, one way to improve sleep is through diet. 1.香蕉 Bananas 香蕉含有丰富的钾质，可以舒缓不宁腿的症状和防止夜间腿抽筋。香蕉还能为身体提供镁，有助肌肉和神经放松，促进血液循环和消化。 They’re high in potassium, which may calm restless legs and help prevent nighttime leg cramps. Plus, bananas also provide magnesium, which helps relax muscles and nerves and promotes healthy circulation and digestion. 2.杏仁 Almonds 杏仁含有镁，可以帮助你进入更好的睡眠状态。它还能提供蛋白质，助你在睡眠时维持稳定的血糖水平。 Almonds contain magnesium and can help ease you into a better night’s sleep. They also provide protein, which can help you maintain a stable blood sugar level while you’re sleeping. 3.凉茶或不含咖啡因的绿茶 Herbal Tea or Decaffeinated Green Tea 专家指出，大多种不含咖啡因的茶也可以引起睡意。绿茶含有茶氨酸，可更容易让人入睡。 Experts say most varieties of decaf tea will encourage drowsiness. Green tea contains theanine, which may promote sleep. 4.燕麦粥 Oatmeal 它含有大量的钙，镁和钾，这些物质都能帮助你更快速地入睡。只是不要放太多糖，因为睡前吸收过量糖份会对睡眠产生反效果。 It packs plenty of calcium, magnesium and potassium, all of which may help make you fall asleep more quickly.Just go easy on the sugar -- too much before bed can have the opposite effect. 5.甘薯 Sweet potatoes 甘薯可提供丰富的钾质，它可以放松肌肉和神经，促进血液循环和消化。 Sweet potatoes are a good source of potassium, which relaxes muscles and nerves and aids circulation and digestion. 6.麦片 Cereal 一小碗低糖、全谷麦片可以作为睡眠时段的健康零食。 A small bowl of low-sugar, whole-grain cereal can be a healthy snack that sets the stage for sleep. 7.鲜奶 Milk（地球人都知道==） 钙可以直接生产褪黑素，有助维持体内的24小时醒睡周期。豆浆比牛奶更好吗？大豆产品可以令人睡得更快、更香，还可以帮助受失眠困扰的更年期妇女。 Calcium plays a direct role in the production of melatonin, which helps to maintain your body’s 24-hour sleep-wake cycle. Prefer soy milk to cow's milk? Soy products have been known to make people fall asleep faster and deeper. They may help insomnia in menopausal women. 现在大家应该知道哪些食物可以让你快速入睡了吧!";

		mTTSPlayer.speak(longtext, TTSPlayer.QUEUE_ADD,"add"+ index++,mMode);

	}

	private int index = 0;


	// play
	private void doSynthPlay() {
		//mTTSPlayer.writeLog("play");
		//mTTSPlayer.stop();
		//mTTSPlayer.play("航班延误25分钟，22:40抵达首都机场t3，可2小时10分钟后出发，您去停车场还是航站楼门口？", "");

		changeParams();

		if (mBatchCheckBox.isChecked()) {
			isContinue = true;
			doSynthPlayNext();
			mReplayBtn.setEnabled(false);
			//mPlayNextBtn.setEnabled(false);
			mPauseBtn.setEnabled(false);
			mResumeBtn.setEnabled(false);
			mPlayBtn.setEnabled(false);
		} else {
			String longtext = mSynthContentEdt.getText().toString();
			//curTime = 0;
			chars = new String[0];
			sylEndTime.clear();
			//longtext="失眠了不爱喝牛奶？七种食物让你能快速入睡                                                                                                                                         难受多喝水？失眠多喝奶？这种不走心的意见是不是已经听到吐！到底小伙伴能不能提些有建设性的意见呢？ 整晚辗转反侧不能入眠,确实是令人沮丧。 另外，缺乏足够睡眠也会导致不少健康问题：如腰围上涨、高血压和情绪变化。要解决问题, 其一是通过饮食来改善睡眠质素,但这方法却往往被忽视。 Tossing and turning is frustrating, and the lack of shut-eye can lead to such health problems as an expanding waistline, high blood pressure and an altered mood. Typically overlooked, one way to improve sleep is through diet. 1.香蕉 Bananas 香蕉含有丰富的钾质，可以舒缓不宁腿的症状和防止夜间腿抽筋。香蕉还能为身体提供镁，有助肌肉和神经放松，促进血液循环和消化。 They’re high in potassium, which may calm restless legs and help prevent nighttime leg cramps. Plus, bananas also provide magnesium, which helps relax muscles and nerves and promotes healthy circulation and digestion. 2.杏仁 Almonds 杏仁含有镁，可以帮助你进入更好的睡眠状态。它还能提供蛋白质，助你在睡眠时维持稳定的血糖水平。 Almonds contain magnesium and can help ease you into a better night’s sleep. They also provide protein, which can help you maintain a stable blood sugar level while you’re sleeping. 3.凉茶或不含咖啡因的绿茶 Herbal Tea or Decaffeinated Green Tea 专家指出，大多种不含咖啡因的茶也可以引起睡意。绿茶含有茶氨酸，可更容易让人入睡。 Experts say most varieties of decaf tea will encourage drowsiness. Green tea contains theanine, which may promote sleep. 4.燕麦粥 Oatmeal 它含有大量的钙，镁和钾，这些物质都能帮助你更快速地入睡。只是不要放太多糖，因为睡前吸收过量糖份会对睡眠产生反效果。 It packs plenty of calcium, magnesium and potassium, all of which may help make you fall asleep more quickly.Just go easy on the sugar -- too much before bed can have the opposite effect. 5.甘薯 Sweet potatoes 甘薯可提供丰富的钾质，它可以放松肌肉和神经，促进血液循环和消化。 Sweet potatoes are a good source of potassium, which relaxes muscles and nerves and aids circulation and digestion. 6.麦片 Cereal 一小碗低糖、全谷麦片可以作为睡眠时段的健康零食。 A small bowl of low-sugar, whole-grain cereal can be a healthy snack that sets the stage for sleep. 7.鲜奶 Milk（地球人都知道==） 钙可以直接生产褪黑素，有助维持体内的24小时醒睡周期。豆浆比牛奶更好吗？大豆产品可以令人睡得更快、更香，还可以帮助受失眠困扰的更年期妇女。 Calcium plays a direct role in the production of melatonin, which helps to maintain your body’s 24-hour sleep-wake cycle. Prefer soy milk to cow's milk? Soy products have been known to make people fall asleep faster and deeper. They may help insomnia in menopausal women. 现在大家应该知道哪些食物可以让你快速入睡了吧!";
			//longtext="失眠了不爱喝牛奶？七种食物让你能快速入睡  难受多喝水？失眠多喝奶？这种不走心的意见是不是已经听到吐！到底小伙伴能不能提些有建设性的意见呢？ 整晚辗转反侧不能入眠,确实是令人沮丧。 另外，缺乏足够睡眠也会导致不少健康问题：如腰围上涨、高血压和情绪变化。要解决问题, 其一是通过饮食来改善睡眠质素,但这方法却往往被忽视。 Tossing and turning is frustrating, and the lack of shut-eye can lead to such health problems as an expanding waistline, high blood pressure and an altered mood. Typically overlooked, one way to improve sleep is through diet. 1.香蕉 Bananas 香蕉含有丰富的钾质，可以舒缓不宁腿的症状和防止夜间腿抽筋。香蕉还能为身体提供镁，有助肌肉和神经放松，促进血液循环和消化。 They’re high in potassium, which may calm restless legs and help prevent nighttime leg cramps. Plus, bananas also provide magnesium, which helps relax muscles and nerves and promotes healthy circulation and digestion. 2.杏仁 Almonds 杏仁含有镁，可以帮助你进入更好的睡眠状态。它还能提供蛋白质，助你在睡眠时维持稳定的血糖水平。 Almonds contain magnesium and can help ease you into a better night’s sleep. They also provide protein, which can help you maintain a stable blood sugar level while you’re sleeping. 3.凉茶或不含咖啡因的绿茶 Herbal Tea or Decaffeinated Green Tea 专家指出，大多种不含咖啡因的茶也可以引起睡意。绿茶含有茶氨酸，可更容易让人入睡。 Experts say most varieties of decaf tea will encourage drowsiness. Green tea contains theanine, which may promote sleep. 4.燕麦粥 Oatmeal 它含有大量的钙，镁和钾，这些物质都能帮助你更快速地入睡。只是不要放太多糖，因为睡前吸收过量糖份会对睡眠产生反效果。 It packs plenty of calcium, magnesium and potassium, all of which may help make you fall asleep more quickly.Just go easy on the sugar -- too much before bed can have the opposite effect. 5.甘薯 Sweet potatoes 甘薯可提供丰富的钾质，它可以放松肌肉和神经，促进血液循环和消化。 Sweet potatoes are a good source of potassium, which relaxes muscles and nerves and aids circulation and digestion. 6.麦片 Cereal 一小碗低糖、全谷麦片可以作为睡眠时段的健康零食。 A small bowl of low-sugar, whole-grain cereal can be a healthy snack that sets the stage for sleep. 7.鲜奶 Milk（地球人都知道==） 钙可以直接生产褪黑素，有助维持体内的24小时醒睡周期。豆浆比牛奶更好吗？大豆产品可以令人睡得更快、更香，还可以帮助受失眠困扰的更年期妇女。 Calcium plays a direct role in the production of melatonin, which helps to maintain your body’s 24-hour sleep-wake cycle. Prefer soy milk to cow's milk? Soy products have been known to make people fall asleep faster and deeper. They may help insomnia in menopausal women. 现在大家应该知道哪些食物可以让你快速入睡了吧!";
//			mTTSPlayer.stop();
			mTTSPlayer.speak(longtext, TTSPlayer.QUEUE_FLUSH,"play"+index++, mMode);
			count ++;
		}
	}

	private int count = 0;
	// pause
	private void doSynthPause() {
		mTTSPlayer.pause();
		//showLog(R.string.state_pause);
//		mTTSPlayer.speak("这款翻译硬件是搜狗公司人工智能落地的战略性产品，专注解决人们在出境交流中的语言障碍问题",TTSPlayer.QUEUE_ADD,"bbb");
	}

	// resume
	private void doSynthResume() {
		mTTSPlayer.resume();
		//showLog(R.string.state_resume);

	}

	// stop
	private void doSynthStop() {
		if(mTTSPlayer == null){
			return;
		}
		if (mBatchCheckBox.isChecked()) {
			isContinue = false;
			if (batchBufferReader != null) {
				try {
					batchBufferReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				batchBufferReader = null;
			}
			mTTSPlayer.stop();

			mReplayBtn.setEnabled(true);
			mPlayNextBtn.setEnabled(true);
			mPauseBtn.setEnabled(true);
			mResumeBtn.setEnabled(true);
			mPlayBtn.setEnabled(true);
			curSynthContent = "";
		} else {
			mTTSPlayer.stop();
			//doSynthPlay();
		}
	}

	// BatchMode play next sentance
	private void doSynthPlayNext() {
		//mTTSPlayer.writeLog("playnext");
		//mTTSPlayer.stop();
		//mTTSPlayer.play("航班延误25分钟，22:40抵达首都机场t3，可2小时10分钟后出发，您去停车场还是航站楼门口？", "");
		//return;
		String batchFile = mFilePathEdt.getText().toString().trim();
		if (batchFile.equals("")) {
			showLog(R.string.state_file_not_found);
			return;
		}
		if (batchBufferReader == null) {
			try {
				batchBufferReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(batchFile)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		String nextSynthContent = null;
		try {
			while ((nextSynthContent = batchBufferReader.readLine()) != null) {
				if (nextSynthContent.length() <= 0) {
					continue;
				} else {
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			showLog(R.string.state_file_read_failure);
			e.printStackTrace();
			return;
		}
		if (nextSynthContent != null) {
			curSynthContent = nextSynthContent;
			if (mTTSPlayer != null) {
				Log.e(TAG, curSynthContent);
				mTTSPlayer.stop();
				mTTSPlayer.speak(curSynthContent,TTSPlayer.QUEUE_FLUSH,"next");
			}
		} else {
			showLog(R.string.state_last_content_finished);
			try {
				batchBufferReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				showLog(R.string.state_file_close_failure);
				e.printStackTrace();
				return;
			}
			batchBufferReader = null;
			isContinue = false;
			mReplayBtn.setEnabled(true);
			mPlayNextBtn.setEnabled(true);
			mPauseBtn.setEnabled(true);
			mResumeBtn.setEnabled(true);
			mPlayBtn.setEnabled(true);
			curSynthContent = "";
		}
	}

	// BatchMode replay current sentance
	private void doSynthReplay() {
		if (curSynthContent != null && curSynthContent.length() > 0) {
			if (mTTSPlayer != null) {
				mTTSPlayer.speak(curSynthContent, TTSPlayer.QUEUE_FLUSH,"replay");
			}
		}
	}

	private void showLog(String id,int stringID) {
		mLogEdt.append("Identifier："+id + " ；" + getResources().getString(stringID) + "\n");
	}

	private void showLog(int stringID) {
		mLogEdt.append(getResources().getString(stringID) + "\n");
	}

	private void showLog(int stringID, int errCode) {
		mLogEdt.append(getResources().getString(stringID) + errCode + "\n");
	}

}
