package com.example.cabbooking.ui.driver.process_booking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.cabbooking.Constants;
import com.example.cabbooking.R;
import com.example.cabbooking.model.Booking;
import com.example.cabbooking.model.DriverLocation;
import com.example.cabbooking.model.User;
import com.example.cabbooking.ui.driver.checkout.DriverCheckoutFragment;
import com.example.cabbooking.ui.driver.checkout.DriverCheckoutViewModel;
import com.example.cabbooking.utilities.DirectionsJSONParser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DriverProcessBookingFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "driverBookingFragment";

    private DriverProcessBookingViewModel mViewModel;


    enum BookingState {
        PICKUP,
        DROPOFF,
    }
    //Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;


    private TextView pickUpText;
    private TextView dropOffText;
    private TextView addressText;
    private TextView serviceText;
    private TextView paymentMethodsText;

    private Button directionBtn;
    private Button callBtn;
    private Button messageBtn;
    private Button pickUpBtn;

    //Google maps variables
    private static final int MY_LOCATION_REQUEST = 99;
    private SupportMapFragment supportMapFragment; //maps view
    private GoogleMap mMap;
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private PlacesClient placesClient;
    private Marker currentUserLocationMarker;
    private Marker currentOriginLocationMarker;
    private Marker currentDestinationLocationMarker;
    private Location currentUserLocation;
    private ArrayList<Polyline> currentRoute = new ArrayList<>();

    //
    BookingState state;
    private User currentUserObject;
    private Booking currentBooking;
    private DocumentReference currentBookingDocRef;


    public static DriverProcessBookingFragment newInstance() {
        return new DriverProcessBookingFragment();
    }

    private void linkViewElements(View rootView) {
        pickUpText = rootView.findViewById(R.id.text_pickup);
        dropOffText = rootView.findViewById(R.id.text_dropOff);
        addressText = rootView.findViewById(R.id.text_address);
        serviceText = rootView.findViewById(R.id.text_service);
        paymentMethodsText = rootView.findViewById(R.id.text_payment_method);

        directionBtn = rootView.findViewById(R.id.btn_direction);
        callBtn = rootView.findViewById(R.id.btn_call);
        messageBtn = rootView.findViewById(R.id.btn_message);
        pickUpBtn = rootView.findViewById(R.id.btn_pickUp);
    }

    // CONFIRM STATE
    @SuppressLint("SetTextI18n")
    private void setViewInPickupState() {
        pickUpBtn.setText(R.string.btn_pickup_state); //Set 'i have arrived' for btn
        pickUpText.setBackgroundColor(getResources().getColor(R.color.darker_gray));
        dropOffText.setBackgroundColor(getResources().getColor(R.color.light_gray));


        //TODO

        //Draw currentDestinationLocationMarker as pickup place
        if (currentDestinationLocationMarker != null) {
            currentDestinationLocationMarker.remove();
        }
        currentDestinationLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(currentBooking.getPickUpPlaceLatitude(), currentBooking.getPickUpPlaceLongitude()))
                        .icon(bitmapDescriptorFromVector(
                                getActivity(),
                                R.drawable.ic_location_blue, Color.BLUE)
                        )
                        .title("Pickup!")
        );


        //Set destination address as pickup address
        addressText.setText(currentBooking.getPickupPlaceAddress());

        //Set service text
        serviceText.setText(currentBooking.getTransportationType() + "-" + currentBooking.getPriceInVND());
    }

    //DROP OFF STATE
    @SuppressLint("SetTextI18n")
    private void setViewInDropOffState() {
        pickUpBtn.setText(R.string.btn_dropoff_state);
        pickUpText.setBackgroundColor(getResources().getColor(R.color.light_gray));
        dropOffText.setBackgroundColor(getResources().getColor(R.color.darker_gray));

        //Draw currentDestinationLocationMarker as dropoff place
        if (currentDestinationLocationMarker != null) {
            currentDestinationLocationMarker.remove();
        }
        currentDestinationLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(currentBooking.getDropOffPlaceLatitude(), currentBooking.getDropOffPlaceLongitude()))
                        .icon(bitmapDescriptorFromVector(
                                getActivity(),
                                R.drawable.ic_location_red, Color.RED)
                        )
                        .title("Drop off!")
        );


        //Set destination address as drop off address
        addressText.setText(currentBooking.getDropOffPlaceAddress());
    }

    /**
     * Set view in check out state
     */
    private void setViewInCheckoutState() {
        pickUpBtn.setText(R.string.btn_pickup_state);
    }


    /**
     * Update arrival status on DB
     */
    private void updateArrivalStatusOnDB(){
        currentBookingDocRef.update(Constants.FSBooking.arrived, true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setViewInDropOffState();
                    }
                });
    }

    /**
     * Update finish status on DB
     */
    private void updateFinishStatusOnDB(){
        currentBookingDocRef.update(Constants.FSBooking.finished, true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendDataToCheckoutFragment();
                        loadCheckoutFragment();
                    }
                });
    }

    /**
     * load check out fragment
     */
    private void loadCheckoutFragment(){
        //TODO
        FragmentManager fm = getChildFragmentManager();
        DriverCheckoutFragment driverCheckoutFragment = DriverCheckoutFragment.newInstance();
        driverCheckoutFragment.show(fm, "fragment_driver_checkout");
    }

    /**
     * send data to checkout fragment
     */
    private void sendDataToCheckoutFragment(){
        //TODO
        DriverCheckoutViewModel driverCheckoutViewModel = new ViewModelProvider(requireActivity()).get(DriverCheckoutViewModel.class);
        driverCheckoutViewModel.setPriceInVNDString(currentBooking.getPriceInVND());
    }

    /**
     * event listener for pickupbtn
     */
    private void addEventListenerForPickUpButton() {
        pickUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (state) {
                    case PICKUP:
                        updateArrivalStatusOnDB();
                        state = BookingState.DROPOFF;
                        break;
                    case DROPOFF:
                        updateFinishStatusOnDB();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_recieve_booking, container, false);
        linkViewElements(view);
        state = BookingState.PICKUP;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        initMapsFragment();
        setActionHandlers();
//        setViewInPickupState();
        return view;
    }

    private void setActionHandlers() {
        addEventListenerForPickUpButton();
    }

    /**
     * Init Google MapsFragment
     */
    private void initMapsFragment() {
        supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_maps);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        addEventListenerForPickUpButton();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.setCurrentBookingDocRef(null);
        mViewModel.setCurrentUserObject(null);
        mViewModel.setCheckoutDone(null);
    }

    private void getCurrentBookingDetails() {
        currentBookingDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentBooking = documentSnapshot.toObject(Booking.class);
                setViewInPickupState();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel =  new ViewModelProvider(requireActivity()).get(DriverProcessBookingViewModel.class);

        //get current user object
        mViewModel.getCurrentUserObject().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user == null) return;
                currentUserObject = user;
            }
        });

        //get current booking doc ref
        mViewModel.getCurrentBookingDocRef().observe(getViewLifecycleOwner(), new Observer<DocumentReference>() {
            @Override
            public void onChanged(DocumentReference documentReference) {
                if (documentReference == null) return;
                currentBookingDocRef = documentReference;
                getCurrentBookingDetails();
//                addEventListenerForPickUpButton();
            }
        });

        //Done checkout
        mViewModel.getCheckoutDone().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) return;
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.nav_driver_home);
            }
        });
    }

    /**
     * Request user for location permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_LOCATION_REQUEST);
    }

    /**
     * //Start location update listener
     */
    @SuppressLint({"MissingPermission", "RestrictedApi"})
    private void startLocationUpdate() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); //20s
        locationRequest.setFastestInterval(10 * 1000); //20s
        locationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        Location location = locationResult.getLastLocation();
                        LatLng latLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        updateCurrentUserLocationMarker(latLng);
                        drawRouteToDestination();
                        updateCurrentDriverLocationOnDB(latLng); //TODO Changeeee this shit

                    }
                }
                , null);
    }

    /**
     * Update current user location marker
     * @param newLatLng
     */
    private void updateCurrentUserLocationMarker(LatLng newLatLng) {
        if (currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }
        drawNewCurrentUserLocationMarker(newLatLng);

//        updateCurrentDriverLocationOnDB(newLatLng);
    }

    /**
     * Update drive location on firebase
     * @param newLatLng
     */
    private void updateCurrentDriverLocationOnDB(LatLng newLatLng){
//        currentUserObject.setCurrentPositionLatitude(newLatLng.latitude);
//        currentUserObject.setCurrentPositionLongitude(newLatLng.longitude);
//        db.collection(Constants.FSUser.userCollection)
//                .document(currentUserObject.getDocId())
//                .set(currentUserObject)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
////                        drawNewCurrentUserLocationMarker(newLatLng);
//                    }
//                });

        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setCurrentPositionLatitude(newLatLng.latitude);
        driverLocation.setCurrentPositionLongitude(newLatLng.longitude);

        db.collection(Constants.FSDriverLocation.driverLocationCollection)
                .document(currentUserObject.getDocId())
                .update(Constants.FSDriverLocation.currentPositionLatitude, driverLocation.getCurrentPositionLatitude(),
                Constants.FSDriverLocation.currentPositionLongitude, driverLocation.getCurrentPositionLongitude())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        drawNewCurrentUserLocationMarker(newLatLng);
                    }
                });

    }

    /**
     * Get BitmapDescriptor from drawable vector asset, for custom cluster marker
     * @param context
     * @param vectorResId
     * @param color
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int color) {
        if (context == null) {
            return null;
        }
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        DrawableCompat.setTint(vectorDrawable, color);
        DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.SRC_IN);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void drawNewCurrentUserLocationMarker(LatLng newLatLng) {
        currentUserLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(newLatLng)
                        .icon(bitmapDescriptorFromVector(
                                getActivity(),
                                R.drawable.ic_current_location_marker, Color.BLUE)
                        )
                        .title("You are here!")
        );
    }

    /**
     * Find my position action handler
     */
    @SuppressLint("MissingPermission")
    public void onGetPositionClick() {
        locationClient.getLastLocation().
                addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Toast.makeText(getActivity(),
                                    Constants.ToastMessage.currentLocationNotUpdatedYet,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        currentUserLocation = location;
                        if (currentUserLocationMarker == null) {
                            updateCurrentUserLocationMarker(latLng);
                        }
                        smoothlyMoveCameraToPosition(latLng, Constants.GoogleMaps.CameraZoomLevel.streets);
                    }
                });
    }


    /**
     * Smoothly change camera position with zoom level
     * @param latLng
     * @param zoomLevel
     */
    private void smoothlyMoveCameraToPosition(LatLng latLng, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(cameraUpdate);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) { //Init GooglePlaceAutocomplete if not existed
            Places.initialize(requireActivity().getApplicationContext(), apiKey);
        }
        this.placesClient = Places.createClient(requireActivity().getApplicationContext());
        mMap = googleMap;
        requestPermission(); //Request user for location permission
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        mMap.getUiSettings().setZoomControlsEnabled(true);
        startLocationUpdate(); //Start location update listener
