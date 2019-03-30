package com.example.administrator.movieapiex;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class GetMyThread extends Thread {
    //메인 쓰레드의 핸들러
    Handler handler;
    //전송할 데이터가 담겨있는 map 객체
    Map<String, String> map;
    //전송할 url 주소
    String url;

    //생성자
    public GetMyThread(Handler handler, Map<String, String> map, String url) {
        this.handler = handler;
        this.map = map;
        this.url = url;
    }

    //스레드 본체
    @Override
    public void run() {
        HttpURLConnection conn = null;
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(this.url);
            Log.d("data>>>", url.toString());

            conn = (HttpURLConnection) url.openConnection();

            if (conn != null) {  //연결이 정상적으로 되었다면
                conn.setConnectTimeout(10000);  // 10 * 1000 = 10초만 대기하겠다.
                conn.setUseCaches(false);       // 캐시사용 안 하겠다.

                //인증정보가 필요한 경우
                //conn.setRequestProperty("Authrization", "키정보");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader isr
                            = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    while (true) {
                        String line = br.readLine();
                        if (line == null) {  //마지막 라인이라면
                            break;
                        }
                        //읽어온 문자열을 builder 객체 저장
                        builder.append(line);
                    } //while 끝
                    br.close();
                }
            }

            //데이터 수신 작업이 완료가 되면(쓰레드에서 완료가 되면)
            Message msg = new Message();
            msg.what = 0;
            msg.obj = builder.toString();
            handler.sendMessage(msg);   //쓰레드 작업이 완료되면 핸들러에 알린다.


        } catch (Exception e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = 1;
            msg.obj = "";
            handler.sendMessage(msg);
        } finally {
            conn.disconnect();
        }
    }
}