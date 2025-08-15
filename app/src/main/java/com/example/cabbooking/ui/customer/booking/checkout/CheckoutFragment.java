package com.example.cabbooking.ui.customer.booking.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.Constants;
import com.example.cabbooking.R;
import com.example.cabbooking.model.User;
import com.example.cabbooking.ui.customer.booking.BookingViewModel;

public class CheckoutFragment extends Fragment {

    private CheckoutViewModel mViewModel;
    private String transportationType;
    private String distanceInKmString;
    private String priceInVNDString;


    //View variables
    private CardView carCardView;
    private CardView bikeCardView;
    private TextView distanceCarTextView;
    private TextView distanceBikeTextView;
    private TextView priceCarTextView;
    private TextView priceBikeTextView;
    private Button bookBtn;

    private User currentUserObject;

    public static CheckoutFragment newInstance() {
        return new CheckoutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_customer_checkout, container, false);
        linkViewElements(view);
        setActionHandlers();
        return view;
    }

    private void setActionHandlers(){
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookingViewModel bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
                bookingViewModel.setBookBtnPressed(true);
            }
        });
    }

    /**
     * Link view elements from xml file
     * @param rootView
     */
    private void linkViewElements(View rootView){
        carCardView = rootView.findViewById(R.id.carCardView);
        bikeCardView = rootView.findViewById(R.id.bikeCardView);
        distanceCarTextView = rootView.findViewById(R.id.distanceCarTextView);
        distanceBikeTextView = rootView.findViewById(R.id.distanceBikeTextView);
        priceCarTextView = rootView.findViewById(R.id.priceCarTextView);
        bookBtn = rootView.findViewById(R.id.bookBtn);
    }

    /**
     * Display card based on vehicle types
     */
    private void hideAccordingCardView(){
        if (transportationType.equals(Constants.Transportation.Type.carType)) {
            carCardView.setVisibility(View.VISIBLE);
            bikeCardView.setVisibility(View.GONE);
        } else {
            carCardView.setVisibility(View.GONE);
            bikeCardView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Display booking information
     */
    private void setCheckoutInfo() {
        if (distanceInKmString == null || transportationType == null) return;

        if (transportationType.equals(Constants.Transportation.Type.carType)) {
            if (distanceInKmString != null && distanceCarTextView != null) distanceCarTextView.setText(distanceInKmString);
            if (priceInVNDString != null && priceCarTextView != null) {
                priceCarTextView.setVisibility(View.VISIBLE);
                priceCarTextView.setText(priceInVNDString);
            }
            if (priceBikeTextView != null) priceBikeTextView.setVisibility(View.GONE);
            if (distanceBikeTextView != null) distanceBikeTextView.setVisibility(View.GONE);
        } else {
            if (distanceInKmString != null && distanceBikeTextView != null) {
                distanceBikeTextView.setVisibility(View.VISIBLE);
                distanceBikeTextView.setText(distanceInKmString);
            }
            if (priceBikeTextView != null) priceBikeTextView.setVisibility(View.GONE);
            if (priceCarTextView != null) priceCarTextView.setVisibility(View.GONE);
            if (distanceCarTextView != null) distanceCarTextView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.setDistanceInKmString(null);
        mViewModel.setPriceInVNDString(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CheckoutViewModel.class);

        //Get customer currently chosen transportation type
        mViewModel.getTransportationType().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s == null) return;
                transportationType = s;
                hideAccordingCardView();
            }
        });

        mViewModel.getDistanceInKmString().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s == null) return;
                distanceInKmString = s;
            }
        });

        mViewModel.getPriceInVNDString().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s == null) return;
                priceInVNDString = s;
                setCheckoutInfo();
            }
        });

    }
}