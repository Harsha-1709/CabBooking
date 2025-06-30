package com.example.cabbooking.ui.driver.alert;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.R;
import com.example.cabbooking.model.Booking;
import com.example.cabbooking.ui.driver.home.DriverHomeViewModel;

import java.util.Objects;

public class DriverAlertFragment extends DialogFragment {
    public static String TAG = "DriverAlertDialog";

    private TextView priceText;
    private TextView distanceText;
    private TextView pickUpLocationText;
    private TextView dropOffLocationText;

    private Button declineBtn;
    private Button acceptBtn;

    public static DriverAlertFragment newInstance() {
        return new DriverAlertFragment();
    }

    /**
     * Link view elements
     * @param rootView
     */
    private void linkViewElements(View rootView) {
        priceText = rootView.findViewById(R.id.priceTextView);
        distanceText = rootView.findViewById(R.id.text_distance);
        pickUpLocationText = rootView.findViewById(R.id.text_pickUpLocation);
        dropOffLocationText = rootView.findViewById(R.id.text_dropLocation);
        declineBtn = rootView.findViewById(R.id.btn_decline);
        acceptBtn = rootView.findViewById(R.id.btn_accept);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_DeviceDefault_Dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_driver_alert, container, false);
        linkViewElements(root);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DriverHomeViewModel driverHomeViewModel = new ViewModelProvider(requireActivity()).get(DriverHomeViewModel.class);


        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverHomeViewModel.setAcceptBookingBtnPressed(false);
                dismiss();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverHomeViewModel.setAcceptBookingBtnPressed(true);
                dismiss();
            }
        });
    }

    /**
     * Set booking detail on layout file
     * @param booking
     */
    private void setBookingDetails(Booking booking){
        priceText.setText(booking.getPriceInVND());
        distanceText.setText(booking.getDistanceInKm());
        pickUpLocationText.setText(booking.getPickupPlaceAddress());
        dropOffLocationText.setText(booking.getDropOffPlaceAddress());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DriverAlertViewModel driverAlertViewModel = new ViewModelProvider(requireActivity()).get(DriverAlertViewModel.class);
        driverAlertViewModel.getBooking().observe(getViewLifecycleOwner(), new Observer<Booking>() {
            @Override
            public void onChanged(Booking booking) {
                setBookingDetails(booking);
            }
        });

    }

}
