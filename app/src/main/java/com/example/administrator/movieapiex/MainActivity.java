package com.example.administrator.movieapiex;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    DatePickerDialog picker;
    EditText et_date;
    Button btn_search;
    ListView list_movie;
    Calendar maxCalendar = Calendar.getInstance();

    ProgressBar prog_01;


    List<String> movies;
    ArrayAdapter<String> adapter;

    // 상세보기를 위한 인텐트 선언
    Intent intent;

    static final String API_KEY = "key=430156241533f1d058c603178cc3ca0e";
    static final String API_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?";

    private final MyHandle myHandle = new MyHandle(this);

    // 영화 진흥원 API 호출 메서드
    public void getApiForMovies(String param) {
        movies.clear(); // 버튼 호출시 마다 데이터 방지하기 위해서
        // 리스트를 초기화
        String url = API_URL + API_KEY + "&targetDt=" + param;
        // 작업을 담당할 스레드 객체
        GetMyThread thread = new GetMyThread(myHandle, null, url);
        thread.start();

    }

    // 쓰레드에서 사용할 핸들러 객체 만들기 (내부 클래스)
    private static class MyHandle extends Handler {
        private final WeakReference<MainActivity> mActivity;

        // 생성자 주입방식
        public MyHandle(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        // 통신이 끝난 후에 호출되는 메소드
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    // Message는 android.os것 임포트
    // 핸들러에서 메세지 처리를 위한 메소드
    // 컨트롤러역할
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case 0: //성공
                // 수신받은 데이터를 listview에 장착
                String jsonStr = String.valueOf(msg.obj);
                try {
                    // 문자열을 json 객체로 바꾸고 key값을 이용해서 데이터를 꺼낸다.
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONObject boxOfficeResult = jsonObject.getJSONObject("boxOfficeResult");
                    JSONArray dailyBoxOfficeList = boxOfficeResult.getJSONArray("dailyBoxOfficeList");

                    for (int i = 5; i < dailyBoxOfficeList.length(); i++) {

                        JSONObject item = dailyBoxOfficeList.getJSONObject(i);
                        String rank = item.getString("rank");
                        String title = item.getString("movieNm");
                        String movieCd = item.getString("movieCd");

                        movies.add("rank: " + rank + " / 제목" + title + " / " + movieCd);
                    }
                    // 모델의 데이터가 변경되었다고 어댑터 객체에 알리기

                    adapter.notifyDataSetChanged();
                    Log.d(">>>", movies.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1: //실패
                Toast.makeText(this, "데이터 수신에 실패했습니다.", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void showAndHideView(View show, View hide) {
        show.setVisibility(View.VISIBLE);
        hide.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 객체 생성
        et_date = findViewById(R.id.et_date);
        btn_search = findViewById(R.id.btn_search);
        list_movie = findViewById(R.id.list_movie);
        prog_01 = findViewById(R.id.prog_01);

        // 리스트뷰 아이템 클릭 이벤트 부여
        list_movie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent= new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("movieCd",parent.getItemAtPosition(position)+"");
                Log.d("data>>>",parent.getItemAtPosition(position)+"");
                startActivity(intent);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String param = et_date.getText().toString();
                getApiForMovies(param);

                // prog보이고, list_movie 숨기고 상속
                showAndHideView(prog_01, list_movie);
            }
        });
        // et클릭시 date picker dialog 날짜 선택하는 창 생성하기
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 달력 클래스
                Calendar calendar = Calendar.getInstance();
                int _year = calendar.get(Calendar.YEAR);
                int _month = calendar.get(Calendar.MONTH);
                int _day = calendar.get(Calendar.DAY_OF_MONTH);

                // datepicker생성
                picker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Strinf.format은 (인자1, 2) 2에 들어오는 숫자를 1 형식으로 맞춰줘
                        String month_str = String.format("%02d", (month + 1));
                        String day_str = String.format("%02d", dayOfMonth);

                        // 눌렀을때 데이터 세팅되는 곳
                        et_date.setText(year + month_str + day_str);
                    }
                }, _year, _month, _day);
                // picker 객체 보이기
                picker.show();

                // 최대 선택일 제한하기
                // 한번만 호출되는 곳으로 옮기면 에러가 안난다. 수정해보기.
                maxCalendar.add(Calendar.DATE, -1); // 오늘부터 하루 빼라
                picker.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());
            }
        });


        // 리스트 뷰에 모델객체 연결
        movies = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, movies);

        // 리스트 뷰에 어댑터 연결
        list_movie.setAdapter(adapter);
        list_movie.setDivider(new ColorDrawable(Color.YELLOW)); // 구분선 색상
        list_movie.setDividerHeight(2); // 구분선 굵기
    }
}
