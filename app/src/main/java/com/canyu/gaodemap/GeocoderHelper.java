/**
 * @fileName:GeocoderHelper.java
 * @date:2016年7月22日
 * @author:ChengFu
 * @Copyright:
 */
package com.canyu.gaodemap;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

public class GeocoderHelper {
	private Context context;
	private ProgressDialog progDialog;
	private GeocodeSearch geocoderSearch;
	private OnGeocodeSearchListener listener = new MyOnGeocodeSearchListener();
	private GeocodeCallBack callBack;

	public GeocoderHelper(Context context, GeocodeCallBack callBack){
		this.context = context;
		this.callBack = callBack;
		geocoderSearch = new GeocodeSearch(context);
		geocoderSearch.setOnGeocodeSearchListener(listener);
		progDialog = new ProgressDialog(context);
	}

	/**
	 * 显示进度条对话框
	 */
	public void showDialog() {
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage("正在获取地址");
		progDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}

	/**
	 * 响应地理编码
	 */
	public synchronized void getLatlon(String name) {
		showDialog();
		GeocodeQuery query = new GeocodeQuery(name, null);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
		geocoderSearch.getFromLocationNameAsyn(query);// 设置异步地理编码请求
	}

	/**
	 * 响应逆地理编码
	 */
	public synchronized void getAddress(LatLonPoint latLonPoint) {
		showDialog();
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
	}

	class MyOnGeocodeSearchListener implements OnGeocodeSearchListener{
		/**
		 * 地理编码查询回调
		 */
		@Override
		public void onGeocodeSearched(GeocodeResult result, int rCode) {
			dismissDialog();
			if (rCode == AMapException.CODE_AMAP_SUCCESS) {
				if (result != null && result.getGeocodeAddressList() != null
						&& result.getGeocodeAddressList().size() > 0) {
					GeocodeAddress address = result.getGeocodeAddressList().get(0);
//					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//							AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
//					geoMarker.setPosition(AMapUtil.convertToLatLng(address
//							.getLatLonPoint()));
					String addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
							+ address.getFormatAddress();
					Toast.makeText(context, addressName, Toast.LENGTH_SHORT).show();
					if(callBack != null){
						callBack.onGeocodeSearched(new LatLng(address.getLatLonPoint().getLatitude(), address.getLatLonPoint().getLongitude()));
					}
				} else {
					Toast.makeText(context, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context, rCode, Toast.LENGTH_SHORT).show();
			}

		}
		/**
		 * 逆地理编码回调
		 */
		@Override
		public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
			dismissDialog();
			if (rCode == AMapException.CODE_AMAP_SUCCESS) {
				if (result != null && result.getRegeocodeAddress() != null
						&& result.getRegeocodeAddress().getFormatAddress() != null) {
					String addressName = result.getRegeocodeAddress().getFormatAddress()
							+ "附近";
//					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//							AMapUtil.convertToLatLng(latLonPoint), 15));
//					regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
					Toast.makeText(context, addressName, Toast.LENGTH_SHORT).show();
					if(callBack != null){
						callBack.onRegeocodeSearched(addressName);
					}
				} else {
					Toast.makeText(context, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context, rCode, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public interface GeocodeCallBack {
		void onGeocodeSearched(LatLng latLng);
		void onRegeocodeSearched(String address);
	}
}
