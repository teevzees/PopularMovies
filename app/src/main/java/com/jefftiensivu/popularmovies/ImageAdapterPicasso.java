package com.jefftiensivu.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Created by jeff on 9/28/2015.
 * This class was copied from the Android Developers Website
 * http://developer.android.com/guide/topics/ui/layout/gridview.html
 * I modified it to use Picasso.
 * Picasso error tracing from,
 * http://stackoverflow.com/questions/25744344/android-picasso-load-image-failed-how-to-show-error-message
 * If I had more time I'd work on that more.
 */
public class ImageAdapterPicasso extends BaseAdapter {
    private final String PATH_PREFIX = "http://image.tmdb.org/t/p/w185";

    private Context mContext;

    // reference to our images
    private String[] mThumbUrls;


    public ImageAdapterPicasso(Context c, String[] tu) {
        mContext = c;
        mThumbUrls = tu;
    }


    public int getCount() {
        if(mThumbUrls != null){
            return mThumbUrls.length;
        }else{
            Toast.makeText(mContext, "Please, connect device to Internet", Toast.LENGTH_LONG).show();
            return 0;
            //TODO I'd like to make this more robust. Perhaps, show a 404 type error in a text view.
        }
    }


    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext)
                .load(PATH_PREFIX + mThumbUrls[position])
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder_error)
                .into(imageView);
/*
        //This was used instead of the above command for finding an error.
        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener(new Picasso.Listener()
        {
            @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
            {
                Log.e("Picasso", "" + uri + "   " + exception);
                exception.printStackTrace();
            }
        });
        builder.build().load(url).into(imageView);
*/
        //TODO implement above


        return imageView;
    }
}
