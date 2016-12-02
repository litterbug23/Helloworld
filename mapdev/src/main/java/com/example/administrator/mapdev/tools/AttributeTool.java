package com.example.administrator.mapdev.tools;

import android.app.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.esri.core.geometry.Point;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.query.QueryParameters;
import com.example.administrator.mapdev.LayersManager;
import com.example.administrator.mapdev.R;

/**
 * Created by Administrator on 2016/4/25.
 */
public class AttributeTool extends BaseTool {
    private PopupContainer popupContainer;
    public static final int ATTRIBUTE_SHOW = 1;
    public static final int ATTRIBUTE_EDIT = 2;

    public AttributeTool(LayersManager layersManager) {
        super(layersManager);
        popupContainer = new PopupContainer(mapView);
    }

    @Override
    public void activate(int toolType) {
        super.activate(toolType);
        mapOnTouchListener = new SingleTapListener(this.mapView.getContext(), this.mapView);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    class SingleTapListener extends MapOnTouchListener {
        private static final long serialVersionUID = 1L;
        AttributeListAdapter listAdapter;

        public SingleTapListener(Context context, MapView mapView) {
            super(context, mapView);
        }

        @Override
        public boolean onDoubleTap(MotionEvent point) {
            if(queryAttribute(point))
                return true;
            return super.onDoubleTap(point);
        }

        @Override
        public boolean onSingleTap(MotionEvent point) {
            if(queryAttribute(point))
                return true;
            return super.onSingleTap(point);
        }

        private boolean queryAttribute(MotionEvent point){
            if (mapView.isLoaded()) {
                Layer[] layers = mapView.getLayers();
                QueryParameters queryParameters = new QueryParameters();
                Point queryPoint = mapView.toMapPoint(point.getX(), point.getY());
                queryParameters.setGeometry(queryPoint);
                queryParameters.setSpatialRelationship(SpatialRelationship.INTERSECTS);
                for (Layer layer : layers) {
                    if (!layer.isInitialized() || !layer.isVisible())
                        continue;
                    if (layer instanceof FeatureLayer) {
                        FeatureLayer featureLayer = (FeatureLayer) layer;
                        listAdapter = new AttributeListAdapter(mapView.getContext(), featureLayer, "");
//                        featureLayer.selectFeatures(queryParameters, FeatureLayer.SelectionMode.NEW, new CallbackListener<FeatureResult>() {
//                            @Override
//                            public void onCallback(FeatureResult objects) {
//                                if (objects.featureCount() == 0)
//                                    return;
//                                FeatureSet featureSet = new FeatureSet();
//                                featureSet.setFields(objects.getFields());
//                                Graphic[] graphics = new Graphic[(int) objects.featureCount()];
//                                Iterator iterator = objects.iterator();
//                                int id = 0;
//                                while (iterator.hasNext()) {
//                                    Feature feature = (Feature) iterator.next();
//                                    Log.d("Test", feature.toString());
//                                    graphics[id++] = new Graphic(feature.getGeometry(), feature.getSymbol(), feature.getAttributes());
//                                }
//                                featureSet.setGraphics(graphics);
//                                listAdapter.setFeatureSet(featureSet);
//                                PopupDialogEx popupDialog = new PopupDialogEx(mapView.getContext(),
//                                        listAdapter);
//                                popupDialog.show();
//                            }
//
//                            @Override
//                            public void onError(Throwable throwable) {
//                            }
//                        });

                        long[] featureIDs = featureLayer.getFeatureIDs(point.getX(), point.getY(), 20, 1);
                        featureLayer.selectFeatures(featureIDs,true);
                        FeatureSet featureSet = new FeatureSet();
                        featureSet.setFields(featureLayer.getFeatureTable().getFields());
                        Graphic[] graphics = new Graphic[featureIDs.length];
                        for(long id=0;id<featureIDs.length;id++){
                            Feature feature=featureLayer.getFeature(id);
                            graphics[(int)id] = new Graphic(feature.getGeometry(), feature.getSymbol(), feature.getAttributes());
                        }
                        featureSet.setGraphics(graphics);
                        listAdapter.setFeatureSet(featureSet);

                        PopupDialogEx popupDialog = new PopupDialogEx(mapView.getContext(),
                                listAdapter);
                        popupDialog.show();

//                        long[] featureIDs = featureLayer.getFeatureIDs(point.getX(), point.getY(), 20, 1);
//                        // Instantiate a PopupContainer
//                        popupContainer = new PopupContainer(mapView);
//                        // Add Popup
//                        for (long id : featureIDs) {
//                            Feature feature = featureLayer.getFeature(id);
//                            Popup popup = featureLayer.createPopup(mapView, 0, feature);
//                            //popup.setEditMode(true);
//                            popupContainer.addPopup(popup);
//                        }
//                        //createEditorBar(featureLayer, false);
//                        // Create a dialog for the popups and display it.
//                        PopupDialog popupDialog = new PopupDialog(mapView.getContext(),
//                                popupContainer);
//                        popupDialog.show();
                    }
                }
                return true;
            }
            return false;
        }
    }

    class PopupDialogEx extends Dialog {
        private AttributeListAdapter listAdapter;

        public PopupDialogEx(Context context, AttributeListAdapter listAdapter) {
            super(context, R.style.AppDialogTheme);
            this.listAdapter = listAdapter;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT);
//            LinearLayout layout = new LinearLayout(getContext());
//            if (listView == null)
//                listView = new ListView(getContext());
//            layout.addView(listView,
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT);
//            setContentView(layout, params);
            setContentView(R.layout.list_layout);
            ListView listView = (ListView) findViewById(R.id.attribute_list_view);
            listView.setAdapter(this.listAdapter);
            this.listAdapter.notifyDataSetChanged();
        }

    }

    // A customize full screen dialog.
    class PopupDialog extends Dialog {
        private PopupContainer pContainer;

        public PopupDialog(Context context, PopupContainer popupContainer) {
            super(context, android.R.style.Theme);
            this.pContainer = popupContainer;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout layout = new LinearLayout(getContext());
            layout.addView(pContainer.getPopupContainerView(),
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            setContentView(layout, params);
        }

    }
}
