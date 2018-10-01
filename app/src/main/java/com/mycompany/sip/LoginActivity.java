package com.mycompany.sip;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth fbA;
    private FirebaseHandler fbh = FirebaseHandler.getInstance();
    private final int RC_SIGN_IN = 333;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fbA = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        //check if user is logged in
        final FirebaseUser currentUser = fbA.getCurrentUser();

        if(currentUser != null)
        {
            FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            Uri deepLink = null;
                            HashMap<String, String> role = new HashMap<>();
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.getLink();
                                Object[] keys = deepLink.getQueryParameterNames().toArray();
                                role.put(keys[0].toString(), deepLink.getQueryParameter(keys[0].toString()));
                                //fbh.addRole(currentUser.getUid(), role);
                            }
                        }
                    });

            //go to sites activity
            Intent in = new Intent(LoginActivity.this, AllSitesActivity.class);
            startActivity(in);
        }
        else
        {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());

// Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    public void createAccount(View view)
    {
        EditText email = findViewById(R.id.emailLogin);
        EditText password = findViewById(R.id.passwordLogin);
        fbA.createUserWithEmailAndPassword(email.toString(), password.toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = fbA.getCurrentUser();
                            //go to sites activity
                            Intent in = new Intent(LoginActivity.this, AllSitesActivity.class);
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Account creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signIn(View view)
    {
        EditText email = view.findViewById(R.id.emailLogin);
        EditText password = view.findViewById(R.id.passwordLogin);
        fbA.signInWithEmailAndPassword(email.toString(), password.toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = fbA.getCurrentUser();
                            //go to sites activity
                            Intent in = new Intent(LoginActivity.this, AllSitesActivity.class);
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println(task.getResult());
                            Toast.makeText(LoginActivity.this, "Login failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent in = new Intent(LoginActivity.this, AllSitesActivity.class);
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                System.out.println(response.getError().getErrorCode());
            }
        }
    }
}
