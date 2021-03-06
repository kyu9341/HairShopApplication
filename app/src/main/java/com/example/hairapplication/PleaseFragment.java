package com.example.hairapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PleaseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PleaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PleaseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PleaseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PleaseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PleaseFragment newInstance(String param1, String param2) {
        PleaseFragment fragment = new PleaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    AlertDialog dialog;
    private ListView pleaseListView;
    private PleaseListAdapter adapter;
    private List<Please> pleaseList;
 //   private int pleaseNum;

    @Override
    public void onActivityCreated(Bundle b){
        super.onActivityCreated(b);
        Button writeButton = (Button)getView().findViewById(R.id.writeButton);
        pleaseListView = (ListView)getView().findViewById(R.id.pleaseListView);
        pleaseList = new ArrayList<Please>(); // 배열에 넣어줌

        Log.e("nickname = "+MainActivity.nickname, "nickname");

   //     TextView test = (TextView)getView().findViewById(R.id.test);
 //       pleaseList.add(new Please("상담부탁드립니다.", "닉네임", "2019-05-04","asdf"));
 //       pleaseList.add(new Please("상담부탁드립니다.1", "닉네임1", "2019-05-04","dffffffff"));

 //       final String nickname = getArguments().getString("nickname"); // 전달한 key 값
   //     Log.e("nickname = "+nickname , "nickname");
    //    test.setText("나오냐? >>"+MainActivity.nickname+" << ");

        adapter = new PleaseListAdapter(getContext().getApplicationContext(), pleaseList, this);
        pleaseListView.setAdapter(adapter); //리스트 뷰에 어댑터 매칭

        new BackgroundTask().execute(); // 데이터베이스 연동

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 글쓰기 화면으로 이동
                Intent intent = new Intent(getActivity(), WriteActivity.class);
 //               intent.putExtra("idText", idText);
                startActivity(intent);
            }
        });


        pleaseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {



                Log.e("type = "+MainActivity.type, "type");

                if(pleaseList.get(i).getAccess() == 1 || MainActivity.nickname.equals(pleaseList.get(i).getName()) ) {


                    Intent intent = new Intent(getActivity(), PleaseContentsActivity.class);

                    intent.putExtra("Title", pleaseList.get(i).please);  // 인텐트로 정보를 넘겨줌
                    intent.putExtra("Name", pleaseList.get(i).name);
                    intent.putExtra("Date", pleaseList.get(i).date);
                    intent.putExtra("Contents", pleaseList.get(i).contents);
                    intent.putExtra("Image", pleaseList.get(i).image);
                    intent.putExtra("Image2", pleaseList.get(i).image2);
                    intent.putExtra("Index", pleaseList.get(i).num);

                    startActivity(intent);
                }else if( MainActivity.type.equals("designer")){
                    Intent intent = new Intent(getActivity(), PleaseContentsActivity.class);

                    intent.putExtra("Title", pleaseList.get(i).please);  // 인텐트로 정보를 넘겨줌
                    intent.putExtra("Name", pleaseList.get(i).name);
                    intent.putExtra("Date", pleaseList.get(i).date);
                    intent.putExtra("Contents", pleaseList.get(i).contents);
                    intent.putExtra("Image", pleaseList.get(i).image);
                    intent.putExtra("Image2", pleaseList.get(i).image2);
                    intent.putExtra("Index", pleaseList.get(i).num);

                    startActivity(intent);
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    dialog = builder.setMessage("비밀글 입니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();

                }

            }
        });


    }

    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected void onPreExecute(){
            target = "http://kyu9341.cafe24.com/PleaseList.php";
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
                String  pleaseTitle, pleaseName, pleaseDate, pleaseContents, pleaseImage, pleaseImage2; // 변수 선언
                int pleaseNum, pleaseAccess;
                while(count < jsonArray.length())
                {
                    JSONObject object = jsonArray.getJSONObject(count); // 현재 배열의 원소값
                    pleaseTitle = object.getString("pleaseTitle");
                    pleaseName = object.getString("pleaseName");
                    pleaseDate = object.getString("pleaseDate");
                    pleaseContents = object.getString("pleaseContents");
                    pleaseNum = object.getInt("pleaseNum");
                    pleaseImage = object.getString("pleaseImage");
                    pleaseImage2 = object.getString("pleaseImage2");
                    pleaseAccess = object.getInt("access");

                    Please please = new Please(pleaseNum, pleaseTitle, pleaseName, pleaseDate, pleaseContents, pleaseImage, pleaseImage2, pleaseAccess); // 객체 생성 (생성자)
                    pleaseList.add(please); // 리스트에 추가
                    adapter.notifyDataSetChanged();


                    Log.e("image = "+pleaseImage, "Image");
                    count++;

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_please, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }




    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
