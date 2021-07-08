package com.codeplay.scanner_demo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Tham on 17/07/16.
 */
public class BarcodeCaptureFragment extends BaseFragment {
    public static final String TAG = "Barcode Scanner";
    public static final int MODE_DEFAULT = 2;
    // constants used to pass extra data in the intent
    public static final String EXT_CATEGORY = "mobile_handover.barcodecapture.extra.Category";
    public static final String EXT_BARCODES = "mobile_handover.barcodecapture.extra.Barcodes";
    public static final String EXT_LAST_SCANNED = "mobile_handover.barcodecapture.extra.Last_Scanned";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_SCANNED = 3;
    // intent request code to exit function due to camera permission denied.
    private static final int RC_HANDLE_EXIT_FUNC = 4;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView tvCategory, tvLastScanned;
    private RelativeLayout rlResult;
    private TextView tvResult;
    private String lastScanned;
    private boolean pendingExitDialog;
    private ArrayList<com.codeplay.scanner_demo.objects.Barcode> barcodes;
    private long prevFrameTimestamp;

    public static BarcodeCaptureFragment newInstance(int categoryStringId, ArrayList<Barcode> scannedList) {
        if (scannedList==null)
            scannedList = new ArrayList<>();

        BarcodeCaptureFragment fragment = new BarcodeCaptureFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(EXT_CATEGORY, categoryStringId);
        bundle.putParcelableArrayList(EXT_BARCODES, scannedList);
        // Note: setArguments method can only be called before the fragment is attached to an Activity.
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_capture, container, false);

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.beep);
        mediaPlayer.setLooping(false);

        barcodes = requireArguments().getParcelableArrayList(EXT_BARCODES);
        final int size = barcodes.size();
        if (size>0)
            lastScanned = barcodes.get(size-1).get();
        else
            lastScanned = "-";

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        tvCategory = view.findViewById(R.id.text_category);
        tvCategory.setText(getString(requireArguments().getInt(EXT_CATEGORY)));
        tvLastScanned = view.findViewById(R.id.text_last_scanned);
        tvLastScanned.setText(getString(R.string.last_scanned, lastScanned));
        cameraView = view.findViewById(R.id.camera_view);
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCameraSource();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (cameraSource!=null)
                    cameraSource.stop();
            }
        });
        rlResult = view.findViewById(R.id.result_container);
        tvResult = view.findViewById(R.id.result);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int permissionCheck = ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA);
        if (permissionCheck==PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        eventsListener.setFragment(this);
        eventsListener.onResume(MODE_DEFAULT);

        if (pendingExitDialog) {
            showDialog(
                    this,
                    RC_HANDLE_EXIT_FUNC,
                    R.string.app_name,
                    R.string.no_camera_permission,
                    null,
                    0,
                    0,
                    true
            );
            pendingExitDialog = false;
        }

        startCameraSource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseCameraSource();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RC_HANDLE_SCANNED:
                if (resultCode==Activity.RESULT_OK) {
                    startCameraSource();
                    tvLastScanned.setText(getString(R.string.last_scanned, lastScanned));
                } else if (resultCode==Activity.RESULT_CANCELED) {
                    handlePopBackMethod();
                }
                break;
            case RC_HANDLE_EXIT_FUNC:
                requireActivity().finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_HANDLE_CAMERA_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
            } else {
                pendingExitDialog = true;
            }
        } else {
            Log.d(TAG, "Unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Handles the requesting of the camera permission in Android 6.0 (API 23)
     */
    private void requestCameraPermission() {
        final String[] permissions = new String[] { Manifest.permission.CAMERA };

        // shouldShowRequestPermissionRationale() method identifies whether a user might need an explanation
        // for the requested permission. This method returns true if the app has requested this permission
        // previously and the user denied the request.
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions here to request the missing permissions,
            // and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission.
            //
            // Use requestPermissions(...) instead of ActivityCompat.requestPermissions(...)
            // in support.v4.app.Fragment, otherwise the onRequestPermissionsResult callback
            // is called on the activity and not the fragment.
            //
            // Note: requestPermissions(...) of android.app.Fragment requires min API level 23.

            //ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        // Show an explanation to the user why the permission is needed.
        final Snackbar snackbar = Snackbar.make(cameraView, R.string.permission_camera_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
                        requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(requireActivity(), R.color.colorActionText));
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);

                if (event==DISMISS_EVENT_SWIPE)
                    requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
            }
        });
        final View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorSnackbar));
        final TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorActionText));
        snackbar.show();
    }

    /**
     * Creates and starts the camera.
     */
    private void createCameraSource() {
        Log.d(TAG, "createCameraSource");
        // A barcode detector is created to track barcodes. An associated processor instance is set to
        // track the barcodes and receive the barcode detection results.
        barcodeDetector = new BarcodeDetector.Builder(requireActivity())
                .setBarcodeFormats(Barcode.CODE_128 | Barcode.CODE_39 | Barcode.QR_CODE)
                .build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                // Note: receiveDetections(...) is not run on UI thread.

                // To avoid instant detection after resumed from previous scan.
                if (detections.getFrameMetadata().getTimestampMillis()-prevFrameTimestamp<500) {
                    Log.d(TAG, "Frame timestamp=" + detections.getFrameMetadata().getTimestampMillis());
                    return;
                }

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    prevFrameTimestamp = detections.getFrameMetadata().getTimestampMillis();
                    Log.d(TAG, "Frame timestamp=" + prevFrameTimestamp);
                    Log.d(TAG, "Scanned barcode=" + barcodes.valueAt(0).displayValue);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop();
                        }
                    }).start();

                    if (vibrator.hasVibrator())
                        vibrator.vibrate(
                                new long[]{
                                        0, // start immediately
                                        200,
                                        200,
                                        200
                                },
                                -1 // do not repeat
                        );

                    lastScanned = barcodes.valueAt(0).displayValue;
                    rlResult.post(new Runnable() {
                        @Override
                        public void run() {
                            showScannedResult(lastScanned);
                        }
                    });

                    startCameraSource();
                }
            }

            @Override
            public void release() {

            }
        });

        if (!barcodeDetector.isOperational()) {
            int messageId;

            // Note: The first time that an app using the barcode or face API is installed on a device,
            // GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time. But if that download
            // has not yet completed, then the above call will not detect any barcodes and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available. The detectors will automatically become operational once the library downloads
            // complete on device.
            messageId = R.string.dependencies_unavailable;

            // Check for low storage. If there is low storage, the native library will not be downloaded,
            // so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = requireActivity().registerReceiver(null, lowstorageFilter) != null;
            if (hasLowStorage) {
                messageId = R.string.low_storage;
            }

            // Show a dialog to inform user and close the fragment.
            showDialog(
                    BarcodeCaptureFragment.this,
                    RC_HANDLE_EXIT_FUNC,
                    R.string.app_name,
                    messageId,
                    null,
                    0,
                    0,
                    true
            );
            return;
        }

        // Note: The camera source uses a higher resolution to enable the barcode detector to detect
        // small barcodes at long distances.
        cameraSource = new CameraSource.Builder(requireActivity(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    /**
     * Starts or restarts the camera source, if it exists.
     */
    // @SuppressWarnings({"ResourceType"})
    @SuppressWarnings({"MissingPermission"})
    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireActivity());
        if (code!=ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(requireActivity(), code, RC_HANDLE_GMS);
            if (dialog != null) dialog.show();
        }

        if (cameraSource!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //noinspection ResourceType
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                        releaseCameraSource();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        releaseCameraSource();

                        // Prior to MIUI version 6.4.14, there is an issue with MIUI dynamic permissions management,
                        // which always returned PERMISSION_GRANTED (value=0) even when the requested permission
                        // has been denied by the user.
                        //
                        // Calling an API with required permission been denied cause a RuntimeException.
                        // Handle the exception here by showing user a dialog and exit this function.
                        showDialog(
                                BarcodeCaptureFragment.this, RC_HANDLE_EXIT_FUNC,
                                R.string.app_name,
                                R.string.no_camera_permission,
                                null,
                                0,
                                0,
                                true
                        );
                    }
                }
            }).start();
        }
    }

    private void releaseCameraSource() {
        if (cameraSource!=null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    /**
     * Thie method prepares the result to be passed back to the target fragment.
     * Note: This methond will only be called if the fragment is started as child
     * fragment.
     */
    private void popBackWithResult() {
        int resultCode = Activity.RESULT_CANCELED;
        Intent intent = null;

        if (barcodes.size()>0) {
            intent = new Intent();
            intent.putParcelableArrayListExtra(EXT_BARCODES, barcodes);
            intent.putExtra(EXT_LAST_SCANNED, lastScanned);

            resultCode = Activity.RESULT_OK;
        }

        if (getTargetFragment()!=null)
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void handlePopBackMethod() {
        if (getParentFragment()==null) {
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            popBackWithResult();
        }
    }

    private void showScannedResult(String code) {
        tvLastScanned.setText(getString(R.string.last_scanned, lastScanned));
        tvResult.setText(getString(R.string.code, code));
        if (!rlResult.isShown())
            rlResult.setVisibility(View.VISIBLE);

        /*Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
        r.play();*/

        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        mediaPlayer.start();
    }
}
