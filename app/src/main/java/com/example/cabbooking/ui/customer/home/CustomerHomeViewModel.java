package com.example.cabbooking.ui.customer.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cabbooking.model.User;

public class CustomerHomeViewModel extends ViewModel {
    private MutableLiveData<User> currentUserObject;


    public CustomerHomeViewModel() {
        currentUserObject = new MutableLiveData<>();
    }

    public void setCurrentUserObject(User currentUserObject) {
        this.currentUserObject.setValue(currentUserObject);
    }

    public MutableLiveData<User> getCurrentUserObject(){
        return this.currentUserObject;
    }
}