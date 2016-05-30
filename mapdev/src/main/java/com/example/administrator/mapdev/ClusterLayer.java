package com.example.administrator.mapdev;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;

/**
 * 离散点聚类(当点集密度过大时，使用聚类进行表现会比较清晰一些）
 * Created by Administrator on 2016/4/3.
 */
public class ClusterLayer extends GraphicsLayer {
	public ClusterLayer() {
		super();
	}

	public ClusterLayer(RenderingMode mode) {
		super(mode);
	}

	public ClusterLayer(MarkerRotationMode rotationMode) {
		super(rotationMode);
	}

	protected ClusterLayer(boolean initLayer) {
		super(initLayer);
	}

	protected ClusterLayer(long handle) {
		super(handle);
	}

	public ClusterLayer(SpatialReference sr, Envelope fullextent) {
		super(sr, fullextent);
	}

	public ClusterLayer(SpatialReference sr, Envelope fullextent, RenderingMode mode) {
		super(sr, fullextent, mode);
	}

	@Override
	protected long create() {
		return super.create();
	}

	@Override
	protected void initLayer() {
		super.initLayer();
	}

	@Override
	public void removeAll() {
		super.removeAll();
	}
}
