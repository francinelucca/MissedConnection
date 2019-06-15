package d.com.flucca.missedconnection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.User;

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

public class CreatePost extends AppCompatActivity implements LocationListener{

    DatabaseReference postDatabase;
    ImageView imageView;
    int REQUEST_IMAGE =1;
    int TAKE_PHOTO =2;
    StorageReference storageReference;
    FirebaseAuth auth;
    Uri ImageURI;
    EditText Titulo,Descripcion;
    Button upload;
    ProgressBar progressBar;
    DatabaseReference userDatabase;
    LocationManager locationManager;
    Location currentLocation;
    private static final int REQUEST_LOCATION_PERMISSION = 1000;
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final String TAG = CreatePost.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initializeUI();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!hasPermission()){
            this.requestForPermission();
        }
        else{
            getCurrentLocation(null);
            updateLocation(null);
        }
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                if(ImageURI != null){
                    StoreImageOnFirebaseAndUpload();
                }else{
                    AddPost("");
                }
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
            intent = new Intent(CreatePost.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
            intent = new Intent(CreatePost.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(CreatePost.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(CreatePost.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(CreatePost.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(CreatePost.this, ViewConnectionsActivity.class);
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
            int permissionCheck = CreatePost.this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += CreatePost.this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCheck += CreatePost.this.checkSelfPermission("Manifest.permission.CAMERA");
            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);
            }else{
                pickFromGallery();
            }
        }
    }

    private void checkImagePermissions(){
            int permissionCheck = CreatePost.this.checkSelfPermission("Manifest.permission.CAMERA");
            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_IMAGE);
            }else{
                takePhoto();
            }
    }

    private void AddPost(String ImageUri){
        final String postId = postDatabase.push().getKey();
        final Post post;
        if(currentLocation != null){
             post = new Post(postId,Titulo.getText().toString(),ImageUri,Descripcion.getText().toString(),new Date(),"RunningFox", FirebaseAuth.getInstance().getUid(),currentLocation.getLongitude(),currentLocation.getLatitude());

        }else{
             post = new Post(postId,Titulo.getText().toString(),ImageUri,Descripcion.getText().toString(),new Date(),"RunningFox", FirebaseAuth.getInstance().getUid(),-1.0,-1.0);
        }
        Query query = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post.setAuthor(dataSnapshot.getChildren().iterator().next().getValue(User.class).getDisplayName());
                postDatabase.child(postId).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getApplicationContext(), "Post Creado Exitosamente!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(CreatePost.this, HomePage.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al Cargar Post, Favor intentar nuevamente!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void initializeUI(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        imageView = findViewById(R.id.ImagenPost);
        Titulo = findViewById(R.id.txtTituloPost);
        Titulo.setText("");
        Descripcion = findViewById(R.id.txtDescripcionPost);
        Descripcion.setText("");
        upload = findViewById(R.id.btnCargarPost);
        progressBar = findViewById(R.id.progressBarCreatePost);
        ImageURI = null;

        imageView.setImageDrawable(getResources().getDrawable(R.drawable.noimage));
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

    private void StoreImageOnFirebaseAndUpload(){
        final StorageReference storageRef = storageReference.child("images/"+ UUID.randomUUID().toString() + ".jpg");
        storageRef.putFile(ImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        AddPost(uri.toString());
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
                    imageView.setImageBitmap(bitmap);
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



//LOCATION

    //solicitar permisos para usar la ubicacion
    private void requestForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    //verificar si tengo permisos
    private boolean hasPermission() {
        return ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocation(View view){
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
    //actualizar loction
    @SuppressLint("MissingPermission")
    public void updateLocation(View view){
        long minTime      = 1000;
        float minDistance = 0.f;
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                this
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_LOCATION_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(null);
            }
        }
        else if(requestCode == REQUEST_IMAGE){
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

    //BEGIN METHODS LISTENER LOCATION
    //================================================================================
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.wtf(TAG, provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.wtf(TAG, provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.wtf(TAG, provider);

    }

}

