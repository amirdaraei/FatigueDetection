package com.example.amir.fatiguedetection;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amir.fatiguedetection.interFace.Filter;
import com.example.amir.fatiguedetection.scale.NoneFilter;
import com.example.amir.fatiguedetection.scale.Scale12Filter;
import com.example.amir.fatiguedetection.scale.Scale5Filter;
import com.example.amir.fatiguedetection.scale.Scale7Filter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class FdActivity extends ActionBarActivity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;

    Core.MinMaxLocResult mmG;
    Point iris;
    Rect eye_template;

    private int cameraid = 1;
    private Mat templateR_open;
    private Mat templateL_open;

    private boolean HaarEyeOpen_R = true;
    private boolean HaarEyeOpen_L = true;

    private static final int TM_SQDIFF = 0;
    private static final int TM_SQDIFF_NORMED = 1;
    private static final int TM_CCOEFF = 2;
    private static final int TM_CCOEFF_NORMED = 3;
    private static final int TM_CCORR = 4;
    private static final int TM_CCORR_NORMED = 5;


    private int learn_frames = 0;
    private Mat teplateR;
    private Mat teplateL;
    int method = 0;

    //Retinex
    // A tag for log output.
    private static final String TAG2 =
            FdActivity.class.getSimpleName();

    // A key for storing the index of the active camera.
    private static final String STATE_CAMERA_INDEX = "cameraIndex";

    // A key for storing the index of the active image size.
    private static final String STATE_IMAGE_SIZE_INDEX =
            "mimageSizeIndex";

    // Keys for storing the indices of the active filters.
    private static final String STATE_RETINEX_FILTER_INDEX =
            "retinexFilterIndex";


    // Keys for storing the indices of the active filters.
    private static final String STATE_Retinex_FILTER_INDEX =
            "retinexFilterIndex";
    // An ID for items in the image size submenu.
    private static final int MENU_GROUP_ID_SIZE = 2;

    // The filters.
    private Filter[] mRetinexFilters;

    // The indices of the active filters.
    private int mRetinexFilterIndex;

    // The index of the active camera.
    private int mCameraIndex;

    // The index of the active image size.
    private int mImageSizeIndex;

    // Whether the active camera is front-facing.
    // If so, the camera view should be mirrored.
    private boolean mIsCameraFrontFacing;

    // The number of cameras on the device.
    private int mNumCameras;

    // The image sizes supported by the active camera.
    private List<Camera.Size> mSupportedImageSizes;

    // The camera view.
    private CameraBridgeViewBase mCameraView;

    // Whether the next camera frame should be saved as a photo.
    private boolean mIsPhotoPending;

    // A matrix that is used when saving photos.
    private Mat mBgr;


    // Whether an asynchronous menu action is in progress.
    // If so, menu interaction should be disabled.
    private boolean mIsMenuLocked;


    // matrix for zooming
    Mat mZoomWindow;
    Mat mZoomWindow2;

    MenuItem               mItemFace50;
    MenuItem               mItemFace40;
    MenuItem               mItemFace30;
    MenuItem               mItemFace20;
   // private MenuItem               mItemType;

     Mat                    mRgba;
     Mat                    mGray;
    // Mat                    mCanny;
     File                   mCascadeFile;
     File                   mCascadeFileEye;
     File                   cascadeFileEyeOpen;
     CascadeClassifier      mJavaDetector;
     CascadeClassifier      mJavaDetectorEye;
     CascadeClassifier      mJavaDetectorEyeRight;
     CascadeClassifier      mJavaDetectorEyeLeft;
     CascadeClassifier      mJavaDetectorEyeOpen;

     int                    mDetectorType       = JAVA_DETECTOR;
     String[]               mDetectorName;

     float                  mRelativeFaceSize   = 0.2f;
     int                    mAbsoluteFaceSize = 0;

     CameraBridgeViewBase   mOpenCvCameraView;
     SeekBar mMethodSeekbar;
     TextView mValue;
    MediaPlayer beep;
    int FrameFace = 0;
    double xCenter = -1;
    double yCenter = -1;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                    mBgr = new Mat();
                    mRetinexFilters = new Filter[] {
                            new NoneFilter(),
                            new Scale12Filter(),
                            new Scale5Filter(),
                            new Scale7Filter()
                    };
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // load cascade file from application resources
                        InputStream ise = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                        File cascadeDirEye = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileEye = new File(cascadeDirEye, "haarcascade_lefteye_2splits.xml");
                        FileOutputStream ose = new FileOutputStream(mCascadeFileEye);

                        while ((bytesRead = ise.read(buffer)) != -1) {
                            ose.write(buffer, 0, bytesRead);
                        }
                        ise.close();
                        ose.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mJavaDetectorEye = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                        if (mJavaDetectorEye.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier for eye");
                            mJavaDetectorEye = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileEye.getAbsolutePath());

                        cascadeDir.delete();
                        cascadeDirEye.delete();


                        // ------------------ load open eye classificator -----------------------
                        InputStream opisel = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
                        File cascadeDirEyeOpen = getDir("cascadeEyeOpen",Context.MODE_PRIVATE);
                        cascadeFileEyeOpen = new File(cascadeDirEyeOpen,"haarcascade_eye_tree_eyeglasses.xml");
                        FileOutputStream oposel = new FileOutputStream(cascadeFileEyeOpen);

                        byte[] bufferEyeOpen = new byte[4096];
                        int bytesReadEyeOpen;
                        while ((bytesReadEyeOpen = opisel.read(bufferEyeOpen)) != -1) {
                            oposel.write(bufferEyeOpen, 0, bytesReadEyeOpen);
                        }
                        opisel.close();
                        oposel.close();

                        //Face Classifier
                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier of face");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "+ mCascadeFile.getAbsolutePath());
                        //cascadeDir.delete();


                        //EyeOpenClassifier
                        mJavaDetectorEyeOpen = new CascadeClassifier(cascadeFileEyeOpen.getAbsolutePath());
                        if (mJavaDetectorEyeOpen.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier of eye open");
                            mJavaDetectorEyeOpen = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "+ cascadeFileEyeOpen.getAbsolutePath());
                        //cascadeDirEyeOpen.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.setCameraIndex(1);
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
      @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(
                    STATE_CAMERA_INDEX, 0);
            mImageSizeIndex = savedInstanceState.getInt(
                    STATE_IMAGE_SIZE_INDEX, 0);
            mRetinexFilterIndex = savedInstanceState.getInt(
                    STATE_RETINEX_FILTER_INDEX, 0);
        } else {
            mCameraIndex = 0;
            mImageSizeIndex = 0;
            mRetinexFilterIndex = 0;}

        final Camera camera;
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraIndex, cameraInfo);
            mIsCameraFrontFacing =
                    (cameraInfo.facing ==
                            Camera.CameraInfo.CAMERA_FACING_FRONT);
            mNumCameras = Camera.getNumberOfCameras();
            camera = Camera.open(mCameraIndex);
        } else { // pre-Gingerbread
            // Assume there is only 1 camera and it is rear-facing.
            mIsCameraFrontFacing = false;
            mNumCameras = 1;
            camera = Camera.open();
        }
        final Camera.Parameters parameters = camera.getParameters();
        camera.release();
        mSupportedImageSizes =
                parameters.getSupportedPreviewSizes();
        final Camera.Size size = mSupportedImageSizes.get(mImageSizeIndex);

        mCameraView = new JavaCameraView(this, mCameraIndex);
        mCameraView.setMaxFrameSize(size.width, size.height);
        mCameraView.setCvCameraViewListener(this);
        setContentView(mCameraView);


        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mMethodSeekbar = (SeekBar) findViewById(R.id.methodSeekBar);
        mValue = (TextView) findViewById(R.id.method);

    //    beep = MediaPlayer.create(this, R.raw.button1);

        mMethodSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser)
            {
                method = progress;
                switch (method) {
                    case 0:
                        mValue.setText("TM_SQDIFF");
                        break;
                    case 1:
                        mValue.setText("TM_SQDIFF_NORMED");
                        break;
                    case 2:
                        mValue.setText("TM_CCOEFF");
                        break;
                    case 3:
                        mValue.setText("TM_CCOEFF_NORMED");
                        break;
                    case 4:
                        mValue.setText("TM_CCORR");
                        break;
                    case 5:
                        mValue.setText("TM_CCORR_NORMED");
                        break;
                }


            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current camera index.
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);

        // Save the current image size index.
        savedInstanceState.putInt(STATE_IMAGE_SIZE_INDEX,
                mImageSizeIndex);

        // Save the current filter indices.
        savedInstanceState.putInt(STATE_RETINEX_FILTER_INDEX,
                mRetinexFilterIndex);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void recreate() {
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.HONEYCOMB) {
            super.recreate();
        } else {
            finish();
            startActivity(getIntent());
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "opencv loaded successfuly");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        mIsMenuLocked = false;
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
//        super.onDestroy();
    }
    public void onCameraViewStarted(int width, int height) {
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray=new Mat(height,width, CvType.CV_8UC1);
//        mCanny=new Mat(height,width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
//        mGray.release();
//        mRgba.release();
//        mZoomWindow.release();
//        mZoomWindow2.release();
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
//TODo... Retinex
        // Apply the active filters.
        mRetinexFilters[mRetinexFilterIndex].apply(mRgba, mRgba);
        if (mIsPhotoPending) {
            mIsPhotoPending = false;
            takePhoto(mRgba);
        }

//        if (mIsCameraFrontFacing) {
//            // Mirror (horizontally flip) the preview.
//            Core.flip(mRgba, mRgba, 1);
//        }



        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }

        }

