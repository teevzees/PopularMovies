package com.jefftiensivu.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jefftiensivu.popularmovies.model.Result;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final String PATH_PREFIX = "http://image.tmdb.org/t/p/w185";
    @Bind(R.id.movie_title) TextView textTitle;
    @Bind(R.id.movie_poster) ImageView imagePoster;
    @Bind(R.id.movie_year) TextView textYear;
    @Bind(R.id.movie_vote) TextView textVote;
    @Bind(R.id.movie_synopsis) TextView textSynopsis;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        Result result = null;
        try {
            result = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("DETAIL_RESULT"));
        }catch (ClassCastException e){
            Log.e(LOG_TAG, e.getMessage());
            return rootView;
        }

        if(result != null){
            if(result.getTitle() != null) {
                textTitle.setText(result.getTitle());
            }else{
                textTitle.setText("Loading Error");
            }
            Picasso.with(getActivity())
                    .load(PATH_PREFIX + result.getUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder_error)
                    .into(imagePoster);

            if(result.getReleaseDate() != null) {
                String[] parts = result.getReleaseDate().split("-");
                textYear.setText(parts[0]);
            }else{
                textYear.setText("Loading Error");
            }

            if(result.getVoteAverage() != null) {
                textVote.setText(result.getVoteAverage() + "/10");
            }else{
                textVote.setText("Loading Error");
            }

            if (result.getOverview() != null) {
                textSynopsis.setText(result.getOverview());
            }else{
                textSynopsis.setText("Loading Error");
            }
        }

        return rootView;
    }
}
