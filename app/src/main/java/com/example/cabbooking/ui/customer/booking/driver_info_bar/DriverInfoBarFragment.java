package com.example.cabbooking.ui.customer.booking.driver_info_bar;

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
import androidx.fragment.app.Fragment;
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

public class DriverInfoBarFragment extends Fragment {

    private DriverInfoBarViewModel mViewModel;

    private TextView driverUsernameTextView;
    private TextView plateNumberTextView;
    private TextView transportationTypeTextView;
    private RatingBar ratingBar;
    private ImageView profileImage;

    //Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference mStorageRef;

    private User driver;

    public static DriverInfoBarFragment newInstance() {
        return new DriverInfoBarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_info_bar, container, false);
        linkViewElement(view);
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
    private void linkViewElement(View rootView) {
        driverUsernameTextView = rootView.findViewById(R.id.driverUsernameTextView);
        plateNumberTextView = rootView.findViewById(R.id.plateNumberTextView);
        transportationTypeTextView = rootView.findViewById(R.id.transportationTypeTextView);
        ratingBar = rootView.findViewById(R.id.score_rating_bar);
        profileImage = rootView.findViewById(R.id.profile_avatar);
    }

    /**
     * Render driver information
     * @param driver
     */
    private void setDriverInfo(User driver) {
        driverUsernameTextView.setText(driver.getUsername());
        plateNumberTextView.setText(driver.getVehiclePlateNumber());
        transportationTypeTextView.setText(driver.getTransportationType());
        ratingBar.setRating(getRatingAverage(driver));
        setProfileImage();
    }

    /**
     * Get driver profile image
     */
    private void setProfileImage() {
        // Retrieve driver information
        db.collection(Constants.FSUser.userCollection)
                .whereEqualTo(Constants.FSUser.emailField, driver.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            User driver = doc.toObject(User.class);

//                            assert driver != null;
                            // Get image URI
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

    /**
     * Get driver avg rating
     * @param driver
     * @return avgRating
     */
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
        mViewModel = new ViewModelProvider(requireActivity()).get(DriverInfoBarViewModel.class);
        mViewModel.getDriver().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                driver = user;
                setDriverInfo(user);
            }
        });
    }

}