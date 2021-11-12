package saarland.cispa.trust.cispabay.fragments;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Objects;

import saarland.cispa.trust.cispabay.ItemAdapter;
import saarland.cispa.trust.cispabay.MainActivity;
import saarland.cispa.trust.cispabay.R;
import saarland.cispa.trust.cispabay.models.Item;

public class HomeFragment extends Fragment {
    private ListView listView;
    private final MainActivity parentActivity;

    public HomeFragment(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Prepare the list of items.
        ArrayList<Item> items = new ArrayList<>();

        // Get content provider client from the content resolver.
        String CP_AUTHORITY = "content://saarland.cispa.trust.serviceapp.contentprovider/items";
        Uri contentProviderUri = Uri.parse(CP_AUTHORITY);
        ContentProviderClient cp = Objects.requireNonNull(this.getContext()).getContentResolver()
                .acquireContentProviderClient(contentProviderUri);

        // Instantiate items and insert them into the items ArrayList
        try {
            Cursor cursor = Objects.requireNonNull(cp).query(contentProviderUri,
                    null, null, null, null);
            while (Objects.requireNonNull(cursor).moveToNext()) {
                int _id = cursor.getInt(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                String imagePath = cursor.getString(3);
                int price = cursor.getInt(4);
                double latitude = cursor.getDouble(5);
                double longitude = cursor.getDouble(6);
                items.add(new Item(_id, title, description, imagePath, price, latitude, longitude));
            }
        } catch (RemoteException | NullPointerException e) {
            e.printStackTrace();
        }

        // Get the UI handle for the list view.
        listView = view.findViewById(R.id.items_list);

        // Register the adapter and pass the items list.
        ItemAdapter mAdapter = new ItemAdapter(Objects.requireNonNull(this.getContext()), items);
        listView.setAdapter(mAdapter);

        // Register onItemClickListener
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Item selectedItem = (Item) listView.getItemAtPosition(position);
            MainActivity.openFragment(Objects.requireNonNull(HomeFragment.this.getActivity()),
                    MapFragment.newInstance(selectedItem, parentActivity.getLastKnownLocation()));
        });

        return view;
    }
}