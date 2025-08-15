package com.example.cabbooking.ui.customer.booking.pickup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.R;
import com.example.cabbooking.ui.customer.booking.BookingViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PickupFragment extends Fragment {

    private PickupViewModel mViewModel;
    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnUseCurrentLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pickup, container, false);

        btnUseCurrentLocation = view.findViewById(R.id.btn_use_current_location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        initGooglePlacesAutocomplete();
        setActionHandlers();
        setupCurrentLocationButton();

        return view;
    }

    public void setActionHandlers(){
        setPlaceSelectedActionHandler();
    }

    private void initGooglePlacesAutocomplete() {
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), apiKey);
        }
        this.placesClient = Places.createClient(requireActivity().getApplicationContext());

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.maps_place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(
                Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                ));
    }

    private void setPlaceSelectedActionHandler() {
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                BookingViewModel bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
                bookingViewModel.setCustomerSelectedPickupPlace(place);
            }

            @Override
            public void onError(@NotNull Status status) {
                Toast.makeText(getActivity().getApplicationContext(), "Error: " + status, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCurrentLocationButton() {
        btnUseCurrentLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                Place currentPlace = Place.builder().setLatLng(currentLatLng).setName("Current Location").build();
                                BookingViewModel bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
                                bookingViewModel.setCustomerSelectedPickupPlace(currentPlace);
                                Toast.makeText(requireContext(), "Current location selected", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autocompleteFragment != null) {
            autocompleteFragment.setOnPlaceSelectedListener(null);
        }
    }
}
