package com.example.cabbooking.ui.customer.booking.processing_booking;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * View model for ProcessingBookingFragment
 */
public class ProcessingBookingViewModel extends ViewModel {
    private MutableLiveData<String> dropOffPlaceAddress;
    private MutableLiveData<String> pickupPlaceAddress;
    private MutableLiveData<String> priceInVNDString;


    public ProcessingBookingViewModel() {
        dropOffPlaceAddress = new MutableLiveData<>();
        pickupPlaceAddress = new MutableLiveData<>();
        priceInVNDString = new MutableLiveData<>();
    }

    public void setPriceInVNDString(String priceInVNDString) {
        this.priceInVNDString.setValue(priceInVNDString);
    }

    public void setDropOffPlaceString(String dropOffPlaceString) {
        this.dropOffPlaceAddress.setValue(dropOffPlaceString);
    }

    public void setPickupPlaceString(String pickupPlaceString) {
        this.pickupPlaceAddress.setValue(pickupPlaceString);
    }

    public MutableLiveData<String> getPriceInVNDString() {
        return priceInVNDString;
    }

    public MutableLiveData<String> getDropOffPlaceString() {
        return dropOffPlaceAddress;
    }

    public MutableLiveData<String> getPickupPlaceString() {
        return pickupPlaceAddress;
    }
}