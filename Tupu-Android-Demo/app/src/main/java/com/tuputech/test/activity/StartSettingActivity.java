package com.tuputech.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuputech.sdk.manager.LicenseManager;
import com.tuputech.sdk.util.SDKUtils;
import com.tuputech.test.R;
import com.tuputech.test.camera.ICamera;
import com.tuputech.test.util.Util;
import com.tuputech.test.view.DialogView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

import static com.tuputech.test.R.id.model_button;


public class StartSettingActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnSeekBarChangeListener {


    private TextView modelText;
    private HashMap<String, Integer> preViewSize;
    private Spinner cameraSizeSpinner;
    private ArrayList<HashMap<String, Integer>> cameraSize;
    private TextView camera_size_text;
    private TextView networkCheck;
    private boolean isBackCamera = false;

    private SeekBar smoothRatio, facenessSeekbar;
    private LicenseManager licenseManager;


    private float smoothRatioValue = 0.3f;

    private TextView smooth_ratio_tv, facenessTv;
    private String modelName = "fanet8ss_utter_4_64_exp.model";
    private float faceness = 0.6f;
    private Button modelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        init();
        initData();
        getCameraSizeList();
        checkNetWork();
    }

    private void init() {
        licenseManager = new LicenseManager(this);
        modelText = (TextView) findViewById(R.id.model_text);
        camera_size_text = (TextView) findViewById(R.id.camera_size_text);
        smooth_ratio_tv = (TextView) findViewById(R.id.smooth_ratio_tv);
        facenessTv = (TextView) findViewById(R.id.faceness_tv);
        preViewSize = new HashMap<String, Integer>();
        modelText.setText(modelName);

        cameraSizeSpinner = (Spinner) findViewById(R.id.camera_size_spinner);
        smoothRatio = (SeekBar) findViewById(R.id.smooth_ratio);
        facenessSeekbar = (SeekBar) findViewById(R.id.faceness_sb);
        networkCheck = (TextView) findViewById(R.id.network_check);

        smoothRatio.setMax(100);
        smoothRatio.setProgress(60);
        smooth_ratio_tv.setText("Smooth: " + smoothRatioValue);

        facenessSeekbar.setMax(100);
        facenessSeekbar.setProgress(60);
        facenessTv.setText("Faceness: " + faceness);

        smoothRatio.setOnSeekBarChangeListener(this);
        facenessSeekbar.setOnSeekBarChangeListener(this);


        cameraSizeSpinner.setOnItemSelectedListener(this);
        modelButton = (Button) findViewById(model_button);

        modelButton.setOnClickListener(this);

    }

    private void getCameraSizeList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int cameraId = 1;
                if (isBackCamera) {
                    cameraId = 0;
                }

                cameraSize = ICamera.getCameraPreviewSize(cameraId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cameraSize == null) {
                            return;
                        }
                        ArrayList<String> allItems = new ArrayList<String>();
                        for (int i = 0; i < cameraSize.size(); i++) {
                            allItems.add(cameraSize.get(i).get("width") + "*" + cameraSize.get(i).get("height"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StartSettingActivity.this,
                                android.R.layout.simple_spinner_item, allItems);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        cameraSizeSpinner.setAdapter(adapter);
                        cameraSizeSpinner.setSelection(allItems.size() / 2);

                    }
                });
            }
        }).start();
    }

    private void initData() {
        if (Util.TP_APP_KEY == null || Util.TP_APP_SECRET == null) {
            DialogView mDialogView = new DialogView(this);
            mDialogView.showDialog("请填写TP_APP_KEY和TP_APP_SECRET");
        }
    }

    private void checkNetWork() {

        networkCheck.setText("正在联网授权中...");
        networkCheck.setVisibility(View.VISIBLE);



        boolean isAuthSuccess = licenseManager.auth();

        if (isAuthSuccess) {
            authState(true);
        } else {
            String authMsg = licenseManager.getAuthMessage(LicenseManager.DURATION_30DAYS);
            AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
            String signature = SDKUtils.getSignature(authMsg, Util.TP_APP_SECRET);

            RequestParams params = new RequestParams();
            params.put("APPKey", Util.TP_APP_KEY);
            params.put("signature", signature);
            params.put("authMsg", authMsg);


            mAsyncHttpclient.post(Util.AUTH_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
                    String successStr = new String(responseByte);
                    JSONObject resultObj = null;
                    int code = -1;
                    String resultMsg = null;

                    Log.w("Test", "successStr:" + successStr);
                    try {
                        resultObj = new JSONObject(successStr);
                        code = resultObj.getInt("code");
                        resultMsg = resultObj.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (resultMsg != null && code == 0) {
                        boolean isSuccess = licenseManager.setLicense(resultMsg);
                        if (isSuccess) {
                            authState(true);
                        } else {
                            authState(false);
                        }
                    } else {
                        authState(false);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    error.printStackTrace();
                    authState(false);
                }
            });
        }

    }

    private void authState(boolean isSuccess) {

        if (isSuccess) {

            networkCheck.setVisibility(View.GONE);

            modelButton.setVisibility(View.VISIBLE);
        } else {

            modelButton.setVisibility(View.GONE);

            networkCheck.setVisibility(View.VISIBLE);
            networkCheck.setText("联网授权失败！请检查网络或找服务商");
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == model_button) {
            Intent intent = new Intent();
            intent.putExtra("model", modelName);
            intent.putExtra("preViewSize", preViewSize);
            intent.putExtra("isBackCamera", isBackCamera);
            intent.putExtra("smoothRatioValue", smoothRatioValue);
            intent.putExtra("faceness", faceness);

            intent.setClass(StartSettingActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        preViewSize = cameraSize.get(position);
        Log.i("preViewSize", preViewSize.get("width") + "**" + preViewSize.get("height"));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.smooth_ratio) {
            smoothRatioValue = progress * 0.5f / 100f;
            smooth_ratio_tv.setText("Smooth: " + smoothRatioValue);
        } else if (seekBar.getId() == R.id.faceness_sb) {
            faceness = progress / 100f;
            facenessTv.setText("Faceness: " + faceness);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {


    }
}
