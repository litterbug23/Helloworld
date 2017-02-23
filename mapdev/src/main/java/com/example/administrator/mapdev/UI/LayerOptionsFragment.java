package com.example.administrator.mapdev.UI;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.mapdev.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LayerOptionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LayerOptionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LayerOptionsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment LayerOptionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LayerOptionsFragment newInstance() {
        LayerOptionsFragment fragment = new LayerOptionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LayerOptionsFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_layer_options, container, false);

    }



}