//        setUpCluster(); //Set up cluster on Google Map
        onGetPositionClick();  // Position the map.
    }

    /**
     * Draw route on the map
     * @param result
     */
    private void drawRoute(List<List<HashMap<String, String>>> result)
    {
        //Clear current route
        for (Polyline polyline : currentRoute) {
            polyline.remove();
        }
        currentRoute.clear();

        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> route = result.get(i);

            for (int j = 0; j < route.size(); j++) {
                HashMap<String, String> point = route.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.RED);
            lineOptions.geodesic(true);

        }

        // Drawing polyline in the Google Map for the i-th route
        currentRoute.add(mMap.addPolyline(lineOptions));
    }

    /**
     * A Class to call Google Directions API with callback
     */
    private class FetchRouteDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = fetchDataFromURL(url[0]);
            } catch (Exception ignored) {
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            RouteParserTask routeParserTask = new RouteParserTask();
            routeParserTask.execute(result);
        }
    }

    /**
     * Draw route to destination
     */
    private void drawRouteToDestination() {
        // Checks, whether start and end locations are captured
        // Getting URL to the Google Directions API

        if (currentDestinationLocationMarker == null) return;

        String url = getRouteUrl(currentUserLocationMarker.getPosition(), currentDestinationLocationMarker.getPosition(), "driving");

        if (url == null) return;

        FetchRouteDataTask fetchRouteDataTask = new FetchRouteDataTask();

        // Start fetching json data from Google Directions API
        fetchRouteDataTask.execute(url);
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class RouteParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser(jObject);

                routes = parser.getRoutes();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            drawRoute(result); //Draw new route
        }
    }

    /**
     * Method to get URL for fetching data from Google Directions API (finding direction from origin to destination)
     *
     * @param origin
     * @param destination
     * @param directionMode
     * @return
     */
    private String getRouteUrl(LatLng origin, LatLng destination, String directionMode) {
        String originParam = Constants.GoogleMaps.DirectionApi.originParam +
                "=" + origin.latitude + "," + origin.longitude;
        String destinationParam = Constants.GoogleMaps.DirectionApi.destinationParam +
                "=" + destination.latitude + "," + destination.longitude;
        String modeParam = Constants.GoogleMaps.DirectionApi.modeParam + "=" + directionMode;
        String params = originParam + "&" + destinationParam + "&" + modeParam;
        String output = Constants.GoogleMaps.DirectionApi.outputParam;
        if (!isDetached()) {
            return Constants.GoogleMaps.DirectionApi.baseUrl + output + "?" + params
                    + "&key=" + getString(R.string.google_maps_key);
        }
        return null;
    }

    /**
     * A method to fetch json data from url
     */
    private String fetchDataFromURL(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
