package com.neuo.simim.sample;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.neuo.common.AsyncTaskProcessor;
import com.neuo.common.BitmapManager;
import com.neuo.common.CommonEvent;
import com.neuo.common.CommonFont;
import com.neuo.common.ConfigureManager;
import com.neuo.common.FontManager;
import com.neuo.common.SecretNumber;
import com.neuo.common.SecretString;
import com.neuo.common.CommonEvent.Event;
import com.neuo.game.GameStateManager;
import com.neuo.glcommon.GlObjManager;
import com.neuo.glcommon.GlTexture;
import com.neuo.glcommon.GlTextureManager;
import com.neuo.glshader.GlShaderManager;

public class SimimStateManager extends GameStateManager {
	private GlTextureManager textureManager;
	private FontManager fontManager;
	@SuppressWarnings("unused")
	private GlObjManager glObjManager;
	private Configuration configuration;
	@SuppressWarnings("unused")
	private ConfigureManager configureManager;

	private SimimActivity activity;
	private GlShaderManager myShaderManager;
	private AsyncTaskProcessor asyncTaskProcessor;
	//private AdmobManager admobManager;
	private SecretString appID;
	private SecretString adsID;
	private PlayView playView;
	private GameManager gameManager;

	public static final int PopResult = 1;
	@Override
	protected void onHandleMessage(Message msg) {
		Log.d("test", "on handle message");
		switch (msg.what) {
		case PopResult:
			playView.popResult();
			break;
		default:
			break;
		}
	}
	
	@Override
	protected GameHandler initHandler() {
		return new GameHandler(this);
	}
	
	public SimimStateManager(SimimActivity activity) {
		super(activity);
	}
	
	@Override
	public void init(Context context) {
		super.init(context);
		this.activity = (SimimActivity)context;
		configureManager = ConfigureManager.initConfigureManager(context.getResources());
		asyncTaskProcessor = AsyncTaskProcessor.getAsyncTaskProcessor();
		configuration = Configuration.initConfig(this.activity.getSharedPreferences(Constants.sharedPrefName,
													Context.MODE_PRIVATE));
		initString();
		initShaderManager();
		initTexManager();
		initFont();
		glObjManager = GlObjManager.getGlObjManager();
		gameManager = new GameManager();
		configuration.debug();
		if (checkDataEmpty()) {
			gameManager.reset();
		} else {
			gameManager.setData(configuration.getData(), (int)configuration.getData()[16].getNumber(),
									(int)configuration.getData()[17].getNumber(),
									(int)configuration.getData()[18].getNumber());
		}
		
		initViews();
		attachView();
		initFirstLoad();
	}
	
	private void initString() {
		appID = new SecretString();
		appID.addString("889");
		appID.addString("8724790");
		adsID = new SecretString();
		adsID.addString("09204");
		adsID.addString("627");
		adsID.addString("88194800832");
	}
	
	private void initFont() {
		fontManager = FontManager.getFontManager();
		fontManager.putFont(new CommonFont(Constants.CarFontName, Constants.CarFontName));
	}
	
	private boolean checkDataEmpty() {
		SecretNumber[] data = configuration.getData();
		for (int i = 0; i < 16; i++) {
			if (data[i].getNumber() != 0) {
				return false;
			}
		}
		return true;
	}
	
	private void initViews() {
		playView = new PlayView(activity, this, gameManager);
	}
	
	private void initTexManager() {
		textureManager = GlTextureManager.getTextureManager();
		textureManager.putTex(new GlTexture(Constants.AchieveName, "achieve.png", activity));
		textureManager.putTex(new GlTexture(Constants.LeaderboardName, "rank.png", activity));
		textureManager.putTex(new GlTexture(Constants.BackName, "back.png", activity));
		textureManager.putTex(new GlTexture(Constants.RetryName, "retry.png", activity));
		textureManager.putTex(new GlTexture(Constants.WinName, "win.png", activity));
		textureManager.putTex(new GlTexture(Constants.LostName, "lost.png", activity));
		int index = 2;
		for (int i = 0; i <= 10; i++) {
			textureManager.putTex(new GlTexture(Constants.NumberName + index,
								"number" + index + ".png", activity));
			if (i < 10)index *= 2;
		}
	}
	
	private void initShaderManager() {
		myShaderManager = GlShaderManager.getShaderManger(this.activity);
		myShaderManager.init(GlShaderManager.All);
	}
	
	private void initFirstLoad(){
		asyncTaskProcessor.start();
		AsyncTaskProcessor.AsyncTask loadTask = new AsyncTaskProcessor.AsyncTask() {
			protected void runnable(){
			}
		};
		asyncTaskProcessor.putAsyncTask(loadTask, false);
	}
	
	private void attachView() {
		FrameLayout.LayoutParams glParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
																			Gravity.TOP| Gravity.LEFT);
		this.activity.getFrameLayout().addView(vfGlDraw, glParams);
	}

	private void releaseAsyncProcessor(){
		if (asyncTaskProcessor != null){
			asyncTaskProcessor.stop();
		}
		AsyncTaskProcessor.release();
	}
	
	@Override
	public void release() {
		releaseAsyncProcessor();
		super.release();
		FontManager.release();
		GlTextureManager.release();
		GlObjManager.release();
		BitmapManager.release();
		Configuration.release();
		GlShaderManager.release();
		ConfigureManager.release();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onPause() {
		asyncTaskProcessor.pause();
		super.onPause();
		//admobManager.onPause();
		persistGameProgress();
	}
	
	private void persistGameProgress() {
		configuration.setData(gameManager.getData(), gameManager.getCurrScore(), 
									gameManager.getCurrStep(),
									gameManager.getBestScore());
		configuration.persistGameData();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		//admobManager.onResume();
		super.onResume();
		asyncTaskProcessor.start();
	}

	@Override
	public boolean onKeyEvent(Event e) {
		if (isRunning) {
			if (e.action == CommonEvent.Back) {
				vfGlDraw.onKeyEvent(e);
				return true;
			}
		}
		return false;
	}

	@Override
	public void start() {
		this.vfGlDraw.setGlView(playView, 0);
	}

	
}
