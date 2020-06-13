package com.kunsubin.pdfreader;

import android.Manifest;
import android.Manifest.permission;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int CODE_PERMISSIONS=1;
    private static final String TAG = "MainActivity";
    private ListView mListView;
    private List<Recently> mRecentlies;
    private RecentlyAdapter mRecentlyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        mListView=findViewById(R.id.list_view);
    
        
        //get recently cache
        String data=PreferencesHelper.getInstance().get("pref_recently","");
        Gson gson=new Gson();
        mRecentlies = gson.fromJson(data, new TypeToken<List<Recently>>(){}.getType());
        mRecentlyAdapter=new RecentlyAdapter(this);
        mRecentlyAdapter.setData(mRecentlies);
        mRecentlyAdapter.setOnItemClickListener((v,recently) -> {
    
            PopupMenu popupMenu=new PopupMenu(this,v, Gravity.CENTER);
            popupMenu.getMenuInflater()
                      .inflate(R.menu.menu_action, popupMenu.getMenu());
            
            popupMenu.setOnMenuItemClickListener(item -> {
              if(item.getItemId()==R.id.open){
                  Recently recently1=new Recently();
                  recently1.setFileName(recently.getFileName());
                  recently1.setPath(recently.getPath());
                  recently1.setDate(new Date());
    
                  if(mRecentlies==null){
                      mRecentlies=new ArrayList<>();
                      mRecentlies.add(0,recently1);
                  }else if(mRecentlies.size()<20){
                      mRecentlies.add(0,recently1);
                  }else {
                      mRecentlies.remove(mRecentlies.size()-1);
                      mRecentlies.add(0,recently1);
                  }
    
                  String dat=gson.toJson(mRecentlies);
                  PreferencesHelper.getInstance().put("pref_recently",dat);
                  mRecentlyAdapter.addItem(recently1);
    
                  //
                  Intent intent=new Intent(this, PDFReaderActivity.class);
                  intent.putExtra("PATH",recently1.getPath());
                  startActivity(intent);
                  
              }else if(item.getItemId()==R.id.delete){
                  mRecentlies.remove(recently);
                  String dat=gson.toJson(mRecentlies);
                  PreferencesHelper.getInstance().put("pref_recently",dat);
                  mRecentlyAdapter.removeItem(recently);
              }
              return true;
            });
            
            
            popupMenu.show();
            
            
            
            
        });
        mListView.setAdapter(mRecentlyAdapter);
    }
    
    public void onClickSelectFile(View view){
        view.startAnimation(AnimationUtils.getFlashAnimation());
    
        if (!PermissionUtils.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{permission.READ_EXTERNAL_STORAGE},
                      CODE_PERMISSIONS);
        }else {
            OpenFileDialog openFileDialog=new OpenFileDialog(this);
            openFileDialog.setOpenDialogListener(fileName -> {
                Log.d(TAG,"FILE: "+fileName);
    
                if(fileName.endsWith(".pdf")){
                    //add recently
                    Recently recently=new Recently();
                    File file=new File(fileName);
                    recently.setFileName(file.getName());
                    recently.setPath(fileName);
                    recently.setDate(new Date());
                    
                    if(mRecentlies==null){
                        mRecentlies=new ArrayList<>();
                        mRecentlies.add(0,recently);
                    }else if(mRecentlies.size()<20){
                        mRecentlies.add(0,recently);
                    }else {
                        mRecentlies.remove(mRecentlies.size()-1);
                        mRecentlies.add(0,recently);
                    }
                    
                    Gson gson=new Gson();
                    
                    String data=gson.toJson(mRecentlies);
                    PreferencesHelper.getInstance().put("pref_recently",data);
                    
                    mRecentlyAdapter.addItem(recently);
                    
                    //
                    Intent intent=new Intent(this, PDFReaderActivity.class);
                    intent.putExtra("PATH",fileName);
                    startActivity(intent);
        
                }else {
                    Toast.makeText(this,"not a pdf file", Toast.LENGTH_LONG).show();
                }
            });
            openFileDialog.show();
        }
        
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
              @NonNull int[] grantResults) {
        if(requestCode==CODE_PERMISSIONS){
            if (PermissionUtils.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
               //code here grant permission
                OpenFileDialog openFileDialog=new OpenFileDialog(this);
                openFileDialog.setOpenDialogListener(fileName -> {
                    Log.d(TAG,"FILE: "+fileName);
                    if(fileName.endsWith(".pdf")){
    
                        Recently recently=new Recently();
                        File file=new File(fileName);
                        recently.setFileName(file.getName());
                        recently.setPath(fileName);
                        recently.setDate(new Date());
    
                        if(mRecentlies==null){
                            mRecentlies=new ArrayList<>();
                            mRecentlies.add(0,recently);
                        }else if(mRecentlies.size()<20){
                            mRecentlies.add(0,recently);
                        }else {
                            mRecentlies.remove(mRecentlies.size()-1);
                            mRecentlies.add(0,recently);
                        }
    
                        Gson gson=new Gson();
    
                        String data=gson.toJson(mRecentlies);
                        PreferencesHelper.getInstance().put("pref_recently",data);
    
                        mRecentlyAdapter.addItem(recently);
    
                        //
                        Intent intent=new Intent(this, PDFReaderActivity.class);
                        intent.putExtra("PATH",fileName);
                        startActivity(intent);
                        
                    }else {
                        Toast.makeText(this,"not a pdf file", Toast.LENGTH_LONG).show();
                    }
                    
                });
                openFileDialog.show();
            }
        }
    }
}
