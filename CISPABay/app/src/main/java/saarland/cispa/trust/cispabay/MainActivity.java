package saarland.cispa.trust.cispabay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import saarland.cispa.trust.cispabay.fragments.AddItemFragment;
import saarland.cispa.trust.cispabay.fragments.HomeFragment;
import saarland.cispa.trust.cispabay.fragments.UserFragment;
import saarland.cispa.trust.serviceapp.IRemoteService;

public class MainActivity extends AppCompatActivity {

    IRemoteService mIRemoteService;
    BottomNavigationView bottomNavigation;

    // BottomNavigation handler
    @SuppressLint("NonConstantResourceId")
    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        openFragment(MainActivity.this, new HomeFragment(MainActivity.this));
                        return true;
                    case R.id.navigation_add_item:
                        openFragment(MainActivity.this, new AddItemFragment(MainActivity.this));
                        return true;
                    case R.id.navigation_user:
                        openFragment(MainActivity.this, UserFragment.newInstance());
                        return true;
                }
                return false;
            };


    // Connect to the remote service and invoke the getVersion() API.
    private final ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("getVersion", "connected!");
            // This gets an instance of the IRemoteInterface, which we can use to call on the service
            mIRemoteService = IRemoteService.Stub.asInterface(service);
            String version;
            try {
                version = mIRemoteService.getVersion();
                Log.d("CISPABay", "getVersion()=" + version);
            } catch (RemoteException | SecurityException e) {
                Log.e("CISPABay", "Error calling getVersion()");
                e.printStackTrace();
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mIRemoteService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For development purposes, populate the content provider
        // with some entries to show in listView
        populateContentProviderIfEmpty();

        // Suppress warnings about accessing public storage
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        // Open the home fragment
        openFragment(this, new HomeFragment(this));
    }

    /**
     * Transits to the target fragment
     * @param fragmentActivity current fragment activity
     * @param fragment the target fragment.
     */
    public static void openFragment(FragmentActivity fragmentActivity, Fragment fragment) {
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Intent i = new Intent("saarland.cispa.trust.intent.service.REMOTE_SERVICE");
        i.setPackage("saarland.cispa.trust.serviceapp");

        boolean bound;
        try {
            bound = bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        } catch (SecurityException e) {
            e.printStackTrace();
            bound = false;
        }
        if(bound) {
            Log.d("CISPABay", "Binding to remote service succeeded");
        } else {
            Log.d("CISPABay", "Binding to remote service failed");
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(mIRemoteService != null)
        {
            unbindService(mConnection);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    /**
     * Uses the Location Service to retrieve the last known location using one of the available
     *  providers: LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, etc..
     *  see https://developer.android.com/reference/android/location/LocationManager.html#getLastKnownLocation(java.lang.String)
     *  Notice that this API (getLastKnownLocation) assumes that a last location exists, otherwise,
     *  it will return null object. To circumvent this, you can either fake a location in the
     *  emulator (see https://developer.android.com/studio/run/emulator-console#geo) or request
     *  location updates to set lastKnownLocation global object (see https://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(java.lang.String,%20long,%20float,%20android.location.LocationListener))
     * @return Location
     */
    public Location getLastKnownLocation() {
        return null;
    }

    /**
     * Checks for two permissions: Manifest.permission.CAMERA and Manifest.permission.WRITE_EXTERNAL_STORAGE
     *  and returns true if both are granted, false otherwise
     *
     * @return boolean
     */
    public boolean hasPermissionToTakePhotoAndStoreInExternalStorage() {
        return false;
    }

    public static void sendNewItemNotification(MainActivity parentActivity, int itemId) {}

    /**
     * Inserts two dump items so the list view won't be empty
     */
    private void populateContentProviderIfEmpty() {
        Uri contentProviderUri = Uri.parse("content://saarland.cispa.trust.serviceapp.contentprovider/items");
        ContentProviderClient cp = getContentResolver().acquireContentProviderClient(contentProviderUri);

        try (Cursor cursor = cp.query(contentProviderUri, null, null, null, null)) {
            if (cursor.getCount() != 0) {
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }

        String uriSofa = "android.resource://saarland.cispa.trust.cispabay/" + R.drawable.sofa;
        String uriTv = "android.resource://saarland.cispa.trust.cispabay/" + R.drawable.tv;

        ContentValues sofaItem = new ContentValues();
        sofaItem.put("title", "Beige Sofa");
        sofaItem.put("description", "Almost new Sofa for half price!");
        sofaItem.put("image_path", uriSofa);
        sofaItem.put("price", 50);
        sofaItem.put("latitude", 49.259455);
        sofaItem.put("longitude", 7.051713);

        ContentValues tvItem = new ContentValues();
        tvItem.put("title", "New TV 45'");
        tvItem.put("description", "DTS HD, HD Triple Tuner, CI+ (B-Ware), if you know what does that mean!");
        tvItem.put("image_path", uriTv);
        tvItem.put("price", 250);
        tvItem.put("latitude", 49.259455);
        tvItem.put("longitude", 7.051713);
        try {
            cp.insert(contentProviderUri, sofaItem);
            cp.insert(contentProviderUri, tvItem);
        } catch (RemoteException | NullPointerException e) {
            Toast.makeText(this,
                    "Something went wrong while inserting the item",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        cp.release();
    }
}
