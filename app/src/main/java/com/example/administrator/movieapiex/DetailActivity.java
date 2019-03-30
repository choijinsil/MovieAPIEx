package com.example.administrator.movieapiex;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class DetailActivity extends AppCompatActivity {
    TextView tv_movie_nm, tv_show_tm, tv_open_dt, tv_director;
    Intent intent;

    static final String API_KEY = "key=430156241533f1d058c603178cc3ca0e";
    static final String API_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?";

    private final MyHandle myHandle = new MyHandle(this);

    // 영화 진흥원 API 호출 메서드
    public void getApiForMovies(String param) {

        // 리스트를 초기화
        String url = API_URL + API_KEY + "&movieCd=" + param;
        // 작업을 담당할 스레드 객체
        GetMyThread thread = new GetMyThread(myHandle, null, url);
        thread.start();

    }

    // 쓰레드에서 사용할 핸들러 객체 만들기 (내부 클래스)
    private static class MyHandle extends Handler {
        private final WeakReference<DetailActivity> mActivity;

        // 생성자 주입방식
        public MyHandle(DetailActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        // 통신이 끝난 후에 호출되는 메소드
        @Override
        public void handleMessage(Message msg) {
            DetailActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case 0: //성공
                // 수신받은 데이터를 listview에 장착
                String jsonStr = String.valueOf(msg.obj);
                try {
                    // 문자열을 json 객체로 바꾸고 key값을 이용해서 데이터를 꺼낸다.
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONObject movieInfoResult = jsonObject.getJSONObject("movieInfoResult");
                    JSONObject movieInfo = movieInfoResult.getJSONObject("moveiInfo");

                    tv_movie_nm.setText(movieInfo.get("movieNm") + "");
                    tv_open_dt.setText(movieInfo.get("openDt") + "");
                    tv_show_tm.setText(movieInfo.get("showTm") + "");

                    //감독개체 데이터 바인딩
                    JSONArray directors = movieInfo.getJSONArray("directors");
                    if (directors.length() > 0) {
                        // 감독 배열의 0번째 방의 정보를 가져온다.
                        JSONObject director = directors.getJSONObject(0);
                        tv_director.setText(director.getString("peopleNm"));

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1: //실패
                Toast.makeText(this, "데이터 수신에 실패했습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tv_movie_nm = findViewById(R.id.tv_movie_nm);
        tv_show_tm = findViewById(R.id.tv_show_tm);
        tv_open_dt = findViewById(R.id.tv_open_dt);
        tv_director = findViewById(R.id.tv_director);

        // 인텐트 객체 수신
        intent = getIntent();
        String tmp = intent.getExtras().getString("movieCd");
        getMovieCd(tmp);
        Log.d("data>>>", getMovieCd(tmp));
        // 영화 상세목록 호출
        if (intent != null) {
            getApiForMovies(getMovieCd(tmp));
        }
    }

    // 넘겨받은 데이터를 필요한 데이터 부분으로 자르기
    public String getMovieCd(String data) {

        return data.split(" / ")[2];
    }
}
