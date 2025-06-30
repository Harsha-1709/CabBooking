package com.example.cabbooking.ui.driver.driver_info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.R;
import com.example.cabbooking.model.User;

public class DriverInfoFragment extends Fragment {

    private DriverInfoViewModel mViewModel;

    private TextView driverName;
    private TextView vehicleTypeTextView;
    private TextView vehiclePlateNumberTextView;
    private TextView driverRating;

    User currentUserObject = null;


    public static DriverInfoFragment newInstance() {
        return new DriverInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_info, container, false);
        linkViewElements(view);

        return view;
    }

    /**
     * Connect view elements for further use
     * @param rootView
     */
    private void linkViewElements(View rootView) {
        driverName = rootView.findViewById(R.id.usernameTextView);
        vehicleTypeTextView = rootView.findViewById(R.id.vehicleTypeTextView);
        vehiclePlateNumberTextView = rootView.findViewById(R.id.vehiclePlateNumberTextView);
        driverRating = rootView.findViewById(R.id.text_rating);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void setDriverInfo() {
        driverName.setText("Username: " + currentUserObject.getUsername());
        vehicleTypeTextView.setText("Vehicle type: " + currentUserObject.getTransportationType());
        vehiclePlateNumberTextView.setText("Plate number: " + currentUserObject.getVehiclePlateNumber());
        driverRating.setText(String.format("%.1f", getRatingAverage(currentUserObject)));
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
        mViewModel =  new ViewModelProvider(requireActivity()).get(DriverInfoViewModel.class);
        mViewModel.getCurrentUserObject().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUserObject = user;
                setDriverInfo();
            }
        });

    }

}