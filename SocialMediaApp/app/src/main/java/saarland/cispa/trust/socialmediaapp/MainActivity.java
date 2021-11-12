package saarland.cispa.trust.socialmediaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final private int START_ACTIVITY_FOR_RESULT_CODE = 200;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.image_placeholder, null));

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            int id = Integer.parseInt(intent.getStringExtra(Intent.EXTRA_TEXT));
            requestUriPermissionFromServiceApp(id);
        } else {
            Log.d("SocialMedia", "Not a sharing intent!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_ACTIVITY_FOR_RESULT_CODE) {
            // Get content provider client from the content resolver.
            int id = Integer.parseInt(data.getStringExtra("ID"));
            getItemAndPopulateView(id);
        }
    }

    private void requestPermission() {
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, readExternalStorage) == PackageManager.PERMISSION_GRANTED) {
            Log.d("SocialMediaApp", "Permission READ_EXTERNAL_STORAGE is granted");
        } else {
            Log.d("SocialMediaApp", "Requesting READ_EXTERNAL_STORAGE permission");
            ActivityCompat.requestPermissions(
                    this, new String[]{readExternalStorage}, 0);
        }
    }

    private void requestUriPermissionFromServiceApp(int id) {
        String currentApp = getApplicationContext().getPackageName();
        Intent grantAccessIntent = new Intent("saarland.cispa.trust.intent.service.GRANT_ACCESS_TO_ITEM");
        grantAccessIntent.putExtra("ID", id);
        grantAccessIntent.putExtra("PACKAGE", currentApp);

        String targetApp = "saarland.cispa.trust.serviceapp";
        String targetComponent = "saarland.cispa.trust.serviceapp.MainActivity";
        grantAccessIntent.setComponent(new ComponentName(targetApp, targetComponent));
        startActivityForResult(grantAccessIntent, START_ACTIVITY_FOR_RESULT_CODE);
    }

    @SuppressLint("DefaultLocale")
    private void getItemAndPopulateView(int id) {
        Uri contentProviderUri =
                Uri.parse("content://saarland.cispa.trust.serviceapp.contentprovider/items/" + id);
        ContentProviderClient cp = this.getContentResolver()
                .acquireContentProviderClient(contentProviderUri);

        // Instantiate items and insert them into the items ArrayList
        try {
            Cursor cursor = cp.query(contentProviderUri,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                String imagePath = cursor.getString(3);
                int price = cursor.getInt(4);

                imageView = findViewById(R.id.imageView);
                imageView.setImageURI(Uri.parse(imagePath));

                TextView titleTextView = findViewById(R.id.title_content_txt);
                titleTextView.setText(title);

                TextView priceTextView = findViewById(R.id.price_content_txt);
                priceTextView.setText(String.format("%d€", price));

                TextView descTextView = findViewById(R.id.description_content_txt);
                descTextView.setText(description);
            }

            cursor.close();
        } catch (RemoteException | NullPointerException e) {
            e.printStackTrace();
        }
        cp.release();
    }
}
