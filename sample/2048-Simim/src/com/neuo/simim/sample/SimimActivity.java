package com.neuo.simim.sample;

import com.neuo.android.AndroidUtil;
import com.neuo.android.CommonHandler;
import com.neuo.android.ScreenRatioUtil;
import com.neuo.android.CommonHandler.HandlerOuter;
import com.neuo.common.CommonEvent;
import com.neuo.common.CommonEvent.Event;
import com.neuo.common.SecretString;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

public class SimimActivity extends Activity
implements HandlerOuter {
	
	public static final int ShowTip = 0;
	public static final int Exit = 1;
	public static final int MoveBack = 2;
	private static class SimimHandler extends CommonHandler {
		private SimimActivity mActivity;
			
		public SimimHandler(SimimActivity activity) {
			super(false);
			mActivity = activity;
		}

		@Override
		protected void handleMessageSelf(Message msg) {
			switch (msg.what) {
			case ShowTip:
				AndroidUtil.showTip(mActivity, msg.arg1, false);
				break;
			case Exit:
				mActivity.finish();
				break;
			case MoveBack:
				mActivity.moveTaskToBack(true);
				break;
			default:
				break;
			}
		}
		
		@Override
		public void release() {
			this.mActivity = null;
			super.release();
		}
	}
	
	private FrameLayout screenFrame;
	private Toast showToast;

	private boolean isRelease = false;
	private SecretString packageName = null;
	private SimimHandler handler;
	
	private SimimStateManager simimStateManager = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AndroidUtil.initFullScreen(this, true, true);
		super.onCreate(savedInstanceState);
		isRelease = false;
		initSecretName();
		initFrameLayout();
		handler = new SimimHandler(this);
		simimStateManager = new SimimStateManager(this);
		simimStateManager.start();
	}
	
	public FrameLayout getFrameLayout() {
		return screenFrame;
	}
	
	protected void onStart() {
		simimStateManager.onStart();
		super.onStart();
	}

	protected void onDestroy() {
		releaseAll();
		super.onDestroy();
	}

	protected void onPause() {
		simimStateManager.onPause();
		handler.onPause();
		super.onPause();
	}
	
	protected void onResume(){
		super.onResume();
		handler.onResume();
		simimStateManager.onResume();
	}
	
	protected void onStop() {
		simimStateManager.onStop();
		super.onStop();
	}
	
	private void initSecretName() {
		packageName = new SecretString(); 
		packageName.addString("xl");
		packageName.addString("n&mvf");
		packageName.addString("l&gluv");
	}
	
	@Override
	public CommonHandler getHandler() {
		return handler;
	}
	
	private void initFrameLayout() {
		screenFrame = new FrameLayout(this);
		setContentView(screenFrame);
	}

	private void releaseAll() {
		if (simimStateManager != null) {
			simimStateManager.release();
			simimStateManager = null;
		}
		if (handler != null) {
			handler.release();
			handler = null;
		}
		if (null != showToast) {
			showToast.cancel();
			showToast = null;
		}
		ScreenRatioUtil.release();
		isRelease = true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent e){
		if (isRelease) {
			return false;
		}
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			simimStateManager.onKeyEvent(new Event(CommonEvent.Back));
		} else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
			simimStateManager.onKeyEvent(new Event(CommonEvent.KeyUp));
		} else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
			simimStateManager.onKeyEvent(new Event(CommonEvent.KeyDown));
		}
		return true;
	}

}

