package saarland.cispa.trust.cispabay.fragments;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import saarland.cispa.trust.cispabay.MainActivity;
import saarland.cispa.trust.cispabay.R;

public class AddItemFragment extends Fragment {
    private ImageView imageView;
    private Uri photoURI = null;
    private final int RESULT_OK = 200;
    private final MainActivity parentActivity;


    public AddItemFragment(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        // Set a placeholder picture
        imageView = view.findViewById(R.id.imageview);
        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.image_placeholder, null));

        // Get handles to input fields
        final Button takePictureButton = view.findViewById(R.id.takeImageBtn);
        final Button addItemButton = view.findViewById(R.id.addItemBtn);
        final EditText titleInput = view.findViewById(R.id.titleInput);
        final EditText descriptionInput = view.findViewById(R.id.descriptionInput);
        final EditText priceInput = view.findViewById(R.id.priceInput);

        // Create a listener for addItemButton
        View.OnClickListener onAddItemButtonClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validated = validateInput(titleInput, descriptionInput, priceInput);
                if (!validated)
                    return;

                int itemId = addNewItem(titleInput, descriptionInput, priceInput);

                if (itemId > -1) {
                    MainActivity.openFragment(Objects.requireNonNull(AddItemFragment.this.getActivity()),
                            new HomeFragment(parentActivity));
                    MainActivity.sendNewItemNotification(parentActivity, itemId);
                }
            }
        };
        // Register the listener for addItemButton
        addItemButton.setOnClickListener(onAddItemButtonClickedListener);


        // Create a listener for takePictureButton
        View.OnClickListener onTakePictureButtonClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        };
        // Register the listener for takePictureButton
        takePictureButton.setOnClickListener(onTakePictureButtonClickedListener);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_OK) {
            imageView.setImageURI(photoURI);
        }
    }

    /**
     * Creates a file in the external storage and returns it
     *
     * @return File
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    /**
     * Uses the Camera application to take a photo. This technique uses startActivityForResult
     * see https://developer.android.com/training/basics/intents/result
     */
    private void takePicture() {
        boolean hasPermission = parentActivity.hasPermissionToTakePhoto();
        if (hasPermission) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(getContext(),
                            "saarland.cispa.trust.cispabay.file_provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, RESULT_OK);
                }
            }

        }
    }

    /**
     * Validates the input fields and creates a toast message if validation failed.
     *      All fields are required
     *
     * @param titleInput The EditText handle of the title string.
     * @param descriptionInput The EditText handle of the description string.
     * @param priceInput The EditText handle of the price.
     *
     * @return True if the validation succeeded and false otherwise.
     */
    private boolean validateInput(EditText titleInput, EditText descriptionInput, EditText priceInput) {
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        int price = (TextUtils.isEmpty(priceInput.getText().toString()))? -1 : Integer.parseInt(priceInput.getText().toString());
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || price < 0) {
            Toast.makeText(this.getActivity(),
                    "Please, fill in all fields.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (photoURI == null) {
            Toast.makeText(this.getActivity(),
                    "Please, add a picture to this item before submitting it.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    /**
     * Inserts a new item in the content provider hosted by the ServiceApp.
     *
     * @param titleInput The EditText handle of the title string.
     * @param descriptionInput The EditText handle of the description string.
     * @param priceInput The EditText handle of the price.
     * @return True if the item is inserted and false otherwise.
     */
    private int addNewItem(EditText titleInput, EditText descriptionInput, EditText priceInput) {
        Uri contentProviderUri = Uri.parse("content://saarland.cispa.trust.serviceapp.contentprovider/items");
        ContentProviderClient cp = parentActivity.getContentResolver().acquireContentProviderClient(
                contentProviderUri);
        if (cp == null)
            return -1;

        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        String imagePath = photoURI.toString();
        int price = Integer.parseInt(priceInput.getText().toString());

        Location location = parentActivity.getLastKnownLocation();
        if (location == null) {
            location = new Location("");
            location.setLatitude(0.0d);
            location.setLongitude(0.0d);
        }

        Uri newItem;
        ContentValues newValues = new ContentValues();
        newValues.put("title", title);
        newValues.put("description", description);
        newValues.put("image_path", imagePath);
        newValues.put("price", price);
        newValues.put("latitude", location.getLatitude());
        newValues.put("longitude", location.getLongitude());
        try {
            assert cp != null;
            newItem = cp.insert(contentProviderUri, newValues);
        } catch (RemoteException | NullPointerException e) {
            Toast.makeText(this.getActivity(),
                    "Something went wrong while inserting the item",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return -3;
        }
        return Integer.parseInt(Objects.requireNonNull(newItem).getLastPathSegment());
    }
}