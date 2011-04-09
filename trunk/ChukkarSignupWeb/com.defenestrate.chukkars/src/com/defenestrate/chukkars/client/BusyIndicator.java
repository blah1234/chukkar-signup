package com.defenestrate.chukkars.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BusyIndicator extends PopupPanel
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private Label _msgLabel;


	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public BusyIndicator()
	{
		layoutComponents();
	}

	
	///////////////////////////////// METHODS //////////////////////////////////
	private void layoutComponents()
	{
		this.setModal(true);
		
		_msgLabel = new Label("Loading...");
		_msgLabel.addStyleDependentName("busyLbl");

		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		mainPanel.addStyleName("dialogVPanel");
		
		DisplayPageClientBundle myImageBundle = GWT.create(DisplayPageClientBundle.class);
		ImageResource busyImgResource = myImageBundle.busyAnimatedIcon();
		
		mainPanel.add(_msgLabel);
		mainPanel.add( new Image(busyImgResource) );
		
		this.setWidget(mainPanel);
	}
	
	public void setMessageText(String txt)
	{
		_msgLabel.setText(txt);
	}
}
