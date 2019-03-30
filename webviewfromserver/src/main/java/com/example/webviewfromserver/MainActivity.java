package com.example.webviewfromserver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        loadWeb("http://192.168.2.254:13000/TestAndroid.jsp");

    }

    private void loadWeb(String url) {
        final Context app = this;
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidHandler(), "hybrid");
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(url);
    }

    // 앱과 웹간의 통신을 위한 클래스
    private class AndroidHandler {
        @JavascriptInterface
        public void setMessage(final String argv) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if ("exit".equals(argv)) {
                        showDialog();
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void toastShort(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    // 웹쪽에서 close버튼 클릭시 보여질 dialog메소드
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("종료하시겠습니까?");
        builder.setCancelable(false);
        builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // 만들어진 다이얼로그 객체 보기
        builder.show();
    }
}
