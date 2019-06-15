package d.com.flucca.missedconnection;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import d.com.flucca.missedconnection.Models.User;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {
    DatabaseReference userDatabase;
    ImageView profileImage;
    TextView userName,Name, Twitter,Instagram,Facebook,Email,Tel1,Tel2,Weblink;
    Button save;
    User user;
    ProgressBar progressBar;
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    int REQUEST_IMAGE =1;
    int TAKE_PHOTO =2;
    StorageReference storageReference;
    Uri ImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();
        initializeUI();
        loadProfile();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                if(ImageURI != null){
                    StoreImageOnFirebaseAndSaveChanges();
                }else{
                    saveChanges();
                }
            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addmenu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent();
        if (id == R.id.mybutton) {
            intent = new Intent(UserProfileActivity.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
            intent = new Intent(UserProfileActivity.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(UserProfileActivity.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(UserProfileActivity.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(UserProfileActivity.this, ViewConnectionsActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    private void showOptionsDialog(){
        String[] pictureOptions = {"Tomar Con Cámara", "Seleccionar de Galería"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cargar Imagen Desde:");
        builder.setItems(pictureOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        checkImagePermissions();
                        break;
                    case 1:
                        checkFilePermissions();
                        break;
                }
            }
        });
        builder.show();
    }

    private void checkFilePermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = UserProfileActivity.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += UserProfileActivity.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += UserProfileActivity.this.checkSelfPermission("Manifest.permission.CAMERA");
            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);
            }else{
                pickFromGallery();
            }
        }
    }

    private void checkImagePermissions(){
        int permissionCheck = UserProfileActivity.this.checkSelfPermission("Manifest.permission.CAMERA");
        if(permissionCheck != 0){
            this.requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_IMAGE);
        }else{
            takePhoto();
        }
    }


    private void takePhoto(){
        Intent openCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if(openCamera.resolveActivity(getPackageManager())!= null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                this.ImageURI = FileProvider.getUriForFile(this,
                        "d.com.flucca.missedconnection.fileprovider",
                        photoFile);
                openCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(openCamera, TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void pickFromGallery(){
        Intent openGallery = new Intent(Intent.ACTION_PICK);
        openGallery.setType("image/*");
        startActivityForResult(openGallery, REQUEST_IMAGE);
    }

    private void StoreImageOnFirebaseAndSaveChanges(){
        final StorageReference storageRef = storageReference.child("images/"+ UUID.randomUUID().toString() + ".jpg");
        storageRef.putFile(ImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageURI = uri;
                        saveChanges();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_IMAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePhoto();
            }
        }
        else if(requestCode == REQUEST_STORAGE_PERMISSION){
            if((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                pickFromGallery();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == TAKE_PHOTO){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                this.ImageURI = getImageUri(getApplicationContext(), photo);
            }
            else if(requestCode == REQUEST_IMAGE){
                this.ImageURI = data.getData();
            }
            Bitmap bitmap = null;
            try{
                final InputStream imageStream = getContentResolver().openInputStream(ImageURI);
                bitmap = BitmapFactory.decodeStream(imageStream);
                profileImage.setImageBitmap(bitmap);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private Uri getImageUri(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo,"title" , null);
        return Uri.parse(path);
    }


    private void saveChanges(){
        user.setTwitterName(Twitter.getText().toString());
        user.setFacebookName(Facebook.getText().toString());
        user.setInstagram(Instagram.getText().toString());
        user.setWebLink(Weblink.getText().toString());
        user.setContactPhone2(Tel2.getText().toString());
        user.setContactPhone1(Tel1.getText().toString());
        user.setContactMail(Email.getText().toString());
        user.setRealName(Name.getText().toString());
        user.setDisplayName(userName.getText().toString());
        if(ImageURI != null){
            user.setDisplayImageURL(ImageURI.toString());
        }
        userDatabase.child(user.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(getApplicationContext(), "Usuario Guardado Exitosamente!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error al Cargar usuario, Favor intentar nuevamente!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void initializeUI(){
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.txtUserName);
        Name = findViewById(R.id.txtNombre);
        Twitter = findViewById(R.id.txtFechaConexion);
        Instagram = findViewById(R.id.txtIG);
        Facebook = findViewById(R.id.txtFb);
        Email = findViewById(R.id.txtEmail);
        Tel1 = findViewById(R.id.txtTel1);
        Tel2 = findViewById(R.id.txtTel2);
        Weblink = findViewById(R.id.txtWebLink);
        save = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void loadProfile(){
        Query query = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    if(!user.getDisplayImageURL().isEmpty()){

                        Glide.with(getBaseContext()).load(user.getDisplayImageURL()).into(profileImage);
                    }else{
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.noimage));
                    }
                    userName.setText(user.getDisplayName());
                    Name.setText(user.getRealName());
                    Twitter.setText(user.getTwitterName());
                    Instagram.setText(user.getInstagram());
                    Facebook.setText(user.getFacebookName());
                    Email.setText(user.getContactMail());
                    Tel1.setText(user.getContactPhone2());
                    Tel2.setText(user.getContactPhone1());
                    Weblink.setText(user.getWebLink());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }



}
