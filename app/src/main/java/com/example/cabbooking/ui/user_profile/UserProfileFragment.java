package com.example.cabbooking.ui.user_profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.Constants;
import com.example.cabbooking.R;
import com.example.cabbooking.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserProfileFragment extends Fragment {

    private UserProfileViewModel mViewModel;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference mStorageRef;
    User currentUserObject = null;


    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    private ImageView profileImgView;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText dateOfBirthEditText;
    private RadioButton maleRadioBtn;
    private RadioButton femaleRadioBtn;
    private Button updateBtn;
    private Button pickerBtn;
    private Button changePassBtn;
    private int year, month, day;


    private void linkViewElements(View rootView) {
        profileImgView = rootView.findViewById(R.id.image_userAva);
        nameEditText = rootView.findViewById(R.id.editText_name);
        emailEditText = rootView.findViewById(R.id.editText_email);
        phoneEditText = rootView.findViewById(R.id.editText_phone);
        dateOfBirthEditText = rootView.findViewById(R.id.editText_DOB);
        maleRadioBtn = rootView.findViewById(R.id.radioButton_genderMale);
        femaleRadioBtn = rootView.findViewById(R.id.radioButton_genderFemale);
        updateBtn = rootView.findViewById(R.id.btn_updateProfle);
        pickerBtn = rootView.findViewById(R.id.btn_pickDate);
        changePassBtn = rootView.findViewById(R.id.btn_changePass);
    }

    //date picker dialog for birthday
    private void setDatePickerBtnAction() {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        pickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(pickerBtn.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dateOfBirthEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

    }

    //Validation after input birth date in the edit text
    private void setBirthDateEditTextAutoFormat() {

        dateOfBirthEditText.addTextChangedListener(new TextWatcher() {
            private String curDateStr = "";
            private final Calendar calendar = Calendar.getInstance();
            private final int tempYear = calendar.get(Calendar.YEAR);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Take action at most 1 number is changed at a time.
                if (!s.toString().equals(curDateStr) && count == 1) {
                    //Current date string in the edit text, after latest change, without the "/" character
                    String curDateStrAfterChangedWithoutSlash = s.toString().replaceAll("[^\\d.]|\\.", "");
                    //Current date string in the edit text, before the latest change, without the "/" character
                    String curDateStrBeforeChangedWithoutSlash = curDateStr.replaceAll("[^\\d.]|\\.", "");

                    int dateStrAfterChangedLen = curDateStrAfterChangedWithoutSlash.length();
                    int cursorPos = dateStrAfterChangedLen; //Cursor position

                    for (int i = 2; i <= dateStrAfterChangedLen && i < 6; i += 2) {
                        cursorPos++;
                    }

                    //If delete the slash character "/", move cursor back 1 position
                    if (curDateStrAfterChangedWithoutSlash.equals(curDateStrBeforeChangedWithoutSlash))
                        cursorPos--;

                    //If the current date string, after latest change, without slash, is not fully filled
                    if (curDateStrAfterChangedWithoutSlash.length() < 8) {
                        String dateFormat = "DDMMYYYY";
                        //
                        curDateStrAfterChangedWithoutSlash = curDateStrAfterChangedWithoutSlash
                                + dateFormat.substring(curDateStrAfterChangedWithoutSlash.length());
                    } else {
                        //Validate and fix the input date if necessary
                        int day = Integer.parseInt(curDateStrAfterChangedWithoutSlash.substring(0, 2));
                        int month = Integer.parseInt(curDateStrAfterChangedWithoutSlash.substring(2, 4));
                        int year = Integer.parseInt(curDateStrAfterChangedWithoutSlash.substring(4, 8));

                        month = month < 1 ? 1 : Math.min(month, 12); //Max month is 12
                        calendar.set(Calendar.MONTH, month - 1);

                        year = (year < 1900) ? 1900 : Math.min(year, tempYear); //Max year for birthday is this year
                        calendar.set(Calendar.YEAR, year);

                        //Get the right day according to the input year and month
                        day = Math.min(day, calendar.getActualMaximum(Calendar.DATE));
                        curDateStrAfterChangedWithoutSlash = String.format("%02d%02d%02d", day, month, year);
                    }

                    //finalize the form of displayed date string
                    curDateStrAfterChangedWithoutSlash = String.format("%s/%s/%s", curDateStrAfterChangedWithoutSlash.substring(0, 2),
                            curDateStrAfterChangedWithoutSlash.substring(2, 4),
                            curDateStrAfterChangedWithoutSlash.substring(4, 8));

                    //Set date string as text in the EditText view and set the cursor position, update current date string
                    cursorPos = Math.max(cursorPos, 0);
                    curDateStr = curDateStrAfterChangedWithoutSlash;
                    dateOfBirthEditText.setText(curDateStr);
                    dateOfBirthEditText.setSelection(Math.min(cursorPos, curDateStr.length()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Send intent to open photo gallery
     */
    private void handleProfileImageClick() {
        profileImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK) {
                Uri imgUri = data.getData();
                profileImgView.setImageURI(imgUri);
                uploadImageFirebase(imgUri);
            }
        }
    }

    /**
     * Get profile image from firebase
     */
    private void setImageFromFirebase() {
        StorageReference fref = mStorageRef.child("profileImages").child(currentUserObject.getDocId()+".jpeg");

        fref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImgView);            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    /**
     * Upload profile image
     * @param uri
     */
    private void uploadImageFirebase(Uri uri) {
        StorageReference fref = mStorageRef.child("profileImages").child(currentUserObject.getDocId()+".jpeg");
        fref.putFile(uri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("USER PROFILE", "onSuccess: upload success");
                Toast.makeText(getActivity(), "Image Uploaded", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("USER PROFILE", "onFalure: upload failed" + e.getMessage());

                    }
                }
        );
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        linkViewElements(view);
        setDatePickerBtnAction();
        setBirthDateEditTextAutoFormat();
        return view;
    }

    /**
     * Render user info to view
     */
    private void renderUserDetails() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        nameEditText.setText(currentUserObject.getUsername());
        emailEditText.setText(currentUserObject.getEmail());
        phoneEditText.setText(currentUserObject.getPhone());
        dateOfBirthEditText.setText(df.format(currentUserObject.getBirthDate()));
        if(currentUserObject.getGender() == "Male") {
            maleRadioBtn.toggle();
        } else {
            femaleRadioBtn.toggle();
        }
    }


    /**
     * Set change password button event listener
     */
    private void setChangePasswordBtnHandler() {
        changePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.sendPasswordResetEmail(Objects.requireNonNull(currentUser.getEmail()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Please check your email to receive further instruction", Toast.LENGTH_LONG).show();
                                    Log.d("USER PROFILE", "Email sent.");
                                }
                            }
                        });
            }
        });
    }

    /**
     * set update button event handler
     */
    private void setUpdateBtnHandler() {
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gender = maleRadioBtn.isChecked() ? "Male" : "Female";

                String[] splitBirthDateStr = dateOfBirthEditText.getText().toString().split("/");
                int day = Integer.parseInt(splitBirthDateStr[0]);
                int month = Integer.parseInt(splitBirthDateStr[1]);
                int year = Integer.parseInt(splitBirthDateStr[2]);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                Date birthDateNew = null;
                try {
                    birthDateNew = df.parse(month + "/" + day + "/" + year);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Map<String, Object> userData = new HashMap<>();
                userData.put(Constants.FSUser.usernameField, nameEditText.getText().toString());
                userData.put(Constants.FSUser.phoneField, phoneEditText.getText().toString());
                userData.put(Constants.FSUser.genderField, gender);
                userData.put(Constants.FSUser.birthDateField, birthDateNew);

                db.collection(Constants.FSUser.userCollection).document(currentUserObject.getDocId())
                        .update(userData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "User profile updates",
                                            Toast.LENGTH_LONG).show();
                                } else {

                                }
                            }
                        });
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        setImageFromFirebase();
        handleProfileImageClick();
        setChangePasswordBtnHandler();
        setUpdateBtnHandler();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        mViewModel.getCurrentUserObject().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUserObject = user;
                renderUserDetails();
                setImageFromFirebase();
            }
        });
    }
}
