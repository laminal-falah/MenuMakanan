package com.laminalfalah.menu.makanan.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.laminalfalah.menu.makanan.BuildConfig;
import com.laminalfalah.menu.makanan.R;
import com.laminalfalah.menu.makanan.model.Makanan;
import com.laminalfalah.menu.makanan.utils.FileCompressor;
import com.laminalfalah.menu.makanan.utils.ProgressBarUtils;
import com.laminalfalah.menu.makanan.utils.SnackBarUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.laminalfalah.menu.makanan.App.CHANNEL_ID_UPLOAD;
import static com.laminalfalah.menu.makanan.utils.Config.DIRECTORY;

public class AddEditMakananActivity extends AppCompatActivity implements EventListener<DocumentSnapshot> {

    private static final String TAG = AddEditMakananActivity.class.getSimpleName();

    public static final String ADD_DATA = "add_data";

    public static final String ID = "id";

    public static final int REQUEST_ADD = 1244;
    public static final int RESULT_ADD_SUCCESS = 1245;
    public static final int RESULT_ADD_FAILED = 1246;

    public static final int REQUEST_UPDATE = 4421;
    public static final int RESULT_UPDATE_SUCCESS = 5421;
    public static final int RESULT_UPDATE_FAILED = 6421;

    private static final int REQUEST_TAKE_PHOTO = 768;
    private static final int REQUEST_GALLERY_PHOTO = 879;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.imageMakanan) AppCompatImageView mImageMakanan;
    @BindView(R.id.tlTitleMakanan) TextInputLayout tlTitleMakanan;
    @BindView(R.id.txtTitleMakanan) TextInputEditText txtTitleMakanan;
    @BindView(R.id.tlHargaMakanan) TextInputLayout tlHargaMakanan;
    @BindView(R.id.txtHargaMakanan) TextInputEditText txtHargaMakanan;
    @BindView(R.id.rateMakanan) AppCompatRatingBar mRateMakanan;
    @BindView(R.id.tlDeskripsiMakanan) TextInputLayout tlDeskripsiMakanan;
    @BindView(R.id.txtDeskripsiMakanan) TextInputEditText txtDeskripsiMakanan;

    private FirebaseFirestore mFirestore;
    private ListenerRegistration mRegistration;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference, mStorageMakanan;

    private ProgressBarUtils mProgressBarUtils;
    private SnackBarUtils mSnackBarUtils;

    private Intent mIntent;
    private Uri mFilePath;
    private Drawable mDrawable;
    private BitmapDrawable mBitmapDrawable;

    private File mPhotoFile;
    private FileCompressor mFileCompressor;

    private NotificationCompat.Builder mNotificationCompat;
    private NotificationManagerCompat mNotificationManagerCompat;

    private String id;
    private boolean isEdit;

    private String namaMakanan, fotoMakanan, deskripsiMakanan, hargaMakanan;
    private double ratingMakanan;
    private String deleteFotoMakanan;

    private HashMap<String, Object> makanans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_makanan);
        ButterKnife.bind(this);

        onAuthFirebase();

        onInitFirebase();

        mProgressBarUtils = new ProgressBarUtils(this);
        mSnackBarUtils = new SnackBarUtils(this);
        mFileCompressor = new FileCompressor(this);
        mIntent = new Intent();

        mNotificationCompat = new NotificationCompat.Builder(this, CHANNEL_ID_UPLOAD);
        mNotificationManagerCompat = NotificationManagerCompat.from(this);

        id = getIntent().getExtras().getString(ID);

        if (id == null) {
            throw new IllegalArgumentException("must be pass id " + id);
        }

        if (getIntent().getExtras().getString(ID).equals(ADD_DATA)) {
            isEdit = false;
            mToolbar.setTitle(getString(R.string.toolbar_makanan, getString(R.string.add_title)));
        } else {
            isEdit = true;
            mToolbar.setTitle(getString(R.string.toolbar_makanan, getString(R.string.edit_title)));
            id = getIntent().getExtras().getString(ID);
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        Glide.with(this).load(R.drawable.background_makanan).fitCenter().into(mImageMakanan);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isEdit) {
            mRegistration = mFirestore.collection(Makanan.COLLECTION).document(id).addSnapshotListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mFileCompressor.compressToFile(mPhotoFile);
                    Glide.with(this)
                            .load(mPhotoFile)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.background_makanan))
                            .into(mImageMakanan);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                mFilePath = data.getData();
                try {
                    mPhotoFile = mFileCompressor.compressToFile(new File(getRealPathFromUri(selectedImage)));
                    Glide.with(this)
                            .load(mPhotoFile)
                            .apply(new RequestOptions().centerCrop().placeholder(R.drawable.background_makanan))
                            .into(mImageMakanan);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            Log.e(TAG, "onEvent: ", e);
        }

        Makanan makanan = snapshot.toObject(Makanan.class);
        setDataMakanan(makanan);
    }

    private void onAuthFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }

    private void onInitFirebase() {
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
    }

    private void dialogSelectImage() {
        final CharSequence[] items = {
                getString(R.string.take_photo),
                getString(R.string.gallery_photo),
                getString(R.string.cancel_photo)
        };

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(getString(R.string.choose_image));
        mBuilder.setItems(items, (dialog, item) -> {
            if (items[item].equals(getString(R.string.take_photo))) {
                dispatchTakePictureIntent();
            } else if (items[item].equals(getString(R.string.gallery_photo))) {
                dispatchGalleryPictureIntent();
            } else {
                dialog.dismiss();
            }
        });
        mBuilder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                mFilePath = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                mPhotoFile = photoFile;
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFilePath);
                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchGalleryPictureIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(galleryIntent, REQUEST_GALLERY_PHOTO);
    }

    private File createImageFile() throws IOException {
        String timestamps = new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault()).format(new Date());
        String mFileName = "IMG_" + timestamps;
        File mStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(mFileName, ".jpg", mStorageDir);
    }

    private String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean validate() {
        boolean valid = true;

        mDrawable = mImageMakanan.getDrawable();
        mBitmapDrawable = mDrawable instanceof BitmapDrawable ? (BitmapDrawable) mDrawable : null;

        namaMakanan = tlTitleMakanan.getEditText().getText().toString();
        hargaMakanan = tlHargaMakanan.getEditText().getText().toString();
        ratingMakanan = (double) mRateMakanan.getRating();
        deskripsiMakanan = tlDeskripsiMakanan.getEditText().getText().toString();

        if (mBitmapDrawable == null || mBitmapDrawable.getBitmap() == null) {
            valid = false;
            fotoMakanan = null;
            mSnackBarUtils.snackBarLong(getString(R.string.error_makanan, getString(R.string.hint_foto_makanan)));
        }

        if (TextUtils.isEmpty(namaMakanan)) {
            valid = false;
            tlTitleMakanan.setErrorEnabled(true);
            tlTitleMakanan.setError(getString(R.string.error_makanan, getString(R.string.hint_nama_makanan)));
            tlTitleMakanan.requestFocus();
        } else {
            tlTitleMakanan.setError(null);
            tlTitleMakanan.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(deskripsiMakanan)) {
            valid = false;
            tlDeskripsiMakanan.setErrorEnabled(true);
            tlDeskripsiMakanan.setError(getString(R.string.error_makanan, getString(R.string.hint_deskripsi_makanan)));
            tlDeskripsiMakanan.requestFocus();
        } else {
            tlDeskripsiMakanan.setError(null);
            tlDeskripsiMakanan.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(String.valueOf(hargaMakanan))) {
            valid = false;
            tlHargaMakanan.setErrorEnabled(true);
            tlHargaMakanan.setError(getString(R.string.error_makanan, getString(R.string.hint_harga_makanan)));
            tlHargaMakanan.requestFocus();
        } else {
            tlHargaMakanan.setError(null);
            tlHargaMakanan.setErrorEnabled(false);
        }

        if (mRateMakanan.getRating() == 0) {
            valid = false;
            mSnackBarUtils.snackBarLong(getString(R.string.error_makanan, getString(R.string.hint_rating_makanan)));
        }

        return valid;
    }

    private void setDataMakanan(Makanan makanan) {
        setDeleteFotoMakanan(makanan.getFotoMakanan());

        if (mFilePath == null) {
            mStorageReference
                    .child(DIRECTORY + makanan.getFotoMakanan())
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(this)
                            .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.background_makanan))
                            .load(uri.toString())
                            .centerCrop()
                            .into(mImageMakanan))
                    .addOnFailureListener(e -> Glide.with(this)
                            .load(R.drawable.background_makanan)
                            .centerCrop()
                            .into(mImageMakanan));
        }

        txtTitleMakanan.setText(makanan.getNamaMakanan());
        txtHargaMakanan.setText(String.valueOf((int) makanan.getHargaMakanan()));
        mRateMakanan.setRating((float) makanan.getRatingMakanan());
        txtDeskripsiMakanan.setText(makanan.getDeskripsiMakanan());
    }

    private String getDeleteFotoMakanan() {
        return deleteFotoMakanan;
    }

    private void setDeleteFotoMakanan(String deleteFotoMakanan) {
        this.deleteFotoMakanan = deleteFotoMakanan;
    }

    @OnClick(R.id.imageMakanan) void choosePhoto() {
        dialogSelectImage();
    }

    @OnClick(R.id.addEditMakanan) void submit() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (!validate()) return;

        mProgressBarUtils.show();

        makanans = new HashMap<>();

        fotoMakanan = UUID.randomUUID().toString() + ".jpg";

        if (mFilePath != null) {
            if (isEdit) {
                mStorageMakanan = mStorageReference.child(DIRECTORY + getDeleteFotoMakanan());
                mStorageMakanan.delete();
            }

            mNotificationCompat.setSmallIcon(R.drawable.ic_file_upload_gray_24dp)
                    .setContentTitle("Upload")
                    .setContentText("Upload in progress")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setChannelId(CHANNEL_ID_UPLOAD)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setProgress(0, 0, false);
            mNotificationManagerCompat.notify(2, mNotificationCompat.build());

            mStorageMakanan = mStorageReference.child(DIRECTORY + fotoMakanan);
            mStorageMakanan.putFile(mFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        mNotificationCompat.setContentText("Upload Finished")
                                .setOngoing(false)
                                .setProgress(0, 0, false);
                        mNotificationManagerCompat.notify(2, mNotificationCompat.build());
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        mNotificationCompat.setProgress(100, (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()), false);
                        mNotificationManagerCompat.notify(2, mNotificationCompat.build());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "submit: ", e);
                        mNotificationCompat.setContentText("Upload Failed")
                                .setOngoing(false)
                                .setProgress(0, 0, false);
                        mNotificationManagerCompat.notify(2, mNotificationCompat.build());
                        mSnackBarUtils.snackBarLong(e.getMessage());
                    });

            makanans.put(Makanan.FIELD_IMAGE_MAKANAN, fotoMakanan);
        }

        makanans.put(Makanan.FIELD_NAMA_MAKANAN, namaMakanan);
        makanans.put(Makanan.FIELD_HARGA_MAKANAN, Double.parseDouble(hargaMakanan));
        makanans.put(Makanan.FIELD_RATING_MAKANAN, ratingMakanan);
        makanans.put(Makanan.FIELD_DESKRIPSI_MAKANAN, deskripsiMakanan);

        new Handler().postDelayed(() -> {
            if (!isEdit) {
                makanans.put(Makanan.TIMESTAMPS, Timestamp.now());
                mFirestore.collection(Makanan.COLLECTION).add(makanans)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                setResult(RESULT_ADD_SUCCESS);
                                finish();
                            } else {
                                mIntent.putExtra("error", "Gagal Menyimpan !");
                                setResult(RESULT_ADD_FAILED, mIntent);
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "submit: ", e);
                            mSnackBarUtils.snackBarLong(e.getMessage());
                        });
            } else {
                mFirestore.collection(Makanan.COLLECTION).document(id).update(makanans)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                setResult(RESULT_UPDATE_SUCCESS);
                                finish();
                            } else {
                                mIntent.putExtra("error", "Gagal Mengubah !");
                                setResult(RESULT_UPDATE_FAILED, mIntent);
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "submit: ", e);
                            mSnackBarUtils.snackBarLong(e.getMessage());
                        });
            }

            mProgressBarUtils.hide();
        }, 2000);
    }
}
