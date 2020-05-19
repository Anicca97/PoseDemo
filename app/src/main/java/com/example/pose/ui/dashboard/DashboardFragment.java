//package com.example.pose.ui.dashboard;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProviders;
//
//import com.example.pose.Main2Activity;
//import com.example.pose.R;
//
//public class DashboardFragment extends Fragment {
//
//    private DashboardViewModel dashboardViewModel;
//    private Button button;
//    private Button button2;
//    private Button button3;
//    private ImageView imageView;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        dashboardViewModel =
//                ViewModelProviders.of(this).get(DashboardViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//
//        imageView = root.findViewById(R.id.imageView_dashboard);
//
//        button = root.findViewById(R.id.button_dashboard);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), Main2Activity.class);
//                intent.putExtra("action","plank");
//                startActivity(intent);
//            }
//        });
//
//        button2 = root.findViewById(R.id.button2_dashboard);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), Main2Activity.class);
//                intent.putExtra("action","bend_over");
//                startActivity(intent);
//            }
//        });
//
//        button3 = root.findViewById(R.id.button3_dashboard);
//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), Main2Activity.class);
//                intent.putExtra("action","squat");
//                startActivity(intent);
//            }
//        });
//        return root;
//    }
//}
package com.example.pose.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.pose.Main2Activity;
import com.example.pose.R;

import java.util.LinkedList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    private List<Pose> mData = null;
    private Context mContext;
    private PoseAdapter mAdapter = null;
    private ListView list_pose;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mContext = getContext();
        list_pose = (ListView) root.findViewById(R.id.list_view);
        mData = new LinkedList<Pose>();
        mData.add(new Pose("平板支撑", "plank", R.drawable.plank));
        mData.add(new Pose("俯身伸展", "bend_over", R.drawable.bentover));
        mData.add(new Pose("靠墙静蹲", "squat", R.drawable.wallsquat));
        mAdapter = new PoseAdapter((LinkedList<Pose>) mData, mContext);
        list_pose.setAdapter(mAdapter);
        list_pose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String actNow = mData.get(position).getTxt();
                Intent intent = new Intent(getActivity(), Main2Activity.class);
                intent.putExtra("action", actNow);
                startActivity(intent);
            }
        });
        return root;
    }
}
