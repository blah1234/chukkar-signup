package com.defenestrate.chukkars.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface DisplayPageClientBundle extends ClientBundle
{
	@Source("com/defenestrate/chukkars/images/wait30trans.gif")
	ImageResource busyAnimatedIcon();

	@Source("com/defenestrate/chukkars/images/error.jpg")
	ImageResource errorIcon();
}
