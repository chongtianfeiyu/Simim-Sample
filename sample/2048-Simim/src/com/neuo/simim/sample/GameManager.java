package com.neuo.simim.sample;

import com.neuo.common.MathUtil;
import com.neuo.common.SecretNumber;

public class GameManager {
	private SecretNumber[] data;
	private SecretNumber currStep;
	private SecretNumber currScore;
	private SecretNumber bestScore;
	private TileMoveInfo[] moveInfos;
	
	private static int[] initProb = new int[]{100, 10, 5, 1};
	static {
		for (int i = 1; i < initProb.length; i ++) {
			initProb[i] = initProb[i] + initProb[i - 1];
		}
	}
	
	private static final int initCount = 2;
	
	public static class TileMoveInfo {
		public int dis;
		public boolean isNew;
		public boolean isNever;
		
		public void clear() {
			dis = 0;
			isNew = false;
			isNever = false;
		}
	}
	
	public static final int Up = 0;
	public static final int Right = 1;
	public static final int Down = 2;
	public static final int Left = 3;
	
	public GameManager() {
		init();
	}
	
	private void init() {
		bestScore = new SecretNumber();
		currScore = new SecretNumber();
		currStep = new SecretNumber();
		bestScore.setNumber(0);
		data = new SecretNumber[16];
		moveInfos = new TileMoveInfo[16];
		for (int i = 0; i < 16; i ++) {
			data[i] = new SecretNumber();
			moveInfos[i] = new TileMoveInfo();
		}
		//reset();
	}
	
	public boolean isTwo1024() {
		int count = 0;
		for (int i = 0; i < 16; i++) {
			if (data[i].getNumber() == 1024) {
				count++;
			}
		}
		return count >= 2;
	}
	
	public int getHighestScore() {
		int max = 0;
		for (int i = 0; i < 16; i++) {
			if (data[i].getNumber() > max) {
				max = (int)data[i].getNumber();
			}
		}
		return max;
	}
	
	public SecretNumber[] getData() {
		return data;
	}
	
	public int getBestScore() {
		return (int)bestScore.getNumber();
	}
	
	public int getCurrScore() {
		return (int)currScore.getNumber();
	}
	
	public int getCurrStep() {
		return (int)currStep.getNumber();
	}
	
	public void setData(SecretNumber[] data, int currScore, int currStep,
							int bestScore) {
		for (int i = 0; i < 16; i++) {
			this.data[i].setNumber(data[i].getNumber());
		}
		this.currScore.setNumber(currScore);
		this.currStep.setNumber(currStep);
		this.bestScore.setNumber(bestScore);
	//	Log.d("test", "" );
	}
	
	private int getInitNumber() {
		int index = 0;
		int r = Math.abs(MathUtil.random.nextInt()) % initProb[initProb.length - 1];
		for (int i = 0; i < initProb.length; i++) {
			if (r < initProb[i]) {
				index = i;
				break;
			}
		}
		return MathUtil.pow(2, index + 1);
	}
	
	public void reset() {
		for (int i = 0; i < 16; i++) {
			data[i].setNumber(0);
		}
		currScore.setNumber(0);
		currStep.setNumber(0);

		for (int i = 0; i < initCount; i++) {
			generaNewNumber();
		}
	}
	
	private int generaNewNumber() {
		int index = Math.abs(MathUtil.random.nextInt()) % 16;
		while (data[index].getNumber() != 0) {
			index = Math.abs(MathUtil.random.nextInt()) % 16;
		}
		data[index].setNumber(getInitNumber());
		return index;
	}
	
	public boolean isWin() {
		for (int i = 0; i < 16; i++) {
			if (data[i].getNumber() >= 2048) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isOver() {
		for (int i = 0; i < 4; i++) {
			if (canMove(i)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean lineCanMove(int start, int interval) {
		int pre = (int)data[start].getNumber();
		for (int i = 1; i < 4; i++) {
			int cur = (int)data[start + i * interval].getNumber();
			if (pre == 0 && cur != 0) {
				return true;
			} else if (pre != 0 && cur == 0) {
				pre = 0;
			} else if (pre != 0 && cur != 0) {
				if (pre == cur) {
					return true;
				} else {
					pre = cur;
				}
			} else {
				//do nothing
			}
		}
		return false;
	}
	
	private boolean canMove(int dir) {
		int interval = 1;
		int start = 0;
		int startInterval = 3;
		switch (dir) {
		case Up:
			start = 0;
			startInterval = 1;
			interval = 4;
			break;
		case Right:
			start = 3;
			startInterval = 4;
			interval = -1;
			break;
		case Down:
			start = 12;
			startInterval = 1;
			interval = -4;
			break;
		case Left:
			start = 0;
			startInterval = 4;
			interval = 1;
			break;
		default:
			throw new RuntimeException("invalid dir " + dir);
		}
		for (int i = 0; i < 4; i ++) {
			if (lineCanMove(start + i * startInterval, interval)) {
				return true;
			}
		}
		return false;
	}
	
	public void clearMoveInfos() {
		for (int i = 0; i < 16; i++) {
			moveInfos[i].clear();
		}
	}
	
	private void addScore(int add) {
		currScore.addNumber(add);
		if (currScore.getNumber() > bestScore.getNumber()) {
			bestScore.setNumber(currScore.getNumber());
		}
	}
	
	public void lineMove(int start, int interval) {
		int moveDis = 0;
		int pre = (int)data[start].getNumber();
		if (pre == 0) {
			moveDis = 1;
		}
		int preIndex = start;
		for (int i = 1; i < 4; i++) {
			int index = start + i * interval;
			int cur = (int)data[index].getNumber();
			if (pre == 0 && cur != 0) {
				moveInfos[index].dis = moveDis;
				pre = cur;
				preIndex = index - moveDis * interval;
				data[index].setNumber(0);
				data[preIndex].setNumber(cur);
			} else if (pre != 0 && cur == 0) {
				moveDis++;
			} else if (pre != 0 && cur != 0) {
				if (pre == cur) {
					moveDis++;
					moveInfos[index].dis = moveDis;
					moveInfos[preIndex].isNew = true;
					pre = 0;
					data[index].setNumber(0);
					data[preIndex].setNumber(cur * 2);
					addScore(cur);
				} else {
					pre = cur;
					moveInfos[index].dis = moveDis;
					preIndex = index - moveDis * interval;
					data[index].setNumber(0);
					data[preIndex].setNumber(cur);
				}
			} else {
				moveDis++;
			}
		}
	}
	
	public TileMoveInfo[] move(int dir) {
		if (!canMove(dir)) {
			return null;
		}
		clearMoveInfos();
		int start = 0;
		int startInterval = 1;
		int interval = 1;
		switch (dir) {
		case Up:
			start = 0;
			startInterval = 1;
			interval = 4;
			break;
		case Right:
			start = 3;
			startInterval = 4;
			interval = -1;
			break;
		case Down:
			start = 12;
			startInterval = 1;
			interval = -4;
			break;
		case Left:
			start = 0;
			startInterval = 4;
			interval = 1;
			break;
		}
		for (int i = 0; i < 4; i ++) {
			lineMove(start + i * startInterval, interval);
		}
		moveInfos[generaNewNumber()].isNever = true;
		currStep.addNumber(1);
		return moveInfos;
	}
}
 
