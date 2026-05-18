package com.plantris.pastelist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.settings_view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText editEmail = view.findViewById(R.id.editEmail);
        TextInputEditText editPassword = view.findViewById(R.id.editPassword);
        Button buttonLogin = view.findViewById(R.id.buttonLogin);
        Button buttonSignIn = view.findViewById(R.id.buttonSignIn);

        // Sign In creates the user (Registers)
        buttonSignIn.setOnClickListener(v -> registerUser(editEmail, editPassword));

        // Log In authenticates the user
        buttonLogin.setOnClickListener(v -> loginUser(editEmail, editPassword));
    }

    private void loginUser(TextInputEditText editEmail, TextInputEditText editPassword) {
        String email = getTextValue(editEmail);
        String password = getTextValue(editPassword);

        if (email.isEmpty()) {
            editEmail.setError("Email required");
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password required");
            return;
        }

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult ->
                        Toast.makeText(requireContext(), "Logged in successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void registerUser(TextInputEditText editEmail, TextInputEditText editPassword) {
        String email = getTextValue(editEmail);
        String password = getTextValue(editPassword);

        if (email.isEmpty()) {
            editEmail.setError("Email required");
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password required");
            return;
        }

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("settings");

                    userRef.child("email").setValue(email)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Database failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Auth failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String getTextValue(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}