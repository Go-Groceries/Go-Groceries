package com.prasunpersonal.gogroceries_customer.Activities;

import static com.prasunpersonal.gogroceries_customer.App.ME;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prasunpersonal.gogroceries_customer.Models.ModelCustomer;
import com.prasunpersonal.gogroceries_customer.databinding.ActivitySignupBinding;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    ActivitySignupBinding binding;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signupToolbar.setNavigationOnClickListener(v -> finish());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.cSignup.setOnClickListener(v -> {
            if (binding.cName.getText().toString().trim().isEmpty()) {
                binding.cName.setError("Name is required!");
                binding.cName.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(binding.cEmail.getText().toString().trim()).matches()) {
                binding.cEmail.setError("Enter a valid Email address!");
                binding.cEmail.requestFocus();
                return;
            }
            if (!Patterns.PHONE.matcher(binding.cPhone.getText().toString().trim()).matches()) {
                binding.cPhone.setError("Enter a valid phone number!");
                binding.cPhone.requestFocus();
                return;
            }
            if (binding.cPass1.getText().toString().trim().length() < 6) {
                binding.cPass1.setError("Enter a valid password of 6 digits!");
                binding.cPass1.requestFocus();
                return;
            }
            if (!binding.cPass2.getText().toString().equals(binding.cPass1.getText().toString())) {
                binding.cPass2.setError("Passwords doesn't match!");
                binding.cPass2.requestFocus();
                return;
            }

            progressDialog.setMessage("Creating Account...");
            progressDialog.show();

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.cEmail.getText().toString().trim(),binding.cPass1.getText().toString().trim()).addOnSuccessListener(authResult -> {
                progressDialog.setMessage("Saving Account Info ...");

                ME = new ModelCustomer(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()), binding.cName.getText().toString().trim(), binding.cEmail.getText().toString().trim(), binding.cPhone.getText().toString().trim());
                FirebaseFirestore.getInstance().collection("Customers").document(ME.getUid()).set(ME).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        startActivity(new Intent(this, MainCustomerActivity.class));
                        progressDialog.dismiss();
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            });
        });
    }
}