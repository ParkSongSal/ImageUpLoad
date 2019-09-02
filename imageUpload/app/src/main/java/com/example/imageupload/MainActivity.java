package com.example.imageupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText mImg_Title;
    private Button mChooseBtn,mUploadBtn;
    private ImageView mImg;
    private static final int IMG_REQUEST = 777;
    private Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImg_Title = findViewById(R.id.img_title);

        mImg = findViewById(R.id.imageView);

        mChooseBtn = findViewById(R.id.choose_Btn);
        mUploadBtn = findViewById(R.id.upload_Btn);

        mChooseBtn.setOnClickListener(this);
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //selectImage();
                selectImage();
            }
        });


        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            //설명을 보여줄 것인가
            if(ActivityCompat.shouldShowRequestPermissionRationale(this
                    ,Manifest.permission.READ_EXTERNAL_STORAGE)){

                // 사용자 응답을 기다리는 설명을 비동기로 보여주기
                // 권한 체크를 안 하면 이 기능을 사용할 수 없다고 어필하고

                // 다이얼로그 표시
                // 이 권한을 수락하지 않으면 이 기능을 사용할 수 없습니다.ㅣ
                // 권한을 설정하시려면 설정 > 애플리케이션 > 앱이름 가서 설정하시오

                // 다시 권한 요청

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                        ,1000);
            }else{
                //권한을 요청
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1000);
            }
        }else{

            // 이미 권한이 존재할 때
            selectImage();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1000:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    //승인 됨
                    Toast.makeText(this,"권한 승인됨", Toast.LENGTH_LONG).show();
                    selectImage();
                }else{


                    // 앱을 종료함
                    //승인 거부됨
                    Toast.makeText(this,"권한 거부됨", Toast.LENGTH_LONG).show();
                    MainActivity.this.finish();
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.choose_Btn:
                selectImage();
                break;

        }
    }

    private void uploadImage(Uri filePath) {

        //String Image = imageToString();
        String Title = mImg_Title.getText().toString();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);



        File originalFile = FileUtils.getFile(this, filePath);
        RequestBody titlePart = RequestBody.create(MultipartBody.FORM,Title);
        RequestBody imagePart = RequestBody.create(
                MediaType.parse(getContentResolver().getType(filePath)),
                originalFile);

        MultipartBody.Part file = MultipartBody.Part.createFormData("image",originalFile.getName(),imagePart);

       Call<ResponseBody> call = apiInterface.uploadImage(titlePart,file);
       call.enqueue(new Callback<ResponseBody>() {
           @Override
           public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
               Log.d("TAG","imageUpload Success.." + response.body());
               Toast.makeText(MainActivity.this,"success",Toast.LENGTH_LONG).show();
           }

           @Override
           public void onFailure(Call<ResponseBody> call, Throwable t) {
               Toast.makeText(MainActivity.this,"faild",Toast.LENGTH_LONG).show();
               Log.d("TAG","imageUpload Faild.." + t.getMessage());
           }
       });



    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMG_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null){

            Uri path = data.getData();

            uploadImage(path);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);
                mImg.setImageBitmap(bitmap);
                mImg.setVisibility(View.VISIBLE);
                mImg_Title.setVisibility(View.VISIBLE);
                mChooseBtn.setEnabled(false);
                mUploadBtn.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String imageToString(){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte,Base64.DEFAULT);
    }


}
