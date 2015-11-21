package com.example.VoiceRecog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.example.VoiceRecog.uilts.JsonParser;
import com.example.VoiceRecog.uilts.operatorFile;
import com.example.VoiceRecog.functions.setBrowser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.io.File;
import java.io.IOException;

public class main extends Activity implements View.OnClickListener{
    /**
     * Called when the activity is first created.
     */
    WebView mWebView;
    MyWebViewClient client;
    EditText search;
    operatorFile opfile;
    public static String url = "http://www.baidu.com/"; //从设置中得到
    private View searchV;
    private RecognizerDialog Recog_Dialog;   //对话框语音识别成文字
    private Toast mToast;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        opfile =new operatorFile(this);
        try {
            loadEngine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initViewControl();
        mWebView.loadUrl(url);
    }

    private void loadEngine() throws IOException {
        File file = this.getFileStreamPath("browser.txt");
        if(file.exists()){
            String enginer =  opfile.ReadFromInner("browser.txt");
            if(enginer.equals("必应")){
                url ="http://cn.bing.com/search?q=";
            }
            else{
                url = "http://www.baidu.com/s?wd=";
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setDefault:
               // showTips("点击了扫面音乐");
                Intent set = new Intent(main.this,setBrowser.class);
                startActivity(set);
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.pauseTimers();
        if(isFinishing()){
            mWebView.loadUrl("about:blank");
            setContentView(new FrameLayout(this));
        }
    }
    @Override
    protected void onResume() {
        super.onRestart();
        mWebView.resumeTimers();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initViewControl() {
        //在线语音识别
        Recog_Dialog = new RecognizerDialog(this, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    showTips("对话框初始化失败:" + code);
                }
            }
        });
        search = (EditText)this.findViewById(R.id.search);
        search.addTextChangedListener(texChangeListen);

        //显示网页
        mWebView = (WebView) this.findViewById(R.id.webView);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.requestFocus();

        //空间加载
        searchV = this.findViewById(R.id.voice);
        searchV.setOnClickListener(this);
        this.findViewById(R.id.back).setOnClickListener(this);
        this.findViewById(R.id.forward).setOnClickListener(this);
        this.findViewById(R.id.search2).setOnClickListener(this);
        this.findViewById(R.id.refresh).setOnClickListener(this);
        //this.findViewById(R.id.setDefault).setOnClickListener(this);
        client = new MyWebViewClient();
        mWebView.setWebViewClient(client);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
    }
    TextWatcher texChangeListen = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            String url_text=search.getText().toString();
            String temp = url;
            temp = url+url_text;
            mWebView.loadUrl(temp);
        }
    };
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.forward:
                mWebView.goForward();
                break;
            case R.id.back:
                mWebView.goBack();
                break;
            case R.id.voice:
            case R.id.search2:
                //showTips("打开对话框");
                Recog_Dialog.setListener(recognizerDialogListener);
                Recog_Dialog.show();
                break;
            case R.id.refresh:
                mWebView.reload();
                break;
            case R.id.exit:
                this.finish();
                break;
        }
    }

    //对话框监听事件
    private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult result, boolean isLast) {
            String text = JsonParser.parseIatResult(result.getResultString());
            search.append(text);
            search.setSelection(search.length());
            if(isLast){
                search.setText(text);
            }
        }
        @Override
        public void onError(SpeechError error) {
            showTips(error.getErrorDescription());
            System.out.println(error.getErrorDescription());
        }
    };

    public void showTips(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });
    }
    class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
