package com.iplant.view.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.iplant.Constant;
import com.iplant.MyError;
import com.iplant.R;
import com.iplant.libzxing.activity.CaptureActivity;
import com.iplant.model.QRResult;
import com.iplant.util.ImageFactory;
import com.iplant.view.widget.XDatePickDialog;

/**
 * @author lildu
 *	网页界面
 */
public class WebActivity extends BaseActivity {
	@Bind(R.id.web)
	WebView mWebView;
	
	String originUrl;
	
	JSImpl myJsImpl = new JSImpl();

	String mCameraFilePath = null;
	String mImgPath = null;
	ValueCallback<Uri[]> mFilePathCallback;
	public static final int REQUEST_SELECT_FILE 		= 10000;
	public static final int REQUEST_SELECT_CAPTURE 	= 10001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		originUrl = getIntent().getStringExtra(Constant.KEY_URL);
		
//		originUrl = "file:///android_asset/test.html";

		setContentView(R.layout.activity_web);
		
		initView();

		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);

		mImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.pathSeparator + "iplant" + File.pathSeparator;
		new File(mImgPath).mkdirs();

		myJsImpl.registerNotify();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		myJsImpl.unregisterNotify();
	}

	@SuppressLint("SetJavaScriptEnabled") 
	private void initView() {
		ButterKnife.bind(this);	
		
		//初始化webview
		WebSettings setting = mWebView.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setAppCacheEnabled(true);
		setting.setBuiltInZoomControls(false);
		setting.setUseWideViewPort(true);
		
		mWebView.addJavascriptInterface(myJsImpl, "JSImpl");
		
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
		
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				if (mFilePathCallback != null) {
					mFilePathCallback.onReceiveValue(null);
					mFilePathCallback = null;
				}

				mFilePathCallback = filePathCallback;
				showImagePick();
				return true;
			}
		});
		
		mWebView.loadUrl(originUrl);
	}

	public static Intent openImageIntent(Context context, Uri cameraOutputFile) {

		// Camera.
		final List<Intent> cameraIntents = new ArrayList<Intent>();
		final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		final PackageManager packageManager = context.getPackageManager();
		final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for(ResolveInfo res : listCam) {
			final String packageName = res.activityInfo.packageName;
			final Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(packageName);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputFile);
			cameraIntents.add(intent);
		}

		// Filesystem.
		final Intent galleryIntent = new Intent();
		galleryIntent.setType("image/*");
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

		// Chooser of filesystem options.
		final Intent chooserIntent = Intent.createChooser(galleryIntent, "选择照片来源");

		// Add the camera options.
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
		return chooserIntent;
	}

	private void showImagePick(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("选择图片来源");
		final String[] froms = {"拍照", "相册"};
		builder.setItems(froms, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
					case 0:
					{
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						mCameraFilePath = mImgPath + System.currentTimeMillis() + ".jpg";
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
						startActivityForResult(intent, REQUEST_SELECT_CAPTURE);
					}
					break;
					case 1:
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						startActivityForResult(intent, REQUEST_SELECT_FILE);
					}
					break;
				}
			}
		});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mFilePathCallback != null) {
					mFilePathCallback.onReceiveValue(null);
					mFilePathCallback = null;
				}
			}
		});

		builder.show();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			if (requestCode == REQUEST_SELECT_FILE || requestCode == REQUEST_SELECT_CAPTURE){
				if (mFilePathCallback != null){
					mFilePathCallback.onReceiveValue(null);
				}
				mFilePathCallback = null;
			}
			return;
		}
		if (requestCode == 1001 && data != null){
			Bundle bundle = data.getExtras();
			
			String result = bundle.getString("result");
			QRResult QRResult = new QRResult();
			if (TextUtils.isEmpty(result)){
				QRResult.errorcode = MyError.UNKNOWN;
			}else{
				QRResult.qrcode = result;
			}
			
			EventBus.getDefault().post(QRResult);
		}else if (requestCode == REQUEST_SELECT_FILE && data != null){
			if (mFilePathCallback == null)
				return;

			Uri uri = data.getData();
			String outFile = mImgPath + System.currentTimeMillis() + ".jpg";

			try {
				Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				new ImageFactory().compressAndGenImage(photo, outFile, 500);
				mFilePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(new File(outFile))});
			} catch (IOException e) {
				e.printStackTrace();
				mFilePathCallback.onReceiveValue(null);
			}

			mFilePathCallback = null;
		}else if(requestCode == REQUEST_SELECT_CAPTURE){
			if (mFilePathCallback == null)
				return;

			String outFile = mImgPath + System.currentTimeMillis() + ".jpg";
			boolean isExist = new File(outFile).exists();
			try {
				new ImageFactory().compressAndGenImage(mCameraFilePath, outFile, 500, true);
				mFilePathCallback.onReceiveValue(new Uri[]{Uri.fromFile(new File(outFile))});
			} catch (IOException e) {
				e.printStackTrace();
				mFilePathCallback.onReceiveValue(null);
			}
			mFilePathCallback = null;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	class JSImpl{
		String QRCallback;
		String DateCallback;
		public void registerNotify(){
			EventBus.getDefault().register(this);
		}
		
		public void unregisterNotify(){
			EventBus.getDefault().unregister(this);
		}
		
		@JavascriptInterface
		public void exit(){
			finish();
		}
		
		@JavascriptInterface
		public void readQRCode(String callback){
			QRCallback = callback;
			
			//打开二维码
			Intent i = new Intent(WebActivity.this, CaptureActivity.class);
			startActivityForResult(i, 1001);
		}
		
		@JavascriptInterface
		public void pickDate(String callback){
			DateCallback = callback;
			
			final Calendar cd = Calendar.getInstance();
			XDatePickDialog pickerdialog = new XDatePickDialog(WebActivity.this,
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, final int year, final int monthOfYear,
								final int dayOfMonth) {
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									String callBack="javascript:" + DateCallback + "(" 
											+ year + ","
											+ monthOfYear + ","
											+ dayOfMonth
											+ ")";
										
										mWebView.loadUrl(callBack);									
								}
							});
						}
					}, 
					cd.get(Calendar.YEAR), 
					cd.get(Calendar.MONTH),
					cd.get(Calendar.DAY_OF_MONTH));
			
			pickerdialog.show();
		}
		
		@Subscribe
		public void onEventMainThread(QRResult result){
			if (!result.isValid()){
				showMsg("获取二维码失败");
			}else{
				//调用js方法
				String callBack="javascript:" + QRCallback + "('" + result.qrcode + "')";
				mWebView.loadUrl(callBack);
			}
		}
	}
}
