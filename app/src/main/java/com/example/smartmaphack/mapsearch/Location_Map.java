package com.example.smartmaphack.mapsearch;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartmaphack.R;
import com.example.smartmaphack.scheduler.Location_Register;
import com.example.smartmaphack.settings.Location_Settings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

//https://inforyou.tistory.com/36
//http://maps.google.com/maps?saddr=37.2670887,127.1567374&daddr=,
//https://developers.google.com/places/web-service/supported_types?hl=ko
//https://www.google.co.kr/maps/dir/A1,B1/A1,B1/
public class Location_Map extends AppCompatActivity implements
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {       //지도 시스템 구현할 예정

    int btnIndex;


    final int DISABLE = 0, ON = 1, OFF = 2;
    int locCount = 0;

    Animation fadeIn, fadeOut;

    Marker item = null;
    List<Marker> btnPlace1;
    List<Marker> btnPlace2;
    List<Marker> btnPlace3;
    List<Marker> btnPlace4;

    List<Marker> previous_marker = null;
    List<Marker> idxMarker = null;
    private GoogleMap mMap;
    private Marker currentMarker = null;    //디폴트 마커와 나의 현재 위치를 지정할 마커 변수를 선언.

    Integer[] markerID = {R.drawable.marker2, R.drawable.marker3, R.drawable.marker4, R.drawable.marker5};

    Integer[] btnColor = {0xFFda915b, 0xFFdabb5b, 0xFFa4da5b, 0xFF5ba4da};
    boolean isLocationCallBack = false;
    boolean isMapReady = false;

    private final String API_KEY = "";
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    String Url = "";

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    //위치정보 획득 권한 / 대략적인 위치 정보 획득.

    Location mCurrentLocation;      //사용자의 위치 정보를 가져와 저장하는 객체.
    LatLng currentPosistion;        //입력된 위,경도 좌표값에 따른 위치정보를 정의하는 클래스

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fabMap, fabMap1, fabMap2;

    private FusedLocationProviderClient mFusedLocationClient;       //기존 FusedLocationPoviderapi는 depricated되고,
    // 업데이트되어 나온 구글 Map 인터페이스
    private LocationRequest locationRequest;                        //Map api과 관련된 구체적인 설정을 하는 객체
    private Location location;

    private View mLayout;       //연결된 xml의 레이아웃 ID를 가져옴.

    Integer[] fBtnId = new Integer[]{R.id.btnType1, R.id.btnType2, R.id.btnType3, R.id.btnType4};
    Button[] fButtons = new Button[4];
    Button btnIam;
    String[] selName = new String[4];
    String[] selType = new String[4];

    ImageView ivRotate, ivBackMap;

    boolean isMarkerClick = false;
    boolean isRefleshClick = true;

    int[] isButtonClick = {DISABLE, DISABLE, DISABLE, DISABLE};

    boolean isLocationCheck = true;

    double prvLng = 0;
    double prvLat = 0;

    int alarmVal, distVal;
    boolean searchCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out);

        btnPlace1 = new ArrayList<>();          //각각의 플레이스를 배열로 저장.
        btnPlace2 = new ArrayList<>();
        btnPlace3 = new ArrayList<>();
        btnPlace4 = new ArrayList<>();

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE); //Map 액티비티 시작 시, 앱 관련 설정값을 읽어온다.

        alarmVal = settings.getInt("alarmVal", 1);
        distVal = settings.getInt("distVal", 1);
        searchCircle = settings.getBoolean("sCircle", true);

        btnIam = findViewById(R.id.btnIam);
        fabMap = findViewById(R.id.fab_Map);
        fabMap1 = findViewById(R.id.fab_Map1);
        fabMap2 = findViewById(R.id.fab_Map2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        ivRotate = findViewById(R.id.ivRotate);
        fabMap.setOnClickListener(this);
        fabMap1.setOnClickListener(this);
        fabMap2.setOnClickListener(this);
        ivBackMap = findViewById(R.id.ivBackMap);

        Snackbar.make(findViewById(R.id.snackView),"위치정보를 읽어오는 중 입니다.\n잠시만 기다려 주세요.",2000).show();

        buttonDecision(settings);

        fabMap.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Snackbar.make(findViewById(R.id.snackView), "현재 마커를 전부 삭제하고, 새로고침 합니다.", 4000).show();
                allReflesh();
                return false;
            }
        });

        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anim);
        ivRotate.startAnimation(anim);

        previous_marker = new ArrayList<>();
        idxMarker = new ArrayList<>();

        for (int i = 0; i < fButtons.length; i++) {       //필터 버튼 클릭 시 이벤트를 처리하는 곳.

            final int index = i;
            fButtons[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPlaceTypeInformation(currentPosistion, selType[index]);
                    //  isRefleshClick = true;
                    isLocationCheck = true;
                    btnIndex = index;
                    btnCheck(index);

                    if (isButtonClick[index] == OFF || isButtonClick[index] == DISABLE) {
                        fButtons[index].setTextColor(Color.WHITE);
                    }
                    breakClickOverlap();
                }
            });
        }

        //앱이 실행중 일 때, 화면이 꺼지지 않도록. = 편의 기능.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //지도가 로딩되는 동안 터치 방지 = > 미리 예외사항 막기.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mLayout = findViewById(R.id.layout_main);

        locationRequest = new LocationRequest()
                .setInterval(0)              //위치가 업데이트 되는 주기.   1000ms = 1초. //해당 시간을 주기로 위치를 업데이트 하기 위해 노력함.
                .setFastestInterval(1000)        //위치 획득 후 업데이트 되는 주기.   500ms = 0.5초 //해당하는 시간 보다 빠르게 위치를 업데이트 하지는 않는다.
                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);   //정확도 <-> 배터리 소모 간 균형 설정. 총 4가지 옵션이 있음.

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();   //기기의 GPS가 꺼져 있을때, 이를 켜는 대화상자를 띄울 수 있도록 하는 기능.
        builder.addLocationRequest(locationRequest);     //위의 설정값을  GPS 요청 대화상자와 연결.


        //위의 선언한 구글 맵 api의 인스턴스. 위치서비스 중, 통합위치정보 제공자의 인스턴스를 자신의 객체에 만들어 가져옴으로써,
        //위치 서비스 클라이언트가 만들어지도록 한다.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        //기존에는 MapVIew+ Activity조합의 배치를 사용하였으나, 구글 맵이 업데이트 되면서, MapFragment가 사용되도록 변경되었다.
        //MapView를 사용해도 되나, 액티비티/ 프래그먼트 간 생애주기 연동작업이 필요하므로, 가급적이면 MapFragment사용을 권장.
        //but Fragment는 안드로이드 3.0부터 등장하였으므로, 2.3(진저브레드)이하 단말기에서는 사용이 불가.
        //구글에서는 이에 따라 별도 라이브러리를 지원하므로 2.3이하 단말기도 프래그먼트 사용이 가능.
        //그러므로 원래는 MapFragment를 사용하나, 구버전 단말기도 배려하여, SupportMapFragment를 사용함.
        //맵 프래그먼트 객체를 생성 -> 매니저를 통하여 프래그먼트 매니저의 인스턴스를 받아, 프래그먼트를 찾는다.
        mapFragment.getMapAsync(this);  //찾아온 프래그먼트를 지도와 동기화(싱크)

    }

    //googleMap값이 Null이 아닐 때, 즉 위치 정보를 보낼 수 있을 때, 호출되어진다.
    //여기서 마커, 카메라 등 위치에 대한 설정을 함.
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");
        mMap = googleMap;   //본체 클래스에 선언한 GoogleMap 객체의 인스턴스가 onMapReady매서드의 googleMap 파라메타와 연결.
        setDefaultLocation();

        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

/*
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            //startLocationUpdates(); // 3. 위치 업데이트 시작

        }
*/
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(Location_Map.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            }
            else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        //UiSettings는 UI와 관련된 설정을 담당하는 클래스. UiSettings의 속성을 변경하면 컨트롤, 제스처 등 UI와 관련된 요소를 제어할 수 있다.
        //그러나, 인스턴스를 직접 생성할 수 없으므로, .getUiSettings()를 호출해서 가져와야 함.
        //하나의 UiSettings 인스턴스는 하나의 MMap 인스턴스와 1:1로 대응하는 원리.
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().isMapToolbarEnabled();
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));//카메라 애니메이션 및 줌인 설정.
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(Location_Map.this, R.style.MyPopup);
                final Marker index;
                index = marker;

                Url = "http://maps.google.com/maps?saddr=" + currentMarker.getPosition().latitude + "," + currentMarker.getPosition().longitude + "&daddr="
                        + index.getPosition().latitude + "," + index.getPosition().longitude;

                dlg.setTitle(index.getTitle());
                dlg.setMessage("\n" + index.getSnippet() + "\n");
                dlg.setNegativeButton("길 찾기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
                        startActivity(intent);

                    }
                });
                dlg.setPositiveButton("확인", null);

                dlg.setNeutralButton("등록하기", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //액티비티 매니저를 통하여, 현재 실행중인 액티비티의 개수를 구함.
                        ActivityManager AM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        assert AM != null;
                        List<ActivityManager.RunningTaskInfo> Info = AM.getRunningTasks(1);
                        int numActivity = Info.get(0).numActivities;
                        Intent intent = new Intent(Location_Map.this, Location_Register.class);

                        intent.putExtra("Latitude", index.getPosition().latitude + "," + currentMarker.getPosition().latitude);
                        intent.putExtra("Longitude", index.getPosition().longitude + "," + currentMarker.getPosition().longitude);
                        intent.putExtra("Snippet", index.getSnippet());
                        intent.putExtra("Location", index.getTitle());
                        intent.putExtra("Url", Url);

                        if (numActivity == 3) {
                            setResult(RESULT_OK, intent);
                        } else {
                            intent.putExtra("Update", true);
                            startActivity(intent);
                        }

                        finish();
                    }
                });

                dlg.show();
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {//맵 클릭에 따른 리스너.
            @Override
            public void onMapClick(LatLng latLng) {
                final double lat = latLng.latitude;
                final double lng = latLng.longitude;
                prvLat = 0;
                prvLng = 0;
                Log.d(TAG, latLng.latitude + ", " + latLng.longitude);                //맵이 클릭되었을 때, 로그를 찍는다.
            }
        });
        mMap.setOnMarkerClickListener(this);

    }

    //지도 API의 핵심 기능이 동작하는 지점.
    //LocationCallBack은 공개 추상 클래스.
    //FusedLocationProviderClient 부터 장치위치가 변경되었거나, 더 이상 확인할 수
    //없을 때를 기점으로 하여, 응답을 받는데 사용하는 매소드.
    //아래의 mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, looper) 메소드가
    //사용되어 위치 클라이언트에 등록되었을 때, LocationCallBack의 하위 매서드가 호출된다.
    LocationCallback locationCallback = new LocationCallback() { //익명 객체 생성 + 오버라이드 방식을 사용함.
        @Override
        //장치 위치 정보를 사용할 수 '있'을때, 호출되는 LocationCallBack 클래스가 보유한 추상 매서드.
        //가능한 한 최신이고, 합리적인 상태를 유지하려 함.
        //아래의 매서드를 호출한다.
        public void onLocationResult(LocationResult locationResult) {
            //해당 매서드가 매개변수로 받는, LocationResult 는 FusedLocationProvider의 지리적 위치 결과를 나타내는 데이터 클래스.
            //getLocation() 매서드에 의해 리턴되는 위치는 유효한 위도,경도,유닉스 시간이 보장된다.
            super.onLocationResult(locationResult);
            ivRotate.clearAnimation();
            ivRotate.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            if (isRefleshClick) {
                List<Location> locationsList = locationResult.getLocations();
                //공개 정적 파이널 클래스인 LocationResult 는 (List<Location> locations)의 형태로 위치 정보를 저장 할, 동적배열을 생성한다.
                //주어진 위치에 대한 위치결과를 생성함.

                if (locationsList.size() > 0) { //지역정보를 받아, 동적배열에 저장했으면,
                    location = locationsList.get(locationsList.size() - 1);//배열에 가장 나중에 저장된 정보.
                    //즉, 최신정보를 location에 저장.

                    currentPosistion = new LatLng(location.getLatitude(), location.getLongitude());

                    String markerSnippet = getCurrentAddress(currentMarker.getPosition());

                    String markerTitle = getCurrentAddress(currentPosistion);
/*                String markerSnippet = "위도"+(location.getLatitude())+
                        " 경도"+(location.getLongitude());*/
                    Log.d(TAG, "onLocationResult : " + markerSnippet);

                    isLocationCallBack = true;
                    distDecision(mMap);

                    setCurrentLocation(location, markerTitle, markerSnippet);
                    mCurrentLocation = location;

                }
            }
            if (isRefleshClick)
                isRefleshClick = false;

        }

    };

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //위치 관리자. 시스템 위치 서비스에 대한 액세스를 제공.
        //별도의 언급이없는 한 모든 위치 API 메소드에는 Manifest.permission.ACCESS_COARSE_LOCATION또는
        //Manifest.permission.ACCESS_FINE_LOCATION권한이 필요. 애플리케이션에 대략적인 권한 만있는 경우 GPS 또는
        //수동 위치 제공자에 액세스 할 수 없다.

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)  //GPS제공자의 이름
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); //네트워크 제공자의 이름.
    }


    //디폴트 위치를 서울로 선언함. 처음 보여줄 화면을 정의하는 매서드.
    public void setDefaultLocation() {
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보를 가져올 수 없습니다.";
        String markerSnippet = "위치 퍼미션과 GPS를 확인해 보세요.";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

     /*   CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);*/

    }

    //매개변수로 위 경도 값을, 받아 GPS주소값으로 변환하도록 정의하는 커스텀 매서드.
    public String getCurrentAddress(LatLng latlng) {//Locale = 지역정보를 담은 클래스.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());//Locale = 각 지역의 나라, 언어정보 등을 담은 클래스.
        List<Address> addresses;    //List<자료형> 생성변수 로서 동적배열 생성.
        //즉 주소를 담을 동적배열 생성.

        try {
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException ioException) {
            Toast.makeText(this, "위치를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return "지오코딩 불가. 인터넷 연결을 확인해 주세요.";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS좌표", Toast.LENGTH_SHORT).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            /*          Toast.makeText(this, "주소 미발견", Toast.LENGTH_SHORT).show();*/
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    //LocationCallback클래스에 포함되어 오버라이드 한, nLocationLocationResult()에서
    // 받아온 위치값을 기반으로 하여, 사용자의 위치를 설정하는 매서드.
    public void setCurrentLocation(Location location, String markerTitle, String markersnippet) {

        if (currentMarker != null)    //현재 마커가 비어있지 않으면, 새로운 마커 표시를 위해 기존 마커를 비운다.
            currentMarker.remove();

        BitmapDrawable bDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker1);
        Bitmap b = bDraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 70, 70, false);

        LatLng currnetLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currnetLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet("현재 위치");
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));


        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currnetLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {

                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Location_Map.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_ENABLE_REQUEST_CODE) {//사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {

                    Log.d(TAG, "onActivityResult : GPS 활성화 되있음");

                    needRequest = true;
                }
            }
        }
    }

    public void showPlaceTypeInformation(LatLng latlng, String locationType) {

      /*  if (previous_marker != null)            //Clear하지 않을 경우 hashset<>에 배열이 계속 저장되어 크기가 커지는 문제가 생긴다.
            previous_marker.clear();//지역정보 마커 클리어
*/
        if (isButtonClick[btnIndex] == OFF) {
            if (btnIndex == 0)
                btnPlace1.clear();
            else if (btnIndex == 1)
                btnPlace2.clear();
            else if (btnIndex == 2)
                btnPlace3.clear();
            else if (btnIndex == 3)
                btnPlace4.clear();
        }

        new NRPlaces.Builder()
                .listener(Location_Map.this)
                .key("AIzaSyAGahDW2n2Hgis5pPpx_y3wQHwHlM6-J-A")
                .latlng(latlng.latitude, latlng.longitude)//현재 위치
                .radius(distVal * 500) //지정한 거리 내에서
                .type(locationType) //음식점
                .build()
                .execute();
    }

    @Override
    public void onPlacesFailure(PlacesException e) {
        Snackbar.make(findViewById(R.id.snackView), selName[btnIndex] + " : 반경 " + distCal(distVal) + " 내 결과가 없습니다 !", BaseTransientBottomBar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    @Override
    public void onPlacesStart() {
    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isButtonClick[btnIndex] == ON)
                    fButtons[btnIndex].setTextColor(btnColor[btnIndex]);
                else
                    fButtons[btnIndex].setTextColor(Color.WHITE);

                if (isButtonClick[btnIndex] == ON) {
                    for (final noman.googleplaces.Place place : places) {

                        LatLng latLng
                                = new LatLng(place.getLatitude()
                                , place.getLongitude());

                        String markerSnippet = getCurrentAddress(latLng);

                        locCount++;

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(place.getName());
                        markerOptions.snippet(markerSnippet);        //mMap.addMarker를 통하여 결국 마커가 생성된다.
                        iconDivider(markerOptions, btnIndex);
                        item = mMap.addMarker(markerOptions);

                        if (btnIndex == 0)
                            btnPlace1.add(item);
                        else if (btnIndex == 1)
                            btnPlace2.add(item);
                        else if (btnIndex == 2)
                            btnPlace3.add(item);
                        else if (btnIndex == 3)
                            btnPlace4.add(item);

                    }
                    //previous_marker.add(item);


                } else {
                    if (isButtonClick[btnIndex] == OFF) {
                        markerDelete();
                    }
                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<>();         //hashset<> 안에 나타난 마커가 전부 저장되어 있다.
                hashSet.addAll(previous_marker);                    //위의 반복문을 통하여, previous_marker안에 먼저 마커 정보를 저장 후,
                //hashSet안에 옮겨 놓음.

                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }

        });
    }

    @Override
    public void onPlacesFinished() {

        if (isButtonClick[btnIndex] == ON) {

            Snackbar.make(findViewById(R.id.snackView), "'" + selName[btnIndex] + "' 검색 결과 : " + locCount + "곳 찾았습니다 ! ", 2000).show();
        } else if (isButtonClick[btnIndex] == OFF)
            Snackbar.make(findViewById(R.id.snackView), "'" + selName[btnIndex] + "' 마커를 전부 삭제했습니다 !", 2000).show();

        locCount = 0;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!isMarkerClick) {
            prvLat = marker.getPosition().latitude;
            prvLng = marker.getPosition().longitude;
        }

        Log.d("asdf", marker.getPosition().latitude + ", " + marker.getPosition().longitude);

        if (isMarkerClick) {           //같은 마커를 연속 두번 터치했을 때, 시작될 이벤트.

            if (prvLat == marker.getPosition().latitude && prvLng == marker.getPosition().longitude) {

            }
        }
        isMarkerClick = !isMarkerClick;

        return false;
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_Map:
                anim();

                break;
            case R.id.fab_Map1:

                isRefleshClick = true;
                Snackbar.make(v, "현재 위치를 새로 고칩니다. 잠시만 기다려 주세요.", 4000).show();
                anim();

                break;
            case R.id.fab_Map2:
                anim();

                Toast.makeText(this, "설정으로 가기 전, 현재 지도에 있는 정보를 모두 지웁니다.", Toast.LENGTH_SHORT).show();

                allReflesh();

                Intent intent = new Intent(Location_Map.this, Location_Settings.class);
                startActivity(intent);

                break;
        }
    }

    public void anim() {

        if (isFabOpen) {
            fabMap1.startAnimation(fab_close);
            fabMap2.startAnimation(fab_close);
            fabMap1.setClickable(false);
            fabMap2.setClickable(false);
            isFabOpen = false;
        } else {
            fabMap1.startAnimation(fab_open);
            fabMap2.startAnimation(fab_open);
            fabMap1.setClickable(true);
            fabMap2.setClickable(true);
            isFabOpen = true;
        }
    }


    void allReflesh() {

        isRefleshClick = true;
        if (mMap != null) {
            mMap.clear();
        }
        for (int i = 0; i < isButtonClick.length; i++) {

            if (isButtonClick[i] == ON)

                isButtonClick[i] = DISABLE;
                fButtons[i].setTextColor(Color.WHITE);
        }
    }

    String distCal(double distVal) {
        String reDist;

        if (distVal * 500 >= 1000)
            reDist = distVal / 2.0 + "km";
        else
            reDist = (int) (distVal * 500) + "m";

        return reDist;
    }


    void btnCheck(int index) {

        if (isButtonClick[index] == 0)
            isButtonClick[index] = 1;
        else if (isButtonClick[index] == 1)
            isButtonClick[index] = 2;
        else if (isButtonClick[index] == 2)
            isButtonClick[index] = 1;

    }

    void markerDelete() {

        if (btnIndex == 0) {
            for (int i = 0; i < btnPlace1.size(); i++) {

                Marker m = btnPlace1.get(i);
                m.remove();
            }
        }
        if (btnIndex == 1) {
            for (int i = 0; i < btnPlace2.size(); i++) {

                Marker m = btnPlace2.get(i);
                m.remove();
            }
        }
        if (btnIndex == 2) {
            for (int i = 0; i < btnPlace3.size(); i++) {

                Marker m = btnPlace3.get(i);
                m.remove();
            }
        }
        if (btnIndex == 3) {
            for (int i = 0; i < btnPlace4.size(); i++) {

                Marker m = btnPlace4.get(i);
                m.remove();
            }
        }
    }

    void iconDivider(MarkerOptions markerOptions, int btnIndex) {
        for (int i = 0; i < fButtons.length; i++) {
            if (btnIndex == i) {
                BitmapDrawable bDraw = (BitmapDrawable) getResources().getDrawable(markerID[i]);
                Bitmap b = bDraw.getBitmap();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(b, 70, 70, false)));
            }
        }
    }

    void breakClickOverlap() {

        for (int i = 0; i < fButtons.length; i++) {
            final int j = i;
            fButtons[j].setClickable(false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fButtons[j].setClickable(true);
                }
            }, 2000);

        }
    }

    void buttonDecision(SharedPreferences settings) {

        for (int i = 0; i < selName.length; i++) {

            selName[i] = settings.getString("selName" + (i + 1), "");
            selType[i] = settings.getString("selType" + (i + 1), "");

            fButtons[i] = findViewById(fBtnId[i]);
            fButtons[i].setText(selName[i]);

            if (selName[i].equals("") || selType.equals("") || selName[i].equals("사용안함")) {
                fButtons[i].setVisibility(View.INVISIBLE);

            } else
                fButtons[i].setVisibility(View.VISIBLE);
        }
    }

    void distDecision(GoogleMap mMap) {
        if (searchCircle) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(currentPosistion.latitude, currentPosistion.longitude))
                    .radius(distVal * 500).strokeColor(0xAAFFCD12))
                    .setStrokeWidth(3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null)
            mMap.clear();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivBackMap.setVisibility(View.INVISIBLE);
            }
        }, 500);

        isRefleshClick = true;
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE); //Map 액티비티 시작 시, 앱 관련 설정값을 읽어온다.

        alarmVal = settings.getInt("alarmVal", 1);
        distVal = settings.getInt("distVal", 1);
        searchCircle = settings.getBoolean("sCircle", true);

        for (int i = 0; i < isButtonClick.length; i++) {
            if (isButtonClick[i] == ON)

                isButtonClick[i] = 0;
        }

        if (isLocationCallBack) {
            mMap.clear();

            distDecision(mMap);
        }
        buttonDecision(settings);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ivBackMap.setVisibility(View.VISIBLE);
    }



}

