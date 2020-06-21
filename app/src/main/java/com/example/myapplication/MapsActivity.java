package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.widget.ToggleButton;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener,
        FriendAdapter.OnItemClickHandler,
        FriendConfirmAdapter.OnItemClickHandler {

    public static final String URL_GET_FRIEND = "http://thomasfeng.ddns.net/friend.php";
    public static final String URL_ADD_FRIEND = "http://thomasfeng.ddns.net/confirm.php";
    public static final String URL_CONFIRM_FRIEND = "http://thomasfeng.ddns.net/friendconfirm.php";
    public static final String URL_CONFIRM_FRIEND_UPLOAD = "http://thomasfeng.ddns.net/friendconfirm2.php";
    public static final String URL_LOCATION_UPLOAD = "http://thomasfeng.ddns.net/postID.php";

    private static final String TAG = "DEBUG";

    private GoogleMap mMap;
    private LocationRequest locationRequest;

    private static final int REQUEST_LOCATION = 2;

    //有Data是給RecyclerView使用，沒有Data是用來儲存資料

    //儲存好友列表
    private ArrayList<FriendItem> friendItemsData = new ArrayList<>();
    private ArrayList<FriendItem> friendItems = new ArrayList<>();
    //儲存好友確認列表
    private ArrayList<FriendItem> friendConfirmItemsData = new ArrayList<>();
    private ArrayList<FriendItem> FriendConfirmItem = new ArrayList<>();

    private ArrayList<FriendLocation> friendLocations = new ArrayList<>();

    private RecyclerView recyclerView;
    private TextView t6,t7, scroll;

    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetBehavior bottomSheetBehavior;
    private ConstraintLayout constraintLayout;

    private boolean autoCamera = false;

    private String androidID;
    private String deviceName;

    private updateFriendThread updateFriendThread = new updateFriendThread();
    private roadUploadThread roadUploadThread = new roadUploadThread();

    private FloatingActionButton current_location;
    private FloatingActionButton zoomIn;
    private FloatingActionButton zoomOut;
    private ToggleButton location_upload_toggle;
    private Button friend_list_button;
    private Button add_friend_button;
    private Button confirm_friend_button;
    private Button name_change_button;

    //活動建立
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        findViewId();
        setBottomSheetBehavior();
        setRecyclerDialogView();
        viewListener();

    }
    //建立定位請求
    private void createLocationRequest() {
        locationRequest = new LocationRequest(); //新增位置請求設置
        locationRequest.setInterval(1000); //設定回報速率
        locationRequest.setFastestInterval(1000); //設定最快回報速率
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //設定精確度
    }
    //獲取布局元件ID
    private void findViewId() {
        current_location = (FloatingActionButton) findViewById(R.id.current_location);
        zoomIn = (FloatingActionButton) findViewById(R.id.zoomin);
        zoomOut = (FloatingActionButton) findViewById(R.id.zoomout);

        t6 = (TextView) findViewById(R.id.t6);
        t7 = (TextView) findViewById(R.id.t7);
        scroll = (TextView) findViewById(R.id.scroll);

        location_upload_toggle = (ToggleButton) findViewById(R.id.location_upload_toggle);
        friend_list_button = (Button) findViewById(R.id.friend_list_button);
        add_friend_button = (Button) findViewById(R.id.add_friend_button);
        confirm_friend_button = (Button) findViewById(R.id.confirm_friend_button);
        name_change_button = (Button) findViewById(R.id.name_change_button);



        constraintLayout = findViewById(R.id.constrain);
    }
    //介面傾聽器
    private void viewListener() {
        current_location.setOnClickListener(this);
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);

        location_upload_toggle.setOnCheckedChangeListener(this);
        friend_list_button.setOnClickListener(this);
        add_friend_button.setOnClickListener(this);
        confirm_friend_button.setOnClickListener(this);
        name_change_button.setOnClickListener(this);

        //上拉選單開關
        scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        //清單關閉傾聽器
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
//                if(listUploadThread.isStart) listUploadThread.Stop();
            }
        });
    }
    //設定上拉式選單
    private void setBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if(i == BottomSheetBehavior.STATE_EXPANDED){
                    location_upload_toggle.setClickable(true);
                    friend_list_button.setClickable(true);
                    add_friend_button.setClickable(true);
                    confirm_friend_button.setClickable(true);
                    name_change_button.setClickable(true);


                    mMap.setPadding(0,0,0,600);
                }
                else if(i == BottomSheetBehavior.STATE_COLLAPSED){
                    location_upload_toggle.setClickable(false);
                    friend_list_button.setClickable(false);
                    add_friend_button.setClickable(false);
                    confirm_friend_button.setClickable(false);
                    name_change_button.setClickable(false);

                    mMap.setPadding(0,0,0,0);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }
    //設定顯示清單列表
    private void setRecyclerDialogView() {
        View recyclerDialogView = getLayoutInflater().inflate(R.layout.recycleview, null);
        bottomSheetDialog= new BottomSheetDialog(MapsActivity.this);
        bottomSheetDialog.setContentView(recyclerDialogView);
        recyclerView = recyclerDialogView.findViewById(R.id.recycler);
    }

    //程式重新打開
    @Override
    protected void onResume() {
        super.onResume();

    }
    //程式退出
    @Override
    protected void onPause() {
        super.onPause();

        if(roadUploadThread.isStart) roadUploadThread.Stop();
    }
    //地圖載入完成，取得google map後自動執行
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //呼叫google map物件
        mMap = googleMap;
        //獲取定位權限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            createLocationRequest();
            fuseLocationRequest();
        }
        //獲取目前位置
        fuseLocation(0);
        //設置地圖風格
        setSelectedStyle();
        //地圖屬性設定
        MapSetting();
        //文字顯示初始化
        testViewInit();
    }
    //文字顯示初始化
    private void testViewInit() {
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceName = getSharedPreferences("device",MODE_PRIVATE).getString("name",androidID);

        t6.setText("設備ID：" + androidID);
        t7.setText("設備暱稱：" + deviceName);
    }
    //地圖設定函數
    private void MapSetting() {
        //顯示羅盤
        mMap.getUiSettings().setCompassEnabled(true);
        //開啟預設定位
        mMap.setMyLocationEnabled(false);
        //顯示預設定位按鈕
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //顯示小工具欄位
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //顯示3D建築物
        mMap.setBuildingsEnabled(false);

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    autoCamera = false;
                }
            }
        });

    }

    private Location currentLocation;

    //自動獲取最新位置函數
    private void fuseLocationRequest() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                    if (autoCamera) animateCamera(location);
                    animateMarker(location);

                }
            }
        };
        client.requestLocationUpdates(locationRequest,locationCallback,null);
    }


    //手動獲取最新位置函數
    private void fuseLocation(final int mode) {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnCompleteListener(
                this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {
                                currentLocation = location;

                                if(mode == 0)moveCamera(location);
                                else if(mode == 1) animateCamera(location);
                            }
                        }
                    }
                });
    }

    private Marker currentMarker = null;
    public void animateMarker(Location location){

        if (currentMarker == null){
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(),location.getLongitude()))
                    .icon(bitmapDescriptorFromVector(R.drawable.self_marker_240dp, 0.25f, 0.25f))
                    .anchor(0.5f, 1f)
                    .flat(true)
                    .zIndex(10)
            );
        }
        else{
            currentMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        }
    }

    //靜態移動畫面
    private void moveCamera(Location location) {
        if (location != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),
                            location.getLongitude()),
                    15));
        }
    }
    //動態移動畫面
    private void animateCamera(Location location) {
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(location.getLatitude(),
                            location.getLongitude())),
                    625, null);
        }
    }

    //地圖風格設定函數
    private void setSelectedStyle() {
        MapStyleOptions style;
        style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_gray);
        mMap.setMapStyle(style);
    }

    //權限設定函數
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest();
                    fuseLocationRequest();
                } else {

                }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.current_location:
                fuseLocation(1);
                autoCamera = true;
                break;
            case R.id.zoomin:
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.zoomout:
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.friend_list_button:
                bottomSheetDialog.show();
                showFriendList();
                break;
            case R.id.add_friend_button:
                final Dialog addFriendDialog = new Dialog(MapsActivity.this);
                addFriendDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                addFriendDialog.setContentView(R.layout.dialogview);
                addFriendDialog.show();
                TextView t1 = addFriendDialog.findViewById(R.id.t1);
                TextView t2 = addFriendDialog.findViewById(R.id.t2);

                Button addFriendDialogButtonCancel = addFriendDialog.findViewById(R.id.db1);
                Button addFriendDialogButtonConfirm = addFriendDialog.findViewById(R.id.db2);
                final EditText addFriendDialogEditText = addFriendDialog.findViewById(R.id.e1);
                t1.setText("新增好友");
                t2.setText("好友ID：");
                addFriendDialogEditText.setHint("請輸入好友ID進行搜尋");
                addFriendDialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addFriendDialog.cancel();
                    }
                });
                addFriendDialogButtonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ID2 = addFriendDialogEditText.getText().toString();
                        addFriend(androidID, ID2);
                        addFriendDialog.cancel();
                    }
                });
                break;
            case R.id.confirm_friend_button:
                bottomSheetDialog.show();
                showFriendConfirmList();
                break;
            case R.id.name_change_button:
                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialogview);
                dialog.show();
                Button db1 = dialog.findViewById(R.id.db1);
                Button db2 = dialog.findViewById(R.id.db2);
                final EditText e1 = dialog.findViewById(R.id.e1);
                e1.setText(deviceName);
                db1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                db2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deviceName = e1.getText().toString();
                        SharedPreferences sharedPreferences = getSharedPreferences("device",MODE_PRIVATE);
                        sharedPreferences.edit().putString("name",deviceName).apply();
                        t7.setText("設備暱稱：" + deviceName);
                        dialog.cancel();
                    }
                });
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){

            case R.id.location_upload_toggle:
                if(buttonView.isChecked()){
                    roadUploadThread.Start();
                }
                else{
                    roadUploadThread.Stop();
                }
                break;
        }
    }

    //----新增好友

    private void addFriend(String ID1, String ID2) {
        OkHttpClient client = new OkHttpClient();
        //OkHttp - POST連線
        // FormBody放要傳的參數和值
        FormBody formBody = new FormBody.Builder()
                .add("ID1", ID1)
                .add("ID2", ID2)
                .build();

        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(URL_ADD_FRIEND)
                .post(formBody)
                .build();
        // 建立Call
        Call call = client.newCall(request);
        // 執行Call連線到網址
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 連線失敗
                Log.d("OKHTTP：", "ROAD上傳失敗");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 連線成功
                Log.d("OKHTTP：", "ROAD上傳成功");
                String json = response.body().string();
                if(json.contains("成功"))
                    Log.d("ADD", "onResponse: 新增成功");
                if(json.contains("失敗"))
                    Log.d("ADD", "onResponse: 新增失敗");
            }
        });
    }

    //----開啟好友確認列表

    private void showFriendConfirmList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFriendConfirmItem();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setFriendConfirmRecyclerView();
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFriendConfirmItem();
                    }
                });
            }
        }).start();
    }

    private FriendConfirmAdapter friendConfirmAdapter;
    private void showFriendConfirmItem() {

        for (int i = 0; i < FriendConfirmItem.size(); i++) {
            if (FriendConfirmItem.get(i).getName() != null) {
                //將儲存的資料轉移到RecyclerView儲存庫內
                friendConfirmItemsData.add(new FriendItem(
                        FriendConfirmItem.get(i).getID(),
                        FriendConfirmItem.get(i).getName(),
                        false
                ));

            }
        }
//        重新呼叫適配器設置畫面
        recyclerView.setAdapter(friendConfirmAdapter);
    }

    private void setFriendConfirmRecyclerView() {
        friendConfirmAdapter = new FriendConfirmAdapter(friendConfirmItemsData,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(MapsActivity.this));

        recyclerView.addItemDecoration(new DividerItemDecoration(MapsActivity.this, DividerItemDecoration.VERTICAL));
//        清空RecyclerView儲存庫內的資料
        friendConfirmItemsData.clear();
//        設置適配器設置畫面
        recyclerView.setAdapter(friendConfirmAdapter);
    }

    private void getFriendConfirmItem() {
        OkHttpClient client = new OkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("ID1", androidID)
                .build();
        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(URL_CONFIRM_FRIEND)
                .post(formBody)
                .build();
        // 建立Call
        Call call = client.newCall(request);
        // 執行Call連線到網址
        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 連線失敗
                Log.d("OKHTTP：", "ROAD下載失敗");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 連線成功，自response取得連線結果
                Log.d("OKHTTP：", "getFriendConfirmItem下載成功");
                String json = response.body().string();
                if(json.contains("<br />"))
                    FriendConfirmItem.clear();
                try {
                    JSONArray array = new JSONArray(json);
                    FriendConfirmItem.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        FriendConfirmItem.add(new FriendItem(
                                obj.getString("ID1"),
                                obj.getString("暱稱"),
                                false
                        ));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void ConfirmFriendUpload(final String ID1, final String ID2) {
        OkHttpClient client = new OkHttpClient();
        //OkHttp - POST連線
        // FormBody放要傳的參數和值
        FormBody formBody = new FormBody.Builder()
                .add("ID1", ID1)
                .add("ID2", ID2)
                .build();
        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(URL_CONFIRM_FRIEND_UPLOAD)
                .post(formBody)
                .build();
        // 建立Call
        Call call = client.newCall(request);
        // 執行Call連線到網址
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 連線失敗
                Log.d("OKHTTP：", "ConfirmFriendUpload上傳失敗");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 連線成功
                Log.d("OKHTTP：", "ConfirmFriendUpload上傳成功 " + ID1+" / "+ ID2);
                String json = response.body().string();
                if(json.contains("成功"))
                    Log.d("OKHTTP", "好友確認: 確認成功");
                if(json.contains("失敗"))
                    Log.d("OKHTTP", "好友確認: 確認失敗");
            }
        });
    }

    //----開啟好友列表

    private void showFriendList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFriendItem();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setFriendRecyclerView();
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFriendItem();
                    }
                });
            }
        }).start();
    }

    private FriendAdapter friendAdapter;
    private void showFriendItem() {

        for (int i = 0; i < friendItems.size(); i++) {
            if (friendItems.get(i).getName() != null) {

                friendItemsData.add(new FriendItem(
                        friendItems.get(i).getID(),
                        friendItems.get(i).getName(),
                        false
                ));
            }
            for (int j = 0; j < friendDownloadThreads.size(); j++){
                if (friendDownloadThreads.get(j).getIdentify().equals(friendItemsData.get(i).getID())){
                    friendItemsData.get(i).setChecked(true);
                }
            }
        }

        recyclerView.setAdapter(friendAdapter);
    }

    private void setFriendRecyclerView() {
        friendAdapter = new FriendAdapter(friendItemsData,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(MapsActivity.this));

        recyclerView.addItemDecoration(new DividerItemDecoration(MapsActivity.this, DividerItemDecoration.VERTICAL));

        friendItemsData.clear();

        recyclerView.setAdapter(friendAdapter);
    }

    private void getFriendItem() {
        //OkHttp - GET連線
        OkHttpClient client = new OkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("ID1", androidID)
                .build();
        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(URL_GET_FRIEND)
                .post(formBody)
                .build();
        // 建立Call
        Call call = client.newCall(request);
        // 執行Call連線到網址
        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 連線失敗
                Log.d("OKHTTP：", "getFriendItem下載失敗");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 連線成功，自response取得連線結果
                Log.d("OKHTTP：", "getFriendItem下載成功");
                String json = response.body().string();
                try {
                    JSONArray array = new JSONArray(json);
                    friendItems.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        friendItems.add(new FriendItem(
                                obj.getString("ID"),
                                obj.getString("暱稱"),
                                false
                        ));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //----獲取好友定位

    private void getFriendLocation() {
        //OkHttp - GET連線
        OkHttpClient client = new OkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("ID1", androidID)
                .build();
        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(URL_GET_FRIEND)
                .post(formBody)
                .build();
        // 建立Call
        Call call = client.newCall(request);
        // 執行Call連線到網址
        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 連線失敗
                Log.d("OKHTTP：", "ROAD下載失敗");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 連線成功，自response取得連線結果
                Log.d("OKHTTP：", "getFriendLocation下載成功");
                String json = response.body().string();
                try {
                    JSONArray array = new JSONArray(json);
                    friendLocations.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        friendLocations.add(new FriendLocation(
                                obj.getString("ID"),
                                obj.getString("經度"),
                                obj.getString("緯度"),
                                obj.getString("暱稱")
                        ));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //----

    private ArrayList<friendDownloadThread> friendDownloadThreads = new ArrayList<>();

    @Override
    public void onItemSelect(int position, String identify) {

        int index = friendDownloadThreads.indexOf(new friendDownloadThread(identify));
        if (index == -1){
            friendDownloadThreads.add(new friendDownloadThread(identify));
            index = friendDownloadThreads.indexOf(new friendDownloadThread(identify));
            if (!friendDownloadThreads.get(index).isStart) friendDownloadThreads.get(index).Start();
        }

        if (!updateFriendThread.isStart) updateFriendThread.Start();
    }

    @Override
    public void onItemCancel(int position, String identify) {

        int index = friendDownloadThreads.indexOf(new friendDownloadThread(identify));
        if (friendDownloadThreads.get(index).isStart) friendDownloadThreads.get(index).Stop();
        friendDownloadThreads.remove(index);

        if(updateFriendThread.isStart && friendDownloadThreads.size() == 0) updateFriendThread.Stop();
    }

    @Override
    public void onItemClick(int position, FriendItem friendItemsData) {
        ConfirmFriendUpload(androidID, friendItemsData.getID());
        friendConfirmAdapter.removeItem(position);
    }

    private LatLng convertLatLng(String latitude, String longitude){
        LatLng latLng;
        if (latitude != null && longitude != null){
            latLng = new LatLng(
                    Double.parseDouble(latitude),
                    Double.parseDouble(longitude)
            );
            return latLng;
        }
        else{
            return null;
        }
    }

    private class friendDownloadThread{

        private String identify;
        private String name;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            friendDownloadThread that = (friendDownloadThread) o;
            return identify.equals(that.identify);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identify);
        }

        public String getIdentify() {
            return identify;
        }

        public void setIdentify(String identify) {
            this.identify = identify;
        }

        public friendDownloadThread(String identify) {
            this.identify = identify;
        }

        private LatLng lastLatLng, currentLatLng;
        private Marker marker;
        private ArrayList<Polyline> polylines = new ArrayList<>();

        private boolean isStart = false;
        private Thread thread;
        private Runnable uiRunnable = new Runnable() {
            @Override
            public void run() {
                if (marker == null){
                    marker = mMap.addMarker(new MarkerOptions()
                            .flat(false)
                            .icon(bitmapDescriptorFromVector(R.drawable.other_marker_240dp, 0.25f, 0.25f))
                            .anchor(0.5f,0.9f)
                            .zIndex(10)
                            .title(name)
                            .position(currentLatLng)
                    );
                }
                else {
                    marker.setPosition(currentLatLng);
                }
                if (lastLatLng != null){
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .add(lastLatLng,currentLatLng)
                            .width(5)
                            .jointType(JointType.ROUND)
                            .startCap(new RoundCap())
                            .endCap(new RoundCap())
                            .color(getResources().getColor(R.color.colorRoad));
                    polylines.add(mMap.addPolyline(polylineOptions));

                }

                lastLatLng = currentLatLng;
            }
        };
        private Runnable mainRunnable = new Runnable() {
            @Override
            public void run() {

                while (true){

                    for (int i = 0; i < friendLocations.size(); i++) {
                        if (friendLocations.get(i).getID().equals(identify)){
                            currentLatLng = convertLatLng(
                                    friendLocations.get(i).getLatitude(),
                                    friendLocations.get(i).getLongitude()
                            );
                            name = friendLocations.get(i).getName();
                        }
                    }

                    if ( currentLatLng != null){
                        runOnUiThread(uiRunnable);
                    }

                    if(thread.isInterrupted())
                        break;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        void Start(){
            isStart = true;
            thread = new Thread(mainRunnable);
            thread.start();
        }

        void Stop(){
            marker.remove();
            for (int i = 0; i < polylines.size(); i++) {
                polylines.get(i).remove();
            }
            isStart = false;
            thread.interrupt();
        }
    }

    private class updateFriendThread{


        private boolean isStart = false;
        private Thread thread;
        private Runnable mainRunnable = new Runnable() {
            @Override
            public void run() {

                while (true){
                    getFriendLocation();
                    if(thread.isInterrupted())
                        break;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        void Start(){
            isStart = true;
            thread = new Thread(mainRunnable);
            thread.start();
        }

        void Stop(){
            isStart = false;
            thread.interrupt();
        }
    }

    private class roadUploadThread {
        private int roadUploadCount = 0;
        private boolean isStart = false;

        private Thread thread;

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            roadUploadCount++;
                            if(currentLocation != null){
                                postRoad(
                                        androidID,
                                        String.valueOf(currentLocation.getLongitude()),
                                        String.valueOf(currentLocation.getLatitude()),
                                        deviceName
                                );
                            }

                        }
                    });
                    if(thread.isInterrupted())
                        break;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        void Start(){
            isStart = true;
            thread = new Thread(runnable);
            thread.start();
        }

        void Stop(){
            isStart = false;
            thread.interrupt();
        }

        private void postRoad(String ID, String Longitude, String Latitude, String name) {
            OkHttpClient client = new OkHttpClient();
            //OkHttp - POST連線
            // FormBody放要傳的參數和值
            FormBody formBody = new FormBody.Builder()
                    .add("ID", ID)
                    .add("Longitude", Longitude)
                    .add("Latitude", Latitude)
                    .add("name",name)
                    .build();
            // 建立Request，設置連線資訊
            Request request = new Request.Builder()
                    .url(URL_LOCATION_UPLOAD)
                    .post(formBody)
                    .build();
            // 建立Call
            Call call = client.newCall(request);
            // 執行Call連線到網址
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    // 連線失敗
                    Log.d("OKHTTP：", "ROAD上傳失敗");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    // 連線成功
                    Log.d("OKHTTP：", "ROAD上傳成功");
                }
            });
        }
    }

    //圖片轉換器
    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId, float scaleX, float scaleY) {
        //新增一個Drawable，是一個有一些快速繪圖當法的對象
        Drawable vectorDrawable = ContextCompat.getDrawable(MapsActivity.this, vectorResId);
        //設置這個Drawalbe的邊界
        vectorDrawable.setBounds(
                0,
                0,
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        //新增一個Bitmap並且設定圖片的大小和顏色標準
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        //新增一個Canvas畫布工具，並且以bitmap來乘載畫布的內容。
        // 因為Canvas本身不能本存圖像，他只能實現一些畫畫的方法，類似小畫家
        Canvas canvas = new Canvas(bitmap);
        //將剛剛載入的圖片使用Drawable的draw方法畫到Canvas畫布上
        vectorDrawable.draw(canvas);
        //此時bitmap上乘載的canvas也就有了drawable畫上來的圖片

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);
        Bitmap bitmapScale = Bitmap.createBitmap(
                bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

        return BitmapDescriptorFactory.fromBitmap(bitmapScale);
    }
}
