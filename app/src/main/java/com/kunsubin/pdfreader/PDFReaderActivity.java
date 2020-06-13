package com.kunsubin.pdfreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PDFReaderActivity extends AppCompatActivity {
    
    private static final String FILENAME = "sample.pdf";
    private static final String TAG = "PDFReaderActivity";
    private final Executor mExecutor= Executors.newSingleThreadExecutor();;
    private ImageView mImageViewReader;
    private TextView mTextViewPageNumber;
    private TextView mTextViewTitle;
    
    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        init();
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
        mImageViewReader = findViewById(R.id.image);
        mTextViewPageNumber=findViewById(R.id.text_page_number);
        mTextViewTitle=findViewById(R.id.title);
    
    
        mImageViewReader.setOnTouchListener(new OnSwipeTouchListener(this){
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
        });
        
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
                showPage(0);
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
       // final File file = new File(getApplication().getCacheDir(), FILENAME);
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            final InputStream asset = getApplication().getAssets().open(FILENAME);
            final FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
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
        
        final Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                  Bitmap.Config.ARGB_8888);
        
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        
        //show ui
        runOnUiThread(() -> {
            mImageViewReader.setImageBitmap(bitmap);
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
