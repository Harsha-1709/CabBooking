package com.example.cabbooking.ui.driver.checkout;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DriverCheckoutViewModel extends ViewModel {

    private MutableLiveData<String> priceInVNDString;

    public DriverCheckoutViewModel() {
        priceInVNDString = new MutableLiveData<>();

    }

    public void setPriceInVNDString(String priceInVNDString) {
        this.priceInVNDString.setValue(priceInVNDString);
    }

    public MutableLiveData<String> getPriceInVNDString() {
        return priceInVNDString;
    }
}
