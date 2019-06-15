package d.com.flucca.missedconnection;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import d.com.flucca.missedconnection.Models.User;

public class LogInActivity extends AppCompatActivity {
    private EditText emailTV, passwordTV;
    private Button loginBtn;
    private TextView register;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    DatabaseReference userDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        initializeUI();
        userDatabase = FirebaseDatabase.getInstance().getReference("users");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUsuario();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, Register.class);
                startActivity(intent);
            }
        });

    }

    private void loginUsuario() {
        progressBar.setVisibility(View.VISIBLE);

        String email, password;
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Favor Ingresar Email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Favor Ingresar Password...", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Query query = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long childrenCount = dataSnapshot.getChildrenCount();
                                    if(childrenCount == 0){
                                        crearUsuarioDefecto();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.wtf(this.getClass().toString(), databaseError.getMessage());
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Inicio de Sesión Exitoso!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(LogInActivity.this, HomePage.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Inicio de Sesión Fallido! Favor Intentar Nuevamente", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void crearUsuarioDefecto(){
        String userId = userDatabase.push().getKey();
        User user = new User(userId,FirebaseAuth.getInstance().getUid(),"AnonymousPanda","","","","","","","","","");
        userDatabase.child(userId).setValue(user);
    }

    private void initializeUI() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        emailTV = findViewById(R.id.txtEmail);
        passwordTV = findViewById(R.id.txtPassword);

        loginBtn = findViewById(R.id.btnLogIn);
        progressBar = findViewById(R.id.progressBar2);
        register = findViewById(R.id.tvRegistro);


        String udata="Registrarse";
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        register.setText(content);
    }
}
