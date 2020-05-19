package com.example.pose.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.pose.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private VideoView videoView;
    private MediaController mediaController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        videoView = (VideoView) root.findViewById(R.id.videoView_home);
        mediaController = new MediaController(getContext());
        String video_uri = "android.resource://" + getContext().getPackageName() + "/"  + R.raw.introduction;
        Uri uri = Uri.parse(video_uri);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.start();
        return root;
    }
}