//        if (mZoomWindow == null || mZoomWindow2 == null)
//            CreateAuxiliaryMats();

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
        {	Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
                FACE_RECT_COLOR, 3);
            xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
            yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
            Point center = new Point(xCenter, yCenter);

            Imgproc.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 3);

            Imgproc.putText(mRgba, "[" + center.x + "," + center.y + "]",
                    new Point(center.x + 20, center.y + 20),
                    Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                            255));

            Rect r = facesArray[i];
            // compute the eye area
            Rect eyearea = new Rect(r.x + r.width / 8,
                    (int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
                    (int) (r.height / 3.0));
            // split it
            Rect eyearea_right = new Rect(r.x + r.width / 16,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
            Rect eyearea_left = new Rect(r.x + r.width / 16
                    + (r.width - 2 * r.width / 16) / 2,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
            FrameFace++;
            // draw the area - mGray is working grayscale mat, if you want to
            // see area in rgb preview, change mGray to mRgba
            Imgproc.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(),
                    new Scalar(255, 0, 0, 255), 2);
            Imgproc.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(),
                    new Scalar(255, 0, 0, 255), 2);

            if (learn_frames < 5) {
                teplateR = get_template(mJavaDetectorEye, eyearea_right, 24);
                teplateL = get_template(mJavaDetectorEye, eyearea_left, 24);
                learn_frames++;
            } else {
//                 Learning finished, use the new templates for template
//                 matching
                match_eye(eyearea_right, teplateR, method);
                match_eye(eyearea_left, teplateL, method);

            }
            //get_template function needs: classifier, area over perform classifier, and desired size of new template
            Rect rectR = get_template(mJavaDetectorEye, eyearea_right);
            Rect rectL = get_template(mJavaDetectorEye, eyearea_left);

            if (rectL.width==0 || rectL.height==0 || rectR.width==0 || rectR.height==0){continue;}



            rectR = get_template(mJavaDetectorEyeOpen, rectR, new Size(1, 1), new Size(50,50));
            templateR_open = mGray.submat(rectR);

            rectL = get_template(mJavaDetectorEyeOpen, rectL, new Size(1, 1), new Size(50,50));
            templateL_open = mGray.submat(rectL);

			/*
			if (rectL.width>0){
			    mRgba = mRgba.submat(rectR);
				Imgproc.resize(mRgba, mRgba, mGray.size());
			}
			*/

            //match_eye
            HaarEyeOpen_R = match_eye(templateR_open);
            HaarEyeOpen_L = match_eye(templateL_open);

            if(!HaarEyeOpen_R && !HaarEyeOpen_L){
                Imgproc.putText(mRgba, "Close", new Point(mRgba.size().width/18, mRgba.size().height/5), Core.FONT_HERSHEY_SCRIPT_COMPLEX, 4, new Scalar(0,255,0),5);
//                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//                tg.startTone(ToneGenerator.TONE_PROP_BEEP);
            }
//            else  (HaarEyeOpen_R && HaarEyeOpen_L)
       else     Imgproc.putText(mRgba, "Open", new Point(mRgba.size().width / 18, mRgba.size().height / 5), Core.FONT_HERSHEY_SCRIPT_COMPLEX, 4, new Scalar(0, 255, 0), 5);


            break;
        }


        return mRgba;
    }

    private void takePhoto(final Mat rgba) {

        // Determine the path and metadata for the photo.
        final long currentTimeMillis = System.currentTimeMillis();
        final String appName = getString(R.string.app_name);
        final String galleryPath =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galleryPath + File.separator +
                appName;
        final String photoPath = albumPath + File.separator +
                currentTimeMillis + LabActivity.PHOTO_FILE_EXTENSION;
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, photoPath);
        values.put(MediaStore.Images.Media.MIME_TYPE,
                LabActivity.PHOTO_MIME_TYPE);
        values.put(MediaStore.Images.Media.TITLE, appName);
        values.put(MediaStore.Images.Media.DESCRIPTION, appName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);

        // Ensure that the album directory exists.
        File album = new File(albumPath);
        if (!album.isDirectory() && !album.mkdirs()) {
            Log.e(TAG2, "Failed to create album directory at " +
                    albumPath);
            onTakePhotoFailed();
            return;
        }

        // Try to create the photo.
        Imgproc.cvtColor(rgba, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
        if (!Imgcodecs.imwrite(photoPath, mBgr)) {
            Log.e(TAG2, "Failed to save photo to " + photoPath);
            onTakePhotoFailed();
        }
        Log.d(TAG2, "Photo saved successfully to " + photoPath);

        // Try to insert the photo into the MediaStore.
        Uri uri;
        try {
            uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (final Exception e) {
            Log.e(TAG2, "Failed to insert photo into MediaStore");
            e.printStackTrace();

            // Since the insertion failed, delete the photo.
            File photo = new File(photoPath);
            if (!photo.delete()) {
                Log.e(TAG2, "Failed to delete non-inserted photo");
            }

            onTakePhotoFailed();
            return;
        }

        // Open the photo in LabActivity.
        final Intent intent = new Intent(this, LabActivity.class);
        intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri);
        intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH,
                photoPath);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        });
    }

    private void onTakePhotoFailed() {
        mIsMenuLocked = false;

        // Show an error message.
        final String errorMessage =
                getString(R.string.photo_error_message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FdActivity.this, errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private Rect get_template(CascadeClassifier clasificator, Rect RectAreaInterest) {
        Mat template = new Mat(); //Where is gonna be stored the eye detected data
        Mat mROI = mGray.submat(RectAreaInterest); //Matrix which contain data of the whole eye area from geometry of face
        MatOfRect eyes = new MatOfRect();
        iris = new Point();
        eye_template = new Rect();
        //detectMultiScale(const Mat& image, vector<Rect>& objects, double scaleFactor=1.1, int minNeighbors=3, int flags=0, Size minSize=Size(), Size maxSize=Size())
        clasificator.detectMultiScale(mROI, //Image which set classification. Needs to be of the type CV_8U
                eyes, //List of rectangles where are stored possibles eyes detected
                1.1, //Scalefactor. How much the image is reduced at each image scale
                2,    //MinNeighbors. Specify how many neighbors each candidate rectangle should have to retain it.
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE, //0 or 1.
                new Size(10, 10), //Minimum possible object size. Objects smaller than that are ignored.
                new Size(100,100)        //Maximum possible object size. Objects larger than that are ignored.
        );

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length;) {
            Rect eyeDetected = eyesArray[i];
            eyeDetected.x = RectAreaInterest.x + eyeDetected.x;
            eyeDetected.y = RectAreaInterest.y + eyeDetected.y;

            mROI = mGray.submat(eyeDetected);
            mmG = Core.minMaxLoc(mROI);

            iris.x = mmG.minLoc.x + eyeDetected.x;
            iris.y = mmG.minLoc.y + eyeDetected.y;
            eye_template = new Rect((int) iris.x -  eyeDetected.width/2, (int) iris.y -  eyeDetected.height/2,  eyeDetected.width,  eyeDetected.height);

            //Imgproc.equalizeHist(template, template);
            break;
            //return template;
        }
        return eye_template;
    }


    private Rect get_template(CascadeClassifier clasificator, Rect RectAreaInterest, Size min_size, Size max_size) {
        Mat template = new Mat(); //Where is gonna be stored the eye detected data
        Mat mROI = mGray.submat(RectAreaInterest); //Matrix which contain data of the whole eye area from geometry of face
        MatOfRect eyes = new MatOfRect();
        iris = new Point();
        eye_template = new Rect();
        //detectMultiScale(const Mat& image, vector<Rect>& objects, double scaleFactor=1.1, int minNeighbors=3, int flags=0, Size minSize=Size(), Size maxSize=Size())
        clasificator.detectMultiScale(mROI, //Image which set classification. Needs to be of the type CV_8U
                eyes, //List of rectangles where are stored possibles eyes detected
                1.01, //Scalefactor. How much the image is reduced at each image scale
                2,    //MinNeighbors. Specify how many neighbors each candidate rectangle should have to retain it.
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE, //0 or 1.
                min_size, //Minimum possible object size. Objects smaller than that are ignored.
                max_size        //Maximum possible object size. Objects larger than that are ignored.
        );

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length;) {
            Rect eyeDetected = eyesArray[i];
            eyeDetected.x = RectAreaInterest.x + eyeDetected.x;
            eyeDetected.y = RectAreaInterest.y + eyeDetected.y;

            mROI = mGray.submat(eyeDetected);
            mmG = Core.minMaxLoc(mROI);

            iris.x = mmG.minLoc.x + eyeDetected.x;
            iris.y = mmG.minLoc.y + eyeDetected.y;
            eye_template = new Rect((int) iris.x -  eyeDetected.width/2, (int) iris.y -  eyeDetected.height/2,  eyeDetected.width,  eyeDetected.height);

            //Imgproc.equalizeHist(template, template);
            break;

            //return template;
        }
        return eye_template;
    }

    private boolean match_eye(Mat mTemplate) {
        //Check for bad template size
        if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
            return false;
        }else{
            return true;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_camera, menu);
        if (mNumCameras < 2) {
            // Remove the option to switch cameras, since there is
            // only 1.
            menu.removeItem(R.id.menu_next_camera);
        }
        int numSupportedImageSizes = mSupportedImageSizes.size();
        if (numSupportedImageSizes > 1) {
            final SubMenu sizeSubMenu = menu.addSubMenu(
                    R.string.menu_image_size);
            for (int i = 0; i < numSupportedImageSizes; i++) {
                final Camera.Size size = mSupportedImageSizes.get(i);
                sizeSubMenu.add(MENU_GROUP_ID_SIZE, i, Menu.NONE,
                        String.format("%dx%d", size.width,
                                size.height));
            }
        }
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
//        //TODO...
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
            if (item == mItemFace50)
                setMinFaceSize(0.5f);
            else if (item == mItemFace40)
                setMinFaceSize(0.4f);
            else if (item == mItemFace30)
                setMinFaceSize(0.3f);
            else if (item == mItemFace20)
                setMinFaceSize(0.2f);


        if (mIsMenuLocked) {
            return true;
        }
        if (item.getGroupId() == MENU_GROUP_ID_SIZE) {
            mImageSizeIndex = item.getItemId();
            recreate();

            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_next_retinex_filter:
                mRetinexFilterIndex++;
                if (mRetinexFilterIndex == mRetinexFilters.length) {
                    mRetinexFilterIndex = 0;
                }
            case R.id.menu_next_camera:
                mIsMenuLocked = true;

                // With another camera index, recreate the activity.
                mCameraIndex++;
                if (mCameraIndex == mNumCameras) {
                    mCameraIndex = 0;
                }
                recreate();

                return true;
            case R.id.menu_take_photo:
                mIsMenuLocked = true;

                // Next frame, take the photo.
                mIsPhotoPending = true;

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    public void onToggleClick(View v) {
        cameraid = cameraid^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(cameraid);
        mOpenCvCameraView.enableView();
    }

    private void CreateAuxiliaryMats() {
        if (mGray.empty())
            return;

        int rows = mGray.rows();
        int cols = mGray.cols();

        if (mZoomWindow == null) {
            mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
                    + cols / 10, cols);
            mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
                    + cols / 10, cols);
        }

    }

    private void match_eye(Rect area, Mat mTemplate, int type) {
        Point matchLoc;
        Mat mROI = mGray.submat(area);
        int result_cols = mROI.cols() - mTemplate.cols() + 1;
        int result_rows = mROI.rows() - mTemplate.rows() + 1;
        // Check for bad template size
        if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
            return ;
        }
        Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

        switch (type) {
            case TM_SQDIFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
                break;
            case TM_SQDIFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_SQDIFF_NORMED);
                break;
            case TM_CCOEFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
                break;
            case TM_CCOEFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_CCOEFF_NORMED);
                break;
            case TM_CCORR:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
                break;
            case TM_CCORR_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_CCORR_NORMED);
                break;
        }

        Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
        // there is difference in matching methods - best match is max/min value
        if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
            matchLoc = mmres.minLoc;
        } else {
            matchLoc = mmres.maxLoc;
        }

        Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
        Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x,
                matchLoc.y + mTemplate.rows() + area.y);

        Imgproc.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 255, 0,
                255));
        Rect rec = new Rect(matchLoc_tx,matchLoc_ty);


    }

    private Mat get_template(CascadeClassifier clasificator, Rect area, int size) {
        Mat template = new Mat();
        Mat mROI = mGray.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();
        Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                        | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30),
                new Size());

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length;) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            Rect eye_only_rectangle = new Rect((int) e.tl().x,
                    (int) (e.tl().y + e.height * 0.4), (int) e.width,
                    (int) (e.height * 0.6));
            mROI = mGray.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);


            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            Imgproc.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int) iris.x - size / 2, (int) iris.y
                    - size / 2, size, size);
            Imgproc.rectangle(mRgba, eye_template.tl(), eye_template.br(),
                    new Scalar(255, 0, 0, 255), 2);
            template = (mGray.submat(eye_template)).clone();
            return template;
        }
        return template;
    }

    public void onRecreateClick(View v)
    {
        learn_frames = 0;
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void camera() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FdActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void cameraPermissions(final PermissionRequest request) {

        FdActivityPermissionsDispatcher.cameraWithCheck(this);
    }
}
