package com.blockhead.bhmusic.activities;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blockhead.bhmusic.R;
import com.blockhead.bhmusic.adapters.ArtistsTracksAdapter;
import com.blockhead.bhmusic.objects.Album;
import com.blockhead.bhmusic.objects.Artist;
import com.blockhead.bhmusic.objects.Song;
import com.blockhead.bhmusic.utils.SongOptions;
import com.nirhart.parallaxscroll.views.ParallaxExpandableListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ViewArtistActivity extends AppCompatActivity {
    MusicService musicSrv;
    FloatingActionButton fab;
    Artist currArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_artist);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        if(mActionBar != null)
            mActionBar.setTitle("");

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Log.d("ANIM", "ENTER (Artist) | Names: " + names.toString() + "\n shareElementsKeys: " + sharedElements.keySet().toArray().toString());
                super.onMapSharedElements(names, sharedElements);
            }
        });

        musicSrv = MainActivity.getMusicService();
        /* Setup Floating Action Button */
        fab = (FloatingActionButton)findViewById(R.id.artistFab);
        setFabDrawable();
        fab.setBackgroundTintList(ColorStateList.valueOf(MainActivity.accentColor));
        fab.setClickable(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabPressed();
            }
        });
        fab.setLongClickable(true);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                nowPlayingButtonPressed();
                return true;
            }
        });


        final ParallaxExpandableListView xLV = (ParallaxExpandableListView)findViewById(R.id.expandableListView);
        currArtist = MainActivity.currArtist;
        ArrayList<Album> albums = currArtist.getAlbums();

        final RelativeLayout abHolder = (RelativeLayout)findViewById(R.id.artist_ab_holder);
        FrameLayout abBackground = (FrameLayout)findViewById(R.id.artist_ab_background);

        int accentColor = currArtist.getAccentColor();
        if(accentColor == Color.WHITE)
            accentColor = MainActivity.primaryColor;

        abBackground.setBackgroundColor(accentColor);
        abHolder.setAlpha(0);

        AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mActionBar != null) {
                    if (firstVisibleItem == 0 && abHolder.getAlpha() == 1) {
                        abHolder.setAlpha(0);
                        mActionBar.setTitle("");
                    } else if (firstVisibleItem >= 1 && abHolder.getAlpha() == 0) {
                        abHolder.setAlpha(1);
                        mActionBar.setTitle(currArtist.getName());
                    }
                }
            }
        };

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_artist_xlarge) // resource or drawable
                .showImageOnFail(R.drawable.default_artist_xlarge)
                .cacheOnDisk(true)
                .build();

        ImageView header = new ImageView(this);

        //Set Header Max Height
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double maxHeight = size.y * 0.6667;
        double minHeight = size.y * 0.5;
        header.setMaxHeight((int) maxHeight);
        header.setTransitionName("artistImage");
        //header.setMinimumHeight((int) minHeight);
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(size.x , (int)minHeight);
        header.setLayoutParams(layoutParams);

        String coverPath = currArtist.getImagePath();
        if(coverPath == null)
        { //Set default artist art if none
            header.setImageResource(R.drawable.default_artist_xlarge);
            header.setBackgroundColor(getResources().getColor(currArtist.getRandomColor()));
        }
        else
        {
            imageLoader.displayImage(coverPath, header, displayOptions);
        }
        header.setAdjustViewBounds(true);
        header.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //Header background for fade effect
        FrameLayout headerBg = (FrameLayout)findViewById(R.id.expandableListView_bg);
        headerBg.setMinimumHeight((int) maxHeight);
        headerBg.setBackgroundColor(accentColor);

        //Set Expanded List View Properties
        xLV.addParallaxedHeaderView(header);
        xLV.setDivider(null);
        xLV.setBackgroundColor(Color.TRANSPARENT);

        int headerHeight = getActionBarHeight() + getStatusBarHeight();
        final ArtistsTracksAdapter mArtistsTracksAdapter = new ArtistsTracksAdapter(getApplicationContext(), albums, headerHeight);
        xLV.setAdapter(mArtistsTracksAdapter);
        xLV.setOnScrollListener(mOnScrollListener);
        ExpandableListView.OnChildClickListener mOnClickedListener = new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(groupPosition != 0)
                    artistTrackPicked(groupPosition,childPosition);
                return true;
            }
        };
        xLV.setOnChildClickListener(mOnClickedListener);

        xLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    Song song = mArtistsTracksAdapter.getChild(groupPosition, childPosition);
                    SongOptions.openSongOptions(song
                            , ViewArtistActivity.this
                            , (CoordinatorLayout)findViewById(R.id.view_artist_coordinator));
                    return true;
                }

                return false;
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            xLV.setIndicatorBoundsRelative(size.x - 150, size.x);
        else
            xLV.setIndicatorBounds(size.x - 150,size.x);

    }//END ON CREATE METHOD

    private void setFabDrawable()
    {
        Drawable pauseDrawable = getResources().getDrawable(R.drawable.ic_pause_white_36dp);
        Drawable playDrawable = getResources().getDrawable(R.drawable.ic_play_white_36dp);
        if(musicSrv.isPng())
            fab.setImageDrawable(pauseDrawable);
        else
            fab.setImageDrawable(playDrawable);
    }

    private void fabPressed()
    {
        if(musicSrv.isPng())
            musicSrv.pausePlayer();
        else
            musicSrv.resumePlayer();

        setFabDrawable();
    }

    @TargetApi(21)
    private void nowPlayingButtonPressed()
    {
        if(musicSrv == null || musicSrv.getCurrSong() == null)
        {
            Toast
                    .makeText(getApplicationContext(), "Please select song first.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Intent intent = new Intent(this, NowPlayingActivity.class);

        if(MainActivity.isLollipop())
        {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) fab, "fab"));
            startActivity(intent, options.toBundle());
        }
        else
            startActivity(intent);
    }

    @TargetApi(21)
    public void artistTrackPicked(int groupPostion, int childPosition)
    {
        Album currAlbum = currArtist.albums.get(groupPostion);
        musicSrv.setSong(childPosition);
        musicSrv.playAlbum(currAlbum, childPosition);
        setFabDrawable();

        Intent intent = new Intent(this, NowPlayingActivity.class);

        if(MainActivity.isLollipop()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create((View) fab, "fab"));
            startActivity(intent, options.toBundle());
        }
        else
            startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_artist, menu);
        return true;
    }

    @Override @TargetApi(21)
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_now_playing) {
            Intent intent = new Intent(this, NowPlayingActivity.class);

            if(MainActivity.isLollipop())
            {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                        Pair.create((View) fab, "fab"));
                startActivity(intent, options.toBundle());
            }
            else
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getActionBarHeight(){
        final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int result = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return result;
    }
}
