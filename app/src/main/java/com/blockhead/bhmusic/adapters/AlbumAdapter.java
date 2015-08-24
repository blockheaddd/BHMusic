package com.blockhead.bhmusic.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.activities.MainActivity;
import com.blockhead.bhmusic.objects.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

/**
 * Created by Gus on 2/25/2015.
 */
public class AlbumAdapter extends BaseAdapter implements SectionIndexer {

    private ArrayList<Album> albums;
    private LayoutInflater albumInf;
    private Context context;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public AlbumAdapter(Context c, ArrayList<Album> theAlbums) {
        imageLoader = ImageLoader.getInstance(); // Get singleton instance

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_cover_xlarge) // resource or drawable
                .showImageOnFail(R.drawable.default_cover_xlarge)
                .resetViewBeforeLoading(true)  // default
                .cacheInMemory(true)
                .displayer(new FadeInBitmapDisplayer(500))
                .build();

        albums = theAlbums;
        albumInf = LayoutInflater.from(c);
        context = c;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int arg0) {
        return albums.get(arg0).getTitle();
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int postion, View convertView, ViewGroup parent) {

        LinearLayout albumLay;
        if(convertView != null)
            albumLay = (LinearLayout) convertView;
        else        //Else recycle view
            albumLay = (LinearLayout) albumInf.inflate(R.layout.album, parent, false);

        //get title and artist views
        TextView albumTitleView = (TextView) albumLay.findViewById(R.id.album_title);
        TextView artistView = (TextView) albumLay.findViewById(R.id.album_artist);
        ImageView coverView = (ImageView) albumLay.findViewById(R.id.artImage);
        CardView cardView = (CardView) albumLay.findViewById(R.id.albumCard);

        //get song using position
        Album currAlbum = albums.get(postion);

        //get/set title and artist strings
        albumTitleView.setText(currAlbum.getTitle());
        artistView.setText(currAlbum.getArtist());


        imageLoader.displayImage(currAlbum.getCoverURI(), coverView, options);

        //Accent Color
        int accentColor = currAlbum.getAccentColor();
        if (accentColor != Color.WHITE && currAlbum.getCoverURI() != null)
        {
            cardView.setCardBackgroundColor(accentColor);
            coverView.setBackgroundColor(accentColor);
            albumTitleView.setTextColor(Color.WHITE);
            artistView.setTextColor(parent.getResources().getColor(R.color.hint_white));
        }
        else
        {
            cardView.setCardBackgroundColor(Color.WHITE);
            coverView.setBackgroundColor(parent.getResources().getColor(currAlbum.getRandomColor()));
            albumTitleView.setTextColor(Color.BLACK);
            artistView.setTextColor(parent.getResources().getColor(R.color.secondary_text_default_material_light));
        }



        //set position as tag
        albumLay.setTag(postion);

        return albumLay;
    }


    //Section Indexer Functions
    @Override
    public int getPositionForSection(int section) {
        if (section == 0)
            return 0;
        int count = 0;
        //while first letter does not match section
        for (int i = 0; i < albums.size(); i++) {
            String temp = albums.get(i).getTitle().charAt(0) + "";
            String key = (char) (section + 64) + "";
            String keyPassed = (char) (section + 65) + "";
            if (temp.equalsIgnoreCase(key) || temp.equalsIgnoreCase(keyPassed))
                return count;
            else
                count++;
        }
        return count;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }
}
