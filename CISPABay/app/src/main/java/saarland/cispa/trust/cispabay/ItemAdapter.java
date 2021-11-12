package saarland.cispa.trust.cispabay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import saarland.cispa.trust.cispabay.models.Item;

public class ItemAdapter extends ArrayAdapter<Item> {

    private final Context mContext;
    private final List<Item> itemsList;

    public ItemAdapter(@NonNull Context context, ArrayList<Item> list) {
        super(context, 0, list);
        mContext = context;
        itemsList = list;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);

        Item currentItem = itemsList.get(position);

        ImageView image = listItem.findViewById(R.id.item_image);
        image.setImageURI(Uri.parse(currentItem.getImagePath()));

        TextView title = listItem.findViewById(R.id.item_title);
        title.setText(currentItem.getTitle());

        TextView price = listItem.findViewById(R.id.item_price);
        price.setText(String.format("%d€", currentItem.getPrice()));

        TextView description = listItem.findViewById(R.id.item_description);
        description.setText(currentItem.getDescription());



        registerSharingHandler(listItem, currentItem.getId());

        return listItem;
    }

    private void registerSharingHandler(View listItem, int itemId) {
        ImageButton shareButton = listItem.findViewById(R.id.item_share_btn);
        shareButton.setTag(itemId);
        shareButton.setOnClickListener(view -> {
            // @TODO : IMPLEMENT
        });
    }

}
