package com.example.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private int mLastPosition=0;
    private boolean mUserScrolled=true;


    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItems> mItems = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_galley, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            GridLayoutManager manager = (GridLayoutManager)mPhotoRecyclerView
                    .getLayoutManager();

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mLastPosition = manager.findLastVisibleItemPosition();
                if (!mUserScrolled) {
                    if (mLastPosition == mItems.size() - 1) {
                        Toast.makeText(getActivity(), "Bottom", Toast.LENGTH_SHORT).show();
                        new FetchItemsTask().execute();
                    }
                }

            }});

        // reconfigured with an appropriate adapter
        setupAdapter();

        return v;
    }

    //Look the current model state,namely the List of GalleryItems
    // and configures the adapter appropriately on your RecyclerView.
    private void setupAdapter() {
        //This confirms that the fragment has been attached
        // an activity,and in turn that getActivity() will not be null
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private final TextView mTitleTextView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItems items) {
            mTitleTextView.setText(items.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItems> mGalleryItems;

        public PhotoAdapter(List<GalleryItems> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItems galleryItems = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItems);

        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItems>> {
        @Override
        protected List<GalleryItems> doInBackground(Void... voids) {
            new FlickFetchr().fetchItems();
            return new FlickFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItems> galleryItems) {
            mItems.addAll(galleryItems);
            mUserScrolled = false;
            setupAdapter();
        }
}}
