package com.tanmay.Train_ticket_Reservation_System;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 1. Load User Data from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String savedName = prefs.getString("user_name", "Unknown User");
        String savedEmail = prefs.getString("user_email", "No Email Found");

        // 2. Set the UI
        tvProfileName.setText(savedName);
        tvProfileEmail.setText(savedEmail);

        // 3. Handle Logout Click
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        // Clear saved token and data
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Sign out of Google to prevent auto-login loop
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        googleSignInClient.signOut().addOnCompleteListener(task -> {

            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();

            // Redirect back to Login Screen
            Intent intent = new Intent(getActivity(), MainActivity.class);
            // Clear the activity stack so the user can't press the "Back" button to return to the Dashboard
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}