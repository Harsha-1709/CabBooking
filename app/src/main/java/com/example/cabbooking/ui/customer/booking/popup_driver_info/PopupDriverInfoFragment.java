package com.example.cabbooking.ui.customer.booking.popup_driver_info;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.Constants;
import com.example.cabbooking.R;
import com.example.cabbooking.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class PopupDriverInfoFragment extends DialogFragment {

    private PopupDriverInfoViewModel mViewModel;

    private TextView driverUsernameTextView;
    private TextView plateNumberAndBike;
    private RatingBar ratingBar;
    private ImageView profileImage;

    //Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference mStorageRef;

    private User driver;

    public static PopupDriverInfoFragment newInstance() {
        return new PopupDriverInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup_driver_info, container, false);
        linkViewElements(view);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        return view;
    }

    /**
     * Link view elements from xml file
     * @param rootView
     */
    private void linkViewElements(View rootView){
        driverUsernameTextView = rootView.findViewById(R.id.driverUsernameTextView);
        plateNumberAndBike = rootView.findViewById(R.id.plateNumberAndBike);
        ratingBar = rootView.findViewById(R.id.ratingBar);
        profileImage = rootView.findViewById(R.id.profile_avatar);
    }

    /**
     * Render driver information
     */
    @SuppressLint("SetTextI18n")
    private void setDriverInfo(){
        driverUsernameTextView.setText(driver.getUsername());
        plateNumberAndBike.setText(driver.getVehiclePlateNumber() + " ● " + driver.getTransportationType());
        ratingBar.setRating(getRatingAverage(driver));
        setProfileImage();
    }

    private void setProfileImage(){
        db.collection(Constants.FSUser.userCollection)
                .whereEqualTo(Constants.FSUser.emailField, driver.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            User driver = doc.toObject(User.class);

//                            assert driver != null;
                            StorageReference fref = mStorageRef.child("profileImages").child(driver.getDocId() + ".jpeg");

                            fref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(profileImage);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                }
                            });
                        }
                    }
                });
    }

    public float getRatingAverage(User driver) {
        double total = 0;
        for (int _rating : driver.getRating()) {
            total += _rating;
        }
        return (float) (total / driver.getRating().size());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(PopupDriverInfoViewModel.class);
        mViewModel.getDriver().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                driver = user;
                setDriverInfo();
            }
        });

    }

}