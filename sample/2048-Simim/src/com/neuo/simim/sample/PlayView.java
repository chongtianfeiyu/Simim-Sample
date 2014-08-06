package com.neuo.simim.sample;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Message;
import android.util.Log;

import com.neuo.common.CommonEvent.Event;
import com.neuo.common.ColorUtil;
import com.neuo.common.CommonEvent;
import com.neuo.common.CommonText.TextInterface;
import com.neuo.common.Controler.OverEvent;
import com.neuo.common.Controler.ProcessEvent;
import com.neuo.common.Controler;
import com.neuo.common.EventHander;
import com.neuo.common.EventManager;
import com.neuo.common.FontManager;
import com.neuo.common.SecretNumber;
import com.neuo.common.Timer;
import com.neuo.common.UpdateManager;
import com.neuo.gl2D.GlEventView;
import com.neuo.gl2D.GlEventView.ClickedListener;
import com.neuo.gl2D.GlEventView.MoveListener;
import com.neuo.gl2D.GlSim2DCamera;
import com.neuo.gl2D.GlTextView;
import com.neuo.gl2D.GlTextureView;
import com.neuo.glcommon.GlControlSimAlpha;
import com.neuo.glcommon.GlControlSimScale;
import com.neuo.glcommon.GlControlSimTrans;
import com.neuo.glcommon.GlDrawManage;
import com.neuo.glcommon.GlTextureManager;
import com.neuo.glcommon.GlView;
import com.neuo.globject.GlObjectByTex;
import com.neuo.glshader.GlShaderManager;
import com.neuo.glshader.ShaderByTex;
import com.neuo.simim.sample.GameManager.TileMoveInfo;
import com.neuo.simim.sample.R;

public class PlayView extends GlView 
implements ClickedListener, MoveListener {
	private GlSim2DCamera glScreen;
	private UpdateManager updateManager;
	private GlDrawManage drawManage;
	private EventManager eventManager;
	private GlTextureManager textureManager;

	private GlTextureView touchView;
	private GlTextureView backView;
	private GlTextureView achieveView;
	private GlTextureView rankView;
	private GlTextureView retryView;
	private GlTextView bestScoreView;
	private GlTextView currScoreView;
	private GlTextView currStepView;
	private GlTextureView shieldView;
	private GlTextureView resView;
	private GlTextureView[] numberViews;
	private Timer timer;
	private GlControlSimTrans mover1;
	private GlControlSimTrans mover2;
	private GlControlSimTrans mover3;
	private GlControlSimScale scaler1;
	private GlControlSimScale scaler2;
	private GlControlSimAlpha alphaer;
	
	private static final long moveSumTime = 400;
	private static final long moveTransTime = 200;
	private static final long moveOverTime = 300;
	private static final float ShieldAlpha = 0.5f;
	private static final float ShieldScale = 0.6f;
	private static final float TileScale = 1.1f;
	private static final float TileMoveX = 0.484f - 0.296f;
	private static final float TileMoveY = 0.509f - 0.405f;
	private static final float TileX = (0.14f + 0.296f) / 2f;
	private static final float TileY = 1f - (0.211f + 0.30f) / 2f;
	
	private static final float BackZ = 1;
	private static final float TouchZ = 2;
	private static final float UIZ = 3;
	private static final float TileZ = 4;
	private static final float TileIntervalZ = 1f;
	private static final float ShieldZ = 49f;
	private static final float ResZ = 50;
	private static final float RetryZ = 51;
	
	private SimimStateManager gameStateManager;
	private GameManager gameManager;
	private int runState;
	
	private static final int NoneState = -1;
	private static final int RunState = 0;
	private static final int OverState = 1;

	private GlTextureView createNewTexture() {
		GlTextureView res = new GlTextureView("", this,
				GlShaderManager.getShaderManager().getShader(ShaderByTex.myShaderTag),
				new GlObjectByTex());
		res.initAttr(false);
		return res;
	}
	
	private GlTextView createNewText() {
		GlTextView res = new GlTextView("", this,
				GlShaderManager.getShaderManager().getShader(ShaderByTex.myShaderTag),
				new GlObjectByTex());
		res.initAttr(false);
		return res;
	}
	
	private SimimActivity getActivity() {
		return (SimimActivity)context;
	}
	
	public PlayView(Context context, SimimStateManager gameStateManager,
							GameManager gameManager) {
		super(context);
		this.gameStateManager = gameStateManager;
		this.gameManager = gameManager;
		
		updateManager = new UpdateManager();
		drawManage = new GlDrawManage();
		eventManager = new EventManager();
		glScreen = new GlSim2DCamera();
		drawManage.setGlCamera(glScreen);

		textureManager = GlTextureManager.getTextureManager();
		
		touchView = createNewTexture();
		touchView.setDrawNothing(true);
		touchView.setMoveListener(this);
		
		backView = createNewTexture();
		backView.setTouchTexture(textureManager.getTex(Constants.BackName), 
				textureManager.getTex(Constants.BackName));
		
		achieveView = createNewTexture();
		achieveView.setTouchTexture(textureManager.getTex(Constants.AchieveName), 
				textureManager.getTex(Constants.AchieveName));
		achieveView.setClickedListener(this);
		
		rankView = createNewTexture();
		rankView.setTouchTexture(textureManager.getTex(Constants.LeaderboardName), 
				textureManager.getTex(Constants.LeaderboardName));
		rankView.setClickedListener(this);
		
		retryView = createNewTexture();
		retryView.setTouchTexture(textureManager.getTex(Constants.RetryName), 
				textureManager.getTex(Constants.RetryName));
		retryView.setClickedListener(this);
		
		shieldView = createNewTexture();
		shieldView.setIsTex(false);
		shieldView.setTouchColor(ColorUtil.Black, ColorUtil.Black);
		
		resView = createNewTexture();
		
		numberViews = new GlTextureView[16];
		
		for (int i = 0; i < 16; i ++) {
			numberViews[i] = createNewTexture();
			numberViews[i].setDrawNothing(true);
		}
		
		currScoreView = createNewText();
		currStepView = createNewText();
		bestScoreView = createNewText();
		
		timer = new Timer();
		mover1 = new GlControlSimTrans();
		mover2 = new GlControlSimTrans();
		mover3 = new GlControlSimTrans();
		scaler2 = new GlControlSimScale();
		scaler1 = new GlControlSimScale();
		alphaer = new GlControlSimAlpha();
	}

	private void setEffectParam(int dir) {
		timer.setStart(moveSumTime, Controler.Single);
		scaler1.setStartAndEnd(1f, TileScale, Controler.Double);
		scaler2.setStartAndEnd(0f, 1f, Controler.Single);
		float ex = 0, ey = 0;
		if (dir == GameManager.Up) {
			ey = TileMoveY * this.height;
		} else if (dir == GameManager.Right) {
			ex = TileMoveX * this.width;
		} else if (dir == GameManager.Down) {
			ey = -TileMoveY * this.height;
		} else {
			ex = -TileMoveX * this.width;
		}
		mover1.setStartAndEnd(0, 0, 0, ex, ey, 0, Controler.Single);
		mover2.setStartAndEnd(0, 0, 0, ex * 2, ey * 2, 0, Controler.Single);
		mover3.setStartAndEnd(0, 0, 0, ex * 3, ey * 3, 0, Controler.Single);
	}
	
	public void popResult() {
		Log.d("test", "pop result");
		timer.setStart(moveOverTime, Controler.Single);
		scaler1.setStartAndEnd(ShieldScale, 1f, Controler.Single);
		alphaer.setStartAndEnd(0, ShieldAlpha, Controler.Single);
		
		shieldView.register(scaler1);
		shieldView.register(alphaer);
		resView.register(scaler1);
		//resView.register(alphaer);
		
		scaler1.activeSelf();
		alphaer.activeSelf();
		
		if (gameManager.isWin()) {
			resView.setTouchTexture(textureManager.getTex(Constants.WinName),
					textureManager.getTex(Constants.WinName));
		} else {
			resView.setTouchTexture(textureManager.getTex(Constants.LostName),
					textureManager.getTex(Constants.LostName));
		}
		updateManager.addUpdate(shieldView, false, false);
		updateManager.addUpdate(resView, false, false);
		shieldView.update();
		resView.update();
		
		drawManage.add(shieldView.getGlDrawer(), false);
		drawManage.add(resView.getGlDrawer(), false);
		
		timer.getControler().getTriggleInfo().clear();
		timer.getControler().getTriggleInfo().event = Controler.Resume;
		timer.getControler().getTriggleInfo().overEvent = popResOverEvent;
		timer.getControler().getTriggleInfo().processEvent = popResProcessEvent;
		timer.getControler().triggle(true);
	}
	
	private final ProcessEvent popResProcessEvent = new ProcessEvent() {
		
		@Override
		public void triggle(float process) {
			scaler1.getControler().setProcess(process);
			alphaer.getControler().setProcess(process);
		}
	};
	
	private final OverEvent popResOverEvent = new OverEvent() {
		
		@Override
		public void triggle() {
			scaler1.unRegisterAll();
			//alphaer.unRegisterAll();
			runState = OverState;
		}
	};
	
	private final OverEvent effectOverEvent = new OverEvent() {
		
		@Override
		public void triggle() {
			if (!moveOver) {
				mover1.unRegisterAll();
				mover2.unRegisterAll();
				mover3.unActiveSelf();
				setNumberViews();
			}
			scaler1.unRegisterAll();
			scaler2.unRegisterAll();
			//gameStateManager.getHandler().sendEmptyMessage(ToofStateManager.PopResult);
			if (gameManager.isWin()) {
				gameStateManager.getHandler().sendEmptyMessage(SimimStateManager.PopResult);
			} else if (gameManager.isOver()) {
				gameStateManager.getHandler().sendEmptyMessage(SimimStateManager.PopResult);
			} else {
				runState = RunState;
			}
		}
	};
	
	private final ProcessEvent effectProcessEvent = new ProcessEvent() {
		
		@Override
		public void triggle(float process) {
			if (!moveOver) {
				if (process > moveProcess) {
					moveOver = true;
					mover1.unRegisterAll();
					mover2.unRegisterAll();
					mover3.unActiveSelf();
					setNumberViews();
				} else {
					process = (float)Math.pow(process / moveProcess, 2);
					mover1.getControler().setProcess(process);
					mover2.getControler().setProcess(process);
					mover3.getControler().setProcess(process);
				}
			} else {
				process = (process - moveProcess) / (1- moveProcess);
				scaler1.getControler().setProcess(process);
				scaler2.getControler().setProcess(process);
			}
		}
	};
	
	private boolean moveOver = false;
	private static final float moveProcess = (float)moveTransTime / (float)moveSumTime;
	private void effect(TileMoveInfo[] moveInfos, int dir) {
		if (moveInfos != null) {
			runState = NoneState;
			
			moveOver = false;
			setEffectParam(dir);
			for (int i = 0; i < 16; i++) {
				if (moveInfos[i].dis == 1) {
					numberViews[i].register(mover1);
				} else if (moveInfos[i].dis == 2) {
					numberViews[i].register(mover2);
				} else if (moveInfos[i].dis == 3) {
					numberViews[i].register(mover3);
				}
				if (moveInfos[i].dis != 0) {
					numberViews[i].activeState(mover1.getState());
				}
				if (moveInfos[i].isNew) {
					numberViews[i].register(scaler1);
					numberViews[i].activeState(scaler1.getState());
				} else if (moveInfos[i].isNever) {
					numberViews[i].register(scaler2);
					numberViews[i].activeState(scaler2.getState());
				}
				numberViews[i].update();
			}
			timer.getControler().getTriggleInfo().clear();
			timer.getControler().getTriggleInfo().event = Controler.Resume;
			timer.getControler().getTriggleInfo().overEvent = effectOverEvent;
			timer.getControler().getTriggleInfo().processEvent = effectProcessEvent;
			timer.getControler().triggle(true);
		}
	}
		
	@Override
	public void onMove(GlEventView view, Event e, boolean isScroll) {
		if (runState == RunState && !isScroll) {
			boolean isVer = Math.abs(e.x) < Math.abs(e.y);
			boolean isDir = isVer ? e.y > 0 : e.x < 0;
			int dir = GameManager.Up;
			if (isVer && isDir) {
				dir = GameManager.Up;
			} else if (isVer && !isDir) {
				dir = GameManager.Down;
			} else if (isDir) {
				dir = GameManager.Left;
			} else {
				dir = GameManager.Right;
			}
			effect(gameManager.move(dir), dir);
		}
	}

	private void startNewGame() {
		alphaer.unRegisterAll();
		updateManager.removeUpdate(shieldView, false);
		updateManager.removeUpdate(resView, false);
		drawManage.remove(shieldView.getGlDrawer(), false);
		drawManage.remove(resView.getGlDrawer(), false);
		
		gameManager.reset();
		setNumberViews();
		runState = RunState;
	}
	
	@Override
	public void onClicked(GlEventView view, Event e, boolean isDouble) {
		if (view == retryView) {
			if (runState == RunState) {
				gameManager.reset();
				setNumberViews();
				//popResult();
			} else if (runState == OverState) {
				startNewGame();
			}
		} else if (view == achieveView || view == rankView) {
			Message msg = new Message();
			msg.what = SimimActivity.ShowTip;
			msg.arg1 = R.string.unsed;
			getActivity().getHandler().sendMessage(msg);
		}
	}
	
	@Override
	public void setScreenSize() {
		glScreen.setProjMatrix2D(this.width, this.height, true);
		glScreen.confirmAll();
		glScreen.update();
		
		touchView.setWHXY(this.width, this.height, 0, 0);
		touchView.setZ(TouchZ);
		touchView.update();
		
		backView.setWHXY(this.width, this.height, 0, 0);
		backView.setZ(BackZ);
		backView.update();
		
		achieveView.setWHLeftXY(0.204f * this.width, 0.117f * this.height,
									0.063f * this.width - this.width / 2,
									(1 - 0.751f - 0.5f) * this.height);
		achieveView.setZ(UIZ);
		//achieveView.update();
		
		rankView.setWHLeftXY(0.204f * this.width, 0.117f * this.height,
									0.295f * this.width - this.width / 2,
									(1 - 0.751f - 0.5f) * this.height);
		rankView.setZ(UIZ);
		
		retryView.setWHLeftXY(0.236f * this.width, 0.135f * this.height,
									(0.706f - 0.5f) * this.width,
									(1 - 0.733f - 0.5f) * this.height);
		retryView.setZ(RetryZ);
		
		bestScoreView.setWHLeftXY(0.302f * this.width, 0.025f * this.height,
									(0.613f - 0.5f) * this.width, 
									(1 - 0.043f - 0.5f) * this.height);
		bestScoreView.setZ(UIZ);
		bestScoreView.initTextTexture();
		//bestScoreView.getTextureText().setTextBold(true);
		bestScoreView.getTextureText().setTextColor(Color.WHITE);
		bestScoreView.getTextureText().setTextSize(bestScoreView.getHeight(true) / 1.1f);
		bestScoreView.getTextureText().setTextFont(FontManager.getFontManager()
				.getFont(Constants.CarFontName).getFont(context));
		bestScoreView.setTextInterface(new TextInterface() {
			@Override
			public String getText() {
				return "" + gameManager.getBestScore();
			}
		});
		bestScoreView.update();
		
		currScoreView.setWHLeftXY(0.15f * this.width, 0.025f * this.height,
				(0.598f - 0.5f) * this.width, 
				(1 - 0.115f - 0.5f) * this.height);
		currScoreView.setZ(UIZ);
		currScoreView.initTextTexture();
		//currScoreView.getTextureText().setTextBold(true);
		currScoreView.getTextureText().setTextColor(Color.WHITE);
		currScoreView.getTextureText().setTextSize(currScoreView.getHeight(true) / 1.1f);
		currScoreView.getTextureText().setTextFont(FontManager.getFontManager()
				.getFont(Constants.CarFontName).getFont(context));
		currScoreView.setTextInterface(new TextInterface() {
			@Override
			public String getText() {
				return "" + gameManager.getCurrScore();
			}
		});
		currScoreView.update();
		
		currStepView.setWHLeftXY(0.14f * this.width, 0.025f * this.height,
				(0.788f - 0.5f) * this.width, 
				(1 - 0.115f - 0.5f) * this.height);
		currStepView.setZ(UIZ);
		currStepView.initTextTexture();
		//currStepView.getTextureText().setTextBold(true);
		currStepView.getTextureText().setTextColor(Color.WHITE);
		currStepView.getTextureText().setTextFont(FontManager.getFontManager()
							.getFont(Constants.CarFontName).getFont(context));
		currStepView.getTextureText().setTextSize(currStepView.getHeight(true) / 1.1f);
		currStepView.setTextInterface(new TextInterface() {
			@Override
			public String getText() {
				return "" + gameManager.getCurrStep();
			}
		});
		currStepView.update();
		
		shieldView.setWHXY(this.width, this.height, 0, 0);
		shieldView.setZ(ShieldZ);
		
		resView.setWHXY(this.width, this.height, 0, 0);
		resView.setZ(ResZ);
		
		float startZ = TileZ;
		for (int i = 0; i < 4; i++) {
			float startX = TileX;
			float startY = TileY - i * TileMoveY;
			for (int j = 0; j < 4; j++) {
				numberViews[i * 4 + j].setWHXY(0.163f * this.width, 0.091f * this.height,
														(startX - 0.5f) * this.width, 
														(startY - 0.5f) * this.height);
				numberViews[i * 4 + j].setZ(startZ);
				startZ += TileIntervalZ;
				startX += TileMoveX;
			}
		}
		//mover1.setStartAndEnd(sx, sy, sz, ex, ey, ez, mode);
	}

	@Override
	public void runSelf(long interval) {
		updateManager.deal(interval);
	}

	@Override
	public void drawSelf() {
		GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        drawManage.draw();
        GLES20.glDisable(GLES20.GL_BLEND);
	}

	private void debugNumbers(SecretNumber[] data) {
		Log.d("test", "debug numbers");
		for (int i = 0; i < 4; i++) {
			String tmp = "";
			for (int j = 0; j < 4; j++) {
				tmp += data[i * 4 + j].getNumber() + " ";
			}
			Log.d("test", tmp);
		}
	}
	
	private void setNumberViews() {
		SecretNumber[] data = gameManager.getData();
		debugNumbers(data);
		for (int i = 0; i < 16; i++) {
			if (data[i].getNumber() != 0) {
				numberViews[i].setTouchTexture(textureManager.getTex(Constants.NumberName + data[i].getNumber()), 
						textureManager.getTex(Constants.NumberName + data[i].getNumber()));
				updateManager.addUpdate(numberViews[i], false, false);
				numberViews[i].update();
				numberViews[i].setDrawNothing(false);
				//drawManage.add(numberViews[i].getGlDrawer(), false);
			}
		}
		for (int i = 0; i < 16; i++) {
			if (data[i].getNumber() == 0) {
				updateManager.removeUpdate(numberViews[i], false);
				numberViews[i].setDrawNothing(true);
				//drawManage.remove(numberViews[i].getGlDrawer(), false);
			}
		}
	}

	@Override
	public void init() {
		runState = RunState;
		updateManager.addUpdate(achieveView, false, false);
		updateManager.addUpdate(rankView, false, false);
		updateManager.addUpdate(retryView, false, false);
		
		updateManager.addCalcu(timer.getControler(), false, false);
		
		eventManager.add(touchView.getToucher(), false);
		eventManager.add(achieveView.getToucher(), false);
		eventManager.add(rankView.getToucher(), false);
		eventManager.add(retryView.getToucher(), false);
		
		drawManage.add(backView.getGlDrawer(), false);
		drawManage.add(touchView.getGlDrawer(), false);
		drawManage.add(achieveView.getGlDrawer(), false);
		drawManage.add(rankView.getGlDrawer(), false);
		drawManage.add(retryView.getGlDrawer(), false);
		drawManage.add(bestScoreView.getGlDrawer(), false);
		drawManage.add(currScoreView.getGlDrawer(), false);
		drawManage.add(currStepView.getGlDrawer(), false);

		for (int i = 0; i < 16; i ++) {
			drawManage.add(numberViews[i].getGlDrawer(), false);
		}
		setNumberViews();
		if (gameManager.isWin() || gameManager.isOver()) {
			runState = NoneState;
			gameStateManager.getHandler().sendEmptyMessageDelayed(SimimStateManager.PopResult, 100);
		}
	}

	@Override
	public void uninit() {
		updateManager.clear();
		drawManage.clear();
		eventManager.clear();
	}

	@Override
	public void onResume() {
		//do nothing
	}

	@Override
	public void onPause() {
		//do nothing
	}

	@Override
	public void resetTouchState() {
		//do nothing
	}

	@Override
	public EventHander isHanderEventSelf(Event e) {
		if (runState == NoneState) {
			return null;
		} else {
			if (e.action == CommonEvent.Back) {
				return this;
			} else {
				return eventManager.isHanderEvent(e);
			}
		}
	}

	@Override
	public int onHanderEventSelf(Event e) {
		if (e.action == CommonEvent.Back) {
			if (runState == RunState) {
				getActivity().getHandler().sendEmptyMessage(SimimActivity.MoveBack);
			} else if (runState == OverState) {
				startNewGame();
			}
			return CommonEvent.None;
		} else {
			return eventManager.onHanderEvent(e);
		}
	}

}
