package com.example.administrator.mapdev.UI;


import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.mapdev.R;

/**
 * A simple {@link DialogFragment} subclass.
 */
public class AttributeEditorFragment extends DialogFragment   {


    public AttributeEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_layout, container,
                false);
        return view;
    }


}
