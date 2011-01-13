package com.defenestrate.chukkars.client;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link VerticalPanel} which has a permanent spacer at the end to prevent CSS collapse of the
 * panel and its parent.
 */
public class HorizontalPanelWithSpacer extends HorizontalPanel 
{
  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_SPACER = "demo-InsertPanelExample-spacer";

  public HorizontalPanelWithSpacer() {
    Label spacerLabel = new Label("");
    spacerLabel.setStylePrimaryName(CSS_DEMO_INSERT_PANEL_EXAMPLE_SPACER);
    super.add(spacerLabel);
  }

  @Override
  public void add(Widget w) {
    super.insert(w, getWidgetCount() - 1);
  }

  @Override
  public void insert(Widget w, int beforeIndex) {
    if (beforeIndex == getWidgetCount()) {
      beforeIndex--;
    }
    super.insert(w, beforeIndex);
  }

}