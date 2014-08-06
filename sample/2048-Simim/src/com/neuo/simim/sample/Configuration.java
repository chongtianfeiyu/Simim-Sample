package com.neuo.simim.sample;

import com.neuo.common.SecretNumber;
import com.neuo.common.AsyncTaskProcessor.AsyncTask;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Configuration {

	private static Configuration config;
	private SharedPreferences preferences;
	
	
	private static final String DataName = "Data";
	private static final String defaultDataString = "0";
	private SecretNumber[] data;
	private boolean isDataChange = false;
	
	@SuppressWarnings("unused")
	private AsyncTask persistTask;
	
	public static void release() {
		config = null;
	}

	public static Configuration getConfig(){
		return config;
	}
	
	public static Configuration initConfig(SharedPreferences p) {
		if (null == config) {
			config = new Configuration(p);
		}
		return config;
	}
	
	private Configuration(SharedPreferences p) {
		preferences = p;
		data = new SecretNumber[19];
		for (int i = 0; i < 19; i++) {
			data[i] = new SecretNumber();
		}
		initLoad();
		persistTask = new AsyncTask() {
			{
				setName("configureAysncTask");
			}
			@Override
			protected void runnable() {
				persistData();
			}
		};	
		debug();
	}

	public void debug() {
		Log.d("test", "configure debug");
		for (int i = 0; i < 4; i++) {
			String tmp = "";
			for (int j = 0; j < 4; j++) {
				tmp += data[i * 4 + j].getNumber() + " ";
			}
			Log.d("test", tmp);
		}
		
		Log.d("test", "" + data[16].getNumber());
		Log.d("test", "" + data[17].getNumber());
		Log.d("test", "" + data[18].getNumber());
	}
	
	public void setData(SecretNumber[] data, int currScore, int currStep, int bestScore) {
		for (int i = 0; i < 16; i++) {
			this.data[i].setNumber(data[i].getNumber());;
		}
		this.data[16].setNumber(currScore);
		this.data[17].setNumber(currStep);
		this.data[18].setNumber(bestScore);
		isDataChange = true;
	}
	
	public SecretNumber[] getData() {
		return data;
	}
	
	public void persistData() {
		synchronized (this) {
			//do nothing
		}
	}
	

	public void persistGameData() {
		if (isDataChange) {
			isDataChange = false;
			Editor editor = preferences.edit();
			String dataString = "";
			for (int i = 0; i < 19; i++) {
				if (i != 0) {
					dataString += ":";
				}
				if (data[i].getNumber() > 0) {
					dataString += data[i].getNumber();
				} else {
					dataString += "0";
				}
			}
			editor.putString(DataName, dataString);
			editor.commit();
		}
	}
	
	private void initLoad() {
		loadData();
	}

	private void loadData() {
		String[] node = preferences.getString(DataName, defaultDataString).split(":");
		for (int i = 0; i < 19 && i < node.length; i++) {
			if (node[i].equals("0")) {
				data[i].setNumber(0);;
			} else {
				data[i].setNumber(Integer.parseInt(node[i]));
			}
		}
		for (int i = node.length; i < 19; i++) {
			data[i].setNumber(0);
		}
	}

}
