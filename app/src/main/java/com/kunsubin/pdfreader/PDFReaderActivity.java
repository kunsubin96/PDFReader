package com.kunsubin.pdfreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import com.kunsubin.pdfreader.photoview.PhotoView;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PDFReaderActivity extends AppCompatActivity {
    
    private static final String TAG = "PDFReaderActivity";
    private final Executor mExecutor= Executors.newSingleThreadExecutor();;
    private PhotoView mPhotoViewReader;
    private TextView mTextViewPageNumber;
    private TextView mTextViewTitle;
    
    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    
    private int mIndex=0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        
        if(savedInstanceState!=null){
            mIndex=savedInstanceState.getInt("current_page");
        }
        
        init();
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_page",mCurrentPage.getIndex());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutor.execute(() -> {
            try {
                closePdfRenderer();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close Pdf", e);
            }
        });
    }
    
    private void init() {
        mPhotoViewReader = findViewById(R.id.image);
        mTextViewPageNumber=findViewById(R.id.text_page_number);
        mTextViewTitle=findViewById(R.id.title);
    
    
        /*mImageViewReader.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeTop() {
                showNext();
            }
            public void onSwipeRight() {
                showPrevious();
            }
            public void onSwipeLeft() {
                showNext();
            }
            public void onSwipeBottom() {
                showPrevious();
            }
        });*/
        
        //get intent
        Intent intent = getIntent();
        if(intent==null)
            return;
        if(intent.getStringExtra("PATH")==null)
            return;
        String pathFile = intent.getStringExtra("PATH");
        File file=new File(pathFile);
    
        mTextViewTitle.setText(file.getName());
        
        //init open pdf
        mExecutor.execute(() -> {
            try {
                openPdfRenderer(file);
                showPage(mIndex);
            } catch (IOException e) {
                Log.e(TAG, "Failed to open Pdf", e);
                runOnUiThread(() -> {
                    Toast.makeText(this,"Failed to open Pdf", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    public void onClickPrevious(View view){
        view.startAnimation(AnimationUtils.getFlashAnimation());
        showPrevious();
    }
    public void onClickNext(View view){
        view.startAnimation(AnimationUtils.getFlashAnimation());
        showNext();
    }
    
    public void onClickBack(View view){
        view.startAnimation(AnimationUtils.getFlashAnimation());
        this.finish();
    }
    
    @WorkerThread
    private void openPdfRenderer(File file) throws IOException {
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }
    
    @WorkerThread
    private void closePdfRenderer() throws IOException {
        if (mCurrentPage != null) {
            mCurrentPage.close();
        }
        if (mPdfRenderer != null) {
            mPdfRenderer.close();
        }
        if (mFileDescriptor != null) {
            mFileDescriptor.close();
        }
    }
    
    @WorkerThread
    private void showPage(int index) {
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        
        /*final Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                  Bitmap.Config.ARGB_8888);*/
        Bitmap bitmap = Bitmap.createBitmap(
                  getResources().getDisplayMetrics().densityDpi * mCurrentPage.getWidth() / 72,
                  getResources().getDisplayMetrics().densityDpi * mCurrentPage.getHeight() / 72,
                  Bitmap.Config.ARGB_8888
        );
        // Paint bitmap before rendering
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        
        //show ui
        runOnUiThread(() -> {
            //mPhotoViewReader.setImageBitmap(bitmap);
            
            AnimationUtils.imageViewAnimatedChange(this,mPhotoViewReader,bitmap);
            
            final int count = mPdfRenderer.getPageCount();
            mTextViewPageNumber.setText((index+1)+"/"+count);
        });
    }
    void showPrevious() {
        if (mPdfRenderer == null || mCurrentPage == null) {
            return;
        }
        final int index = mCurrentPage.getIndex();
        if (index > 0) {
            mExecutor.execute(() -> showPage(index - 1));
        }
    }
    
    void showNext() {
        if (mPdfRenderer == null || mCurrentPage == null) {
            return;
        }
        final int index = mCurrentPage.getIndex();
        if (index + 1 < mPdfRenderer.getPageCount()) {
            mExecutor.execute(() -> showPage(index + 1));
        }
    }
}
