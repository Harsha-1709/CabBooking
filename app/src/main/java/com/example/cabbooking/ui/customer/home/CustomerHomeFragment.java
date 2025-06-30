package com.example.cabbooking.ui.customer.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.cabbooking.Constants;
import com.example.cabbooking.R;
import com.example.cabbooking.model.User;
import com.example.cabbooking.ui.customer.booking.BookingViewModel;
import com.example.cabbooking.ui.customer.booking.checkout.CheckoutViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerHomeFragment extends Fragment {

    private CustomerHomeViewModel customerHomeViewModel;

    private ImageButton bikeBtn;
    private ImageButton carBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private User currentUserObject;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        linkViewElements(view); //Link view elements to class properties
        setActionHandlers();


        return view;
    }

    /**
     * link view elements
     * @param rootView
     */
    private void linkViewElements(View rootView){
        bikeBtn = rootView.findViewById(R.id.bike_image_button);
        carBtn = rootView.findViewById(R.id.car_image_button);
    }

    private void setActionHandlers(){
        setBikeBtnActionHandler();
        setCarBtnActionHandler();
    }

    /**
     * Event listener for Bike btn
     */
    private void setBikeBtnActionHandler(){
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pass data of chosen transportation type
                CheckoutViewModel checkoutViewModel = new ViewModelProvider(requireActivity()).get(CheckoutViewModel.class);
                checkoutViewModel.setTransportationType(Constants.Transportation.Type.bikeType);

                BookingViewModel bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
                bookingViewModel.setTransportationType(Constants.Transportation.Type.bikeType);

                //Move to Drop off page
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.nav_customer_booking);
            }
        });
    }

    /**
     * Event listener for Car btn
     */
    private void setCarBtnActionHandler(){
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pass data of chosen transportation type
                CheckoutViewModel checkoutViewModel = new ViewModelProvider(requireActivity()).get(CheckoutViewModel.class);
                checkoutViewModel.setTransportationType(Constants.Transportation.Type.carType);

                BookingViewModel bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
                bookingViewModel.setTransportationType(Constants.Transportation.Type.carType);

                //Move to Drop off page
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.nav_customer_booking);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customerHomeViewModel = new ViewModelProvider(this).get(CustomerHomeViewModel.class);
        customerHomeViewModel.getCurrentUserObject().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUserObject = user;
            }
        });
    }
}