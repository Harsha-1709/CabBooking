package com.example.cabbooking.ui.driver.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cabbooking.model.User;

public class DriverHomeViewModel extends ViewModel {
    private MutableLiveData<User> currentUserObject;
    private MutableLiveData<Boolean> acceptBookingBtnPressed;

    public DriverHomeViewModel() {
        currentUserObject = new MutableLiveData<>();
        acceptBookingBtnPressed = new MutableLiveData<>();
    }

    public void setCurrentUserObject(User currentUserObject) {
        this.currentUserObject.setValue(currentUserObject);
    }

    public void setAcceptBookingBtnPressed(Boolean acceptBookingBtnPressed) {
        this.acceptBookingBtnPressed.setValue(acceptBookingBtnPressed);
    }

    public MutableLiveData<User> getCurrentUserObject(){
        return this.currentUserObject;
    }

    public MutableLiveData<Boolean> getAcceptBookingBtnPressed() {
        return acceptBookingBtnPressed;
    }
}