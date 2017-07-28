/** 
 * @fileName:LocationHelper.java 
 * @date:2016年7月22日 
 * @author:ChengFu
 * @Copyright:
 */
package com.canyu.gaodemap;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * 高德地图定位工具类
 */
public class LocationHelper {
	private LocationCallBack callBack;
	//声明AMapLocationClient类对象
	private AMapLocationClient mLocationClient = null;
	//声明定位回调监听器
	private AMapLocationListener mLocationListener = new MyAMapLocationListener();
	private AMapLocationClientOption mLocationOption;

	public LocationHelper(Context context, LocationCallBack callBack) {
		this.callBack = callBack;
		//初始化定位
		mLocationClient = new AMapLocationClient(context.getApplicationContext());
		//给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(getLocOption());
		//设置定位回调监听
		mLocationClient.setLocationListener(mLocationListener);

	}

	/**
	 * 开始定位
	 */
	public void startLocation() {
		//启动定位
		if (mLocationClient != null) {
			mLocationClient.startLocation();
		}
	}

	/**
	 * 停止定位
	 */
	public void stopLocation() {
		//停止定位
		if (mLocationClient != null) {
			mLocationClient.stopLocation();
		}
	}

	/**
	 * 销毁定位
	 */
	public void destroyLocation() {
		if (mLocationClient != null) {
			mLocationClient.unRegisterLocationListener(mLocationListener);
			mLocationClient.stopLocation();
			mLocationClient.onDestroy();
		}
		mLocationClient = null;
		mLocationOption = null;
	}

	private AMapLocationClientOption getLocOption() {
		//初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setNeedAddress(true);
		//设置是否只定位一次,默认为false
		mLocationOption.setOnceLocation(false);
		//可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mLocationOption.setWifiScan(true);
		//设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setMockEnable(false);
		//可选，设置是否使用缓存定位，默认为true
		//mLocationOption.setLocationCacheEnable(true);
		//设置定位间隔,单位毫秒,默认为2000ms
		mLocationOption.setInterval(2000);
		return mLocationOption;
	}

	private class MyAMapLocationListener implements AMapLocationListener{

		@Override
		public void onLocationChanged(AMapLocation location) {
			if (callBack != null){
				callBack.callBack(location);
			}
		}

	}

	public interface LocationCallBack {
		void callBack(AMapLocation location);
	}

}
