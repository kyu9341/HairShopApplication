package com.example.hairapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Rating;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewContentsActivity extends AppCompatActivity {


    private String reviewcommentName;
    private String reviewcommentDate;
    private String reviewcommentContents;

    private AlertDialog dialog; // 알림창

    private ListView reviewcommentListView;
    private ReviewCommentListAdapter adapter;
    private List<ReviewComment> reviewcommentList;
    private int Index;

    private ImageView iv_view;
    private ImageView iv_view2;
    // LOG
    private Log_Class LOG = new Log_Class();
    private String TAG = this.getClass().getSimpleName()+"_LOG";

    private static Bitmap bPicture = null;


    // URL
    private String ServerIP = "http://kyu9341.cafe24.com/image_up_down/";
    private String ImageToServerURL = "ImageUploadToServer.php";

    private String sPictureUrl;
    private String sPictureUrl2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_contents);

        long now = System.currentTimeMillis();  // 현재 시간 받아오기
        Date date1 = new Date(now);
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String strCurDate = CurDateFormat.format(date1);
        reviewcommentDate = strCurDate; // 현재 날짜 저장

        final ScrollView sv1;
        sv1 = (ScrollView)findViewById(R.id.sv1);
        iv_view = (ImageView)findViewById(R.id.iv_view);
        iv_view2 = (ImageView)findViewById(R.id.iv_view2);
        final EditText reviewcommentText= (EditText)findViewById(R.id.reviewcommentText);

        Intent intent = getIntent();

        RatingBar reviewRating = (RatingBar)findViewById(R.id.reviewRating);
        Button completeButton = (Button)findViewById(R.id.completeButton);
        TextView writer = (TextView) findViewById(R.id.writer);
        TextView title = (TextView) findViewById(R.id.title);
        TextView date = (TextView) findViewById(R.id.date);
        TextView contents = (TextView)findViewById(R.id.contents);

        title.setText(" " + intent.getStringExtra("Title"));
        writer.setText(intent.getStringExtra("Name"));
        date.setText(intent.getStringExtra("Date"));
        contents.setText(intent.getStringExtra("Contents"));
        reviewRating.setRating(intent.getFloatExtra("Rate", 1));
        sPictureUrl = intent.getStringExtra("Image");
        sPictureUrl2 = intent.getStringExtra("Image2");
        Log.e("image = "+sPictureUrl, "Image");
        Log.e("image2 = "+sPictureUrl2, "Image2");

        contents.setMovementMethod(new ScrollingMovementMethod());

        contents.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                sv1.requestDisallowInterceptTouchEvent(true);
                //스크롤뷰가 텍스트뷰의 터치이벤트를 가져가지 못하게 함
                return false;
            }
        });

        reviewcommentText.setOnTouchListener(new View.OnTouchListener() { // 댓글 작성 시 스크롤 생성
            public boolean onTouch(View view, MotionEvent event) {
                if (view.getId() ==R.id.commentText) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()&MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        Index = intent.getIntExtra("Index", 1); // reviewList의 고유 번호, 이 값을 reviewcomment테이블에 넣고 그에 맞는 댓글을 가져옴

        reviewcommentListView = (ListView)findViewById(R.id.ReviewCommentListView);
        reviewcommentList = new ArrayList<ReviewComment>(); // 배열에 넣어줌

        adapter = new ReviewCommentListAdapter(getApplicationContext(), reviewcommentList, this);
        reviewcommentListView.setAdapter(adapter); //리스트 뷰에 어댑터 매칭

       new ReviewContentsActivity.BackgroundTask().execute(); // 데이터베이스 연동


        ReviewContentsActivity.GetImageFromServer GetImageFromServer_th = new ReviewContentsActivity.GetImageFromServer();
        GetImageFromServer_th.execute();

        ReviewContentsActivity.GetImageFromServer2 GetImageFromServer_th2 = new ReviewContentsActivity.GetImageFromServer2();
        GetImageFromServer_th2.execute();

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 댓글 입력
                reviewcommentName = MainActivity.nickname;
                reviewcommentContents = reviewcommentText.getText().toString();

                if(reviewcommentContents.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewContentsActivity.this);
                    dialog = builder.setMessage("내용을 입력해주세요.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }


                Response.Listener<String> responseListener = new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if(success){ // 작성에 성공했을 경우 성공 알림창 출력
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReviewContentsActivity.this);
                                dialog = builder.setMessage("작성에 성공했습니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();


                            }else{ // 작성에 실패한 경우 실패 알림창 출력
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReviewContentsActivity.this);
                                dialog = builder.setMessage("작성에 실패했습니다.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();

                            }
                        }
                        catch (Exception e){
                            e.printStackTrace(); // 오류 출력
                        }

                    }
                } ;
                ReviewCommentRequest reviewcommentRequest = new ReviewCommentRequest(Index, reviewcommentName, reviewcommentDate, reviewcommentContents, responseListener);
                RequestQueue queue = Volley.newRequestQueue(ReviewContentsActivity.this);
                queue.add(reviewcommentRequest);
            }
        });

        if(sPictureUrl.equals("noimage")){
            iv_view.setVisibility(View.GONE);
        }  if(sPictureUrl2.equals("noimage")){
            iv_view2.setVisibility(View.GONE);
        }


    }




    class BackgroundTask extends AsyncTask<Void, Void, String> // 서버에 review리스트 뷰의 인덱스를 전송하여 그 인덱스에 해당하는 댓글만을 가져와 리스트 뷰로 출력
    {
        String target;

        @Override
        protected void onPreExecute(){
            try{
                target = "http://kyu9341.cafe24.com/ReviewCommentList.php?reviewCommentIndex="+URLEncoder.encode(Index+""); // GET 방식으로 인덱스를 서버에 전송
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream(); // 넘어오는 결과값들을 저장
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); // 해당 inputstream에 있는 내용들을 버퍼에 담아 읽을수 있도록 함.
                String temp;
                StringBuilder stringBuilder = new StringBuilder(); // 문자열 형태로 저장
                while ((temp = bufferedReader.readLine()) != null){  // 버퍼에서 받아오는 값을 한줄씩 읽으면 temp에 저장
                    stringBuilder.append(temp + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onProgressUpdate(Void... values){
            super.onProgressUpdate();
        }

        @Override
        public void onPostExecute(String result){ // 해당 결과 처리
            try{
                JSONObject jsonObject = new JSONObject(result); // 응답 부분 처리
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                int count = 0;
                String reviewcommentName, reviewcommentDate, reviewcommentContents; // 변수 선언

                while(count < jsonArray.length())
                {
                    JSONObject object = jsonArray.getJSONObject(count); // 현재 배열의 원소값

                    reviewcommentName = object.getString("reviewCommentName");
                    reviewcommentDate = object.getString("reviewCommentDate");
                    reviewcommentContents = object.getString("reviewCommentContents");

                    ReviewComment reviewcomment = new ReviewComment(reviewcommentName, reviewcommentDate, reviewcommentContents); // 객체 생성 (생성자)
                    reviewcommentList.add(reviewcomment); // 리스트에 추가
                    adapter.notifyDataSetChanged();

                    count++;


                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    // =============================================================================================
    // =========================== 서버로부터 이미지 받아오는 스레드 ===============================
    private class GetImageFromServer extends AsyncTask<String, String, String> {
        URL Url;

        @Override
        protected void onPreExecute() {
            LOG.i(TAG,"::::: [GetImageFromServer Start] :::::");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                if (!sPictureUrl.equals("")) {
                    Url = new URL(ServerIP+sPictureUrl);
                    HttpURLConnection conn;
                    conn = (HttpURLConnection) Url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();

                    Bitmap PictureDefault = BitmapFactory.decodeStream(is);
                    bPicture = PictureDefault != null ? PictureDefault : BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    conn.disconnect();
                    is.close();
                    return "True";
                }

            } catch (UnsupportedEncodingException e) {
                LOG.i(TAG,"[GetImageFromServer] Frist UnsupportedEncodingException");
                e.printStackTrace();
                return "UnsupportedEncodingException";
            } catch (IOException e) {
                LOG.i(TAG,"[GetImageFromServer] Frist IOException");
                e.printStackTrace();
                return "IOException";
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("True")){
                iv_view.setImageBitmap(bPicture);
      //          Toast.makeText(ReviewContentsActivity.this, "이미지 다운로드 성공.", Toast.LENGTH_SHORT).show();
            }
            else if(s.equals("UnsupportedEncodingException")){

            }
            else if(s.equals("IOException")){
                //      Toast.makeText(PleaseContentsActivity.this, "서버에서 파일을 다운로드 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ReviewContentsActivity.this, "실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // =============================================================================================


    // =============================================================================================
    // =========================== 서버로부터 이미지 받아오는 스레드2 ===============================
    private class GetImageFromServer2 extends AsyncTask<String, String, String> {
        URL Url;

        @Override
        protected void onPreExecute() {
            LOG.i(TAG,"::::: [GetImageFromServer Start] :::::");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                if (!sPictureUrl2.equals("")) {
                    Url = new URL(ServerIP+sPictureUrl2);
                    HttpURLConnection conn;
                    conn = (HttpURLConnection) Url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();

                    Bitmap PictureDefault = BitmapFactory.decodeStream(is);
                    bPicture = PictureDefault != null ? PictureDefault : BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    conn.disconnect();
                    is.close();
                    return "True";
                }

            } catch (UnsupportedEncodingException e) {
                LOG.i(TAG,"[GetImageFromServer] Frist UnsupportedEncodingException");
                e.printStackTrace();
                return "UnsupportedEncodingException";
            } catch (IOException e) {
                LOG.i(TAG,"[GetImageFromServer] Frist IOException");
                e.printStackTrace();
                return "IOException";
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("True")){
                iv_view2.setImageBitmap(bPicture);
                //         Toast.makeText(PleaseContentsActivity.this, "이미지 다운로드 성공.", Toast.LENGTH_SHORT).show();
            }
            else if(s.equals("UnsupportedEncodingException")){

            }
            else if(s.equals("IOException")){
                //      Toast.makeText(PleaseContentsActivity.this, "서버에서 파일을 다운로드 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ReviewContentsActivity.this, "실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // =============================================================================================

}
