package com.canyu.gaodemap;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AMap.OnCameraChangeListener,AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnMarkerDragListener, LocationSource,AMap.OnMapLoadedListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, LocationHelper.LocationCallBack, GeocoderHelper.GeocodeCallBack {

    @Bind(R.id.mapView)
    MapView mapView;

    private AMap aMap;
    private LatLng myLatLng;
    private OnLocationChangedListener mListener;

    public static final int MY_PERMISSIONS_MAP = 0;
    public static final int REQUEST_CODE_SETTING= 300;
    private Marker screenMarker;
    private LocationHelper mLocationHelper;
    private GeocoderHelper mGeocoderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLocationHelper = new LocationHelper(this,this);
        mGeocoderHelper = new GeocoderHelper(this,this);

        //申请权限
        AndPermission.with(this)
                .requestCode(MY_PERMISSIONS_MAP)
                .permission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        // 这里的对话框可以自定义，只要调用rationale.resume()就可以继续申请。
                        AndPermission.rationaleDialog(MainActivity.this, rationale).show();
                    }
                })
                .send();

        initMap();

        mapView.onCreate(savedInstanceState);// 此方法必须重写

    }

    /**
     * 初始化AMap对象
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            //aMap.moveCamera(CameraUpdateFactory.zoomTo(18));//将地图的缩放级别调整到13级
            setUpMap();
        }

    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {

        // 如果要设置定位的默认状态，可以在此处进行设置
        //MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 1.只定位，不进行其他操作
        //aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW));
        //aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
        // 2.设置定位的类型为 跟随模式
        // aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW));
        // 3.设置定位的类型为根据地图面向方向旋转
        // aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE));
        // 4.定位、且将视角移动到地图中心点，定位点依照设备方向旋转，  并且会跟随设备移动。
        //aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE));
        // 5.定位、但不会移动到地图中心点，并且会跟随设备移动。
        //aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER));
        // 6.定位、但不会移动到地图中心点，地图依照设备方向旋转，并且会跟随设备移动。
        //aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER));
        // 7.定位、但不会移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        //aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER));

        //设置自定义定位蓝点
        setupLocationStyle();

        aMap.setLocationSource(this);// 设置定位监听
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMapClickListener(this);//地图点击事件
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        //aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
//        aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
        aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
//        aMap.setOnMapTouchListener(this);// 对amap添加触摸地图事件监听器

        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        uiSettings.setZoomControlsEnabled(true);// 设置缩放按钮是否显示
        uiSettings.setZoomGesturesEnabled(true);//缩放手势
        uiSettings.setScrollGesturesEnabled(true);//滑动手势
        uiSettings.setTiltGesturesEnabled(true);//倾斜手势
        uiSettings.setRotateGesturesEnabled(true);//旋转手势
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

    }

    /**
     * 设置自定义定位蓝点
     */
    private void setupLocationStyle(){
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.mipmap.navi_map_gps_locked));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.argb(180, 3, 145, 255));
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(10, 0, 0, 180));
        // 设置定位的默认状态
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void callBack(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                Toast.makeText(getApplicationContext(), "定位成功！", Toast.LENGTH_SHORT).show();
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                myLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                //点击定位按钮 能够将地图的中心移动到定位点
                //aMap.moveCamera(CameraUpdateFactory.changeLatLng(myLatLng));
                //aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18));
                //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
                //aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(myLatLng, 18, 45, 0)));
                //以动画形式移动到定位点
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(myLatLng, 18, 45, 0)), 1000, null);
                //添加标记
                addMarkersToMap();
            } else {
                Toast.makeText(getApplicationContext(), "定位失败！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "定位失败！amapLocation为null", Toast.LENGTH_SHORT).show();
        }
        //停止定位
        deactivate();
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {

        addMarkerInScreenCenter();

        double latitude = myLatLng.latitude;
        double longitude = myLatLng.longitude;
        LatLng latLng = new LatLng(latitude + 0.5 , longitude + 0.5);
        Marker marker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .position(latLng).title("标记")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .draggable(false)//可拖拽
                .setFlat(true));//将Marker设置为贴地显示，可以双指下拉看效果

//        marker.setRotateAngle(90);// 设置marker旋转90度
          //设置Marker在屏幕上,不跟随地图移动
//        marker.setPositionByPixels(400, 400);
//        marker.showInfoWindow();// 设置默认显示一个infowinfow

        //文字显示标注，可以设置显示内容，位置，字体大小颜色，背景色旋转角度,Z值等
        TextOptions textOptions = new TextOptions().position(new LatLng(39.90403, 116.407525))
                .text("Text").fontColor(Color.BLACK)
                .backgroundColor(Color.BLUE).fontSize(30).rotate(20).align(Text.ALIGN_CENTER_HORIZONTAL, Text.ALIGN_CENTER_VERTICAL)
                .zIndex(1.f).typeface(Typeface.DEFAULT_BOLD);
        aMap.addText(textOptions);
    }

    /**
     * 在屏幕中心添加一个Marker
     */
    private void addMarkerInScreenCenter() {
        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        screenMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f,0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.purple_pin)));
        //设置Marker在屏幕上,不跟随地图移动
        screenMarker.setPositionByPixels(screenPosition.x,screenPosition.y);

    }

    /**
     * 监听amap地图加载成功事件回调
     */
    @Override
    public void onMapLoaded() {
        Toast.makeText(this, "地图加载成功", Toast.LENGTH_SHORT).show();
        // 设置所有maker显示在当前可视区域地图中
//        LatLngBounds bounds = new LatLngBounds.Builder()
//                .include(XIAN).include(CHENGDU)
//                .include(ZHENGZHOU).include(BEIJING).build();
//        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
    }

    /**
     * 地图的点击事件
     */
    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(this, "您点击了地图", Toast.LENGTH_SHORT).show();
    }

    /**
     * 对marker标注点点击响应事件
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        Toast.makeText(this, "你点击的是" + marker.getTitle(), Toast.LENGTH_SHORT).show();

        return false;
    }
    //================================================
    /**
     * 监听开始拖动marker事件回调
     */
    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(this, marker.getTitle() + "开始拖动", Toast.LENGTH_SHORT).show();
    }
    /**
     * 监听拖动marker时事件回调
     */
    @Override
    public void onMarkerDrag(Marker marker) {
        String curDes = marker.getTitle() + "拖动时当前位置:(lat,lng)\n("
                + marker.getPosition().latitude + ","
                + marker.getPosition().longitude + ")";
        Toast.makeText(this, marker.getTitle() + curDes, Toast.LENGTH_SHORT).show();
    }
    /**
     * 监听拖动marker结束事件回调
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        Toast.makeText(this, marker.getTitle() + "停止拖动", Toast.LENGTH_SHORT).show();
    }
    //=================================================

    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 监听点击infowindow窗口事件回调
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "你点击了infoWindow窗口" + marker.getTitle(), Toast.LENGTH_SHORT).show();
    }
    /**
     * 对正在移动地图事件回调
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i("TAG", "onCameraChange: cameraPosition="+cameraPosition.target);
        Log.i("TAG", "onCameraChange: screenMarker="+screenMarker.getPosition());
    }
    /**
     * 对移动地图结束事件回调
     */
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.i("TAG", "onCameraChangeFinish: cameraPosition="+cameraPosition.target);
        Log.i("TAG", "onCameraChangeFinish: screenMarker="+screenMarker.getPosition());
        startJumpAnimation();
        mGeocoderHelper.getAddress(new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude));
    }

    /**
     *
     * 响应逆地理编码回调
     */
    @Override
    public void onGeocodeSearched(LatLng latLng) {

    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onRegeocodeSearched(String address) {
        Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
    }

    /**
     * 屏幕中心marker 跳动
     */
    public void startJumpAnimation() {

        if (screenMarker != null ) {
            //根据屏幕距离计算需要移动的目标点
            final LatLng latLng = screenMarker.getPosition();
            Point point =  aMap.getProjection().toScreenLocation(latLng);
            point.y -= dip2px(this,125);
            LatLng target = aMap.getProjection().fromScreenLocation(point);
            //使用TranslateAnimation,填写一个需要移动的目标点
            Animation animation = new TranslateAnimation(target);
            animation.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    // 模拟重加速度的interpolator
                    if(input <= 0.5) {
                        return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                    } else {
                        return (float) (0.5f - Math.sqrt((input - 0.5f)*(1.5f - input)));
                    }
                }
            });
            //整个移动所需要的时间
            animation.setDuration(600);
            //设置动画
            screenMarker.setAnimation(animation);
            //开始动画
            screenMarker.startAnimation();

        } else {
            Log.e("ama","screenMarker is null");
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.mListener = onLocationChangedListener;
        mLocationHelper.startLocation();
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mLocationHelper.stopLocation();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mLocationHelper.startLocation();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        deactivate();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationHelper.destroyLocation();
        mapView.onDestroy();
        ButterKnife.unbind(this);
    }

    //----------------------------------权限回调处理----------------------------------//

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
    }

    private PermissionListener listener = new PermissionListener() {

        @Override
        public void onSucceed(int requestCode, List<String> grantPermissions) {
            // 权限申请成功回调。
            switch (requestCode) {
                case MY_PERMISSIONS_MAP:
                    //开始定位
                    mLocationHelper.startLocation();
                    break;
            }

        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。

            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, deniedPermissions)) {
                // 第一种：用默认的提示语。
                AndPermission.defaultSettingDialog(MainActivity.this, REQUEST_CODE_SETTING).show();

            }

        }
    };

    //dip和px转换
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
