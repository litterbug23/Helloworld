package com.example.administrator.mapdev;

import java.util.EventListener;

public interface DrawEventListener extends EventListener {

	void handleDrawEvent(DrawEvent event);
}
