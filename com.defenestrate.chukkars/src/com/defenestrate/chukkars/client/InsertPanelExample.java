package com.defenestrate.chukkars.client;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.HorizontalPanelDropController;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;

/**
 * Example of columns that can be rearranged, with widget that can be moved within a column or
 * between columns.
 */
public final class InsertPanelExample extends Example 
{
  private static final int COLUMNS = 4;

  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE = "demo-InsertPanelExample";

  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_COLUMN_COMPOSITE = "demo-InsertPanelExample-column-composite";

  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER = "demo-InsertPanelExample-container";

  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_HEADING = "demo-InsertPanelExample-heading";

  private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_WIDGET = "demo-InsertPanelExample-widget";

  private static final int ROWS = 3;

  private static final int SPACING = 0;

  public InsertPanelExample(final DemoDragHandler demoDragHandler) {
    addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE);
    int count = 0;

    // use the boundary panel as this composite's widget
    AbsolutePanel boundaryPanel = new AbsolutePanel();
    boundaryPanel.setSize("100%", "100%");
    setWidget(boundaryPanel);

    // initialize our row drag controller
    PickupDragController rowDragController = new PickupDragController(boundaryPanel, false);
    rowDragController.setBehaviorMultipleSelection(false);
    rowDragController.addDragHandler(demoDragHandler);

    // initialize our widget drag controller
    PickupDragController widgetDragController = new PickupDragController(boundaryPanel, false);
    widgetDragController.setBehaviorMultipleSelection(false);
    widgetDragController.addDragHandler(demoDragHandler);

    // initialize vertical panel to hold our rows
    VerticalPanel vertPanel = new VerticalPanel();
    vertPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER);
    vertPanel.setSpacing(SPACING);
    boundaryPanel.add(vertPanel);

    // initialize our column drop controller
    VerticalPanelDropController rowDropController = new VerticalPanelDropController(
        vertPanel);
    rowDragController.registerDropController(rowDropController);
    
    createHeaderRow(vertPanel);

    for(int row = 1; row <= ROWS; row++) 
    {
    	createRow(vertPanel, row, count, widgetDragController, rowDragController);
    }
  }
  
  private void createRow(VerticalPanel vertPanel, 
		  				 int row, 
		  				 int count,
		  				 PickupDragController widgetDragController, 
		  				 PickupDragController rowDragController)
  {
	  // initialize a horizontal panel to hold the heading and a second horizontal
      // panel
      HorizontalPanel rowCompositePanel = new HorizontalPanel();
      rowCompositePanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_COLUMN_COMPOSITE);

      // initialize inner horizontal panel to hold individual widgets
      HorizontalPanel horizPanel = new HorizontalPanelWithSpacer();
      horizPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER);
      horizPanel.setSpacing(SPACING);
      vertPanel.add(rowCompositePanel);
      //TODO: Shawn's debug
      horizPanel.setTitle("row " + row);

      // initialize a widget drop controller for the current row
      HorizontalPanelDropController widgetDropController = new HorizontalPanelDropController(
          horizPanel)
      {
    	  public void onPreviewDrop(DragContext context) throws VetoDragException  
    	  {
    		  if( !((Widget)dropTarget).getTitle().equals(context.draggable.getTitle()) )
    		  {
    			  throw new VetoDragException();
    		  }
    		  else
    		  {
    			  super.onPreviewDrop(context);
    		  }
    	  }
      };
      widgetDragController.registerDropController(widgetDropController);

      // Put together the column pieces
      Label heading = new Label("Row " + row);
      heading.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_HEADING);
      rowCompositePanel.add(heading);
      rowCompositePanel.add(horizPanel);

      // make the row draggable by its heading
      rowDragController.makeDraggable(rowCompositePanel, heading);

      for(int col = 1; col <= COLUMNS; col++) 
      {
        // initialize a widget
        HTML widget = new HTML("Draggable&nbsp;#" + ++count);
        widget.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_WIDGET);
        widget.setHeight(4 + "em");
        
        VerticalPanel widgetPanel = new VerticalPanel();
        widgetPanel.add(widget);
        widgetPanel.add( new ToggleButton(" ", "X") );
        horizPanel.add(widgetPanel);
        
        widgetPanel.setTitle( horizPanel.getTitle() );

        // make the widget draggable
        widgetDragController.makeDraggable(widgetPanel, widget);
      }
  }
  
  private void createHeaderRow(VerticalPanel vertPanel)
  {
	  // initialize a horizontal panel to hold the heading and a second horizontal
      // panel
      HorizontalPanel rowCompositePanel = new HorizontalPanel();
      rowCompositePanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_COLUMN_COMPOSITE);

      // initialize inner horizontal panel to hold individual widgets
      HorizontalPanel horizPanel = new HorizontalPanelWithSpacer();
      horizPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER);
      horizPanel.setSpacing(SPACING);
      vertPanel.add(rowCompositePanel);

      // Put together the column pieces
      TextBox nameBox = new TextBox();
      nameBox.addStyleName("demo-InsertPanelExample-add-heading");
      
      Button addBtn = new Button("+");
      HorizontalPanel headingPanel = new HorizontalPanel();
      headingPanel.add(nameBox);
      headingPanel.add(addBtn);
      headingPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_HEADING);
      
      rowCompositePanel.add(headingPanel);
      rowCompositePanel.add(horizPanel);

      
      for(int col = 1, count=0; col <= COLUMNS; col++) 
      {
    	  // initialize a widget
    	  Label widget = new Label( Integer.toString(++count) );
    	  widget.setHeight(4 + "em");
        
    	  Button minusBtn = new Button("-");
        
    	  headingPanel = new HorizontalPanel();
    	  headingPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
    	  headingPanel.add(widget);
    	  headingPanel.add(minusBtn);
    	  headingPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_WIDGET);
        
    	  horizPanel.add(headingPanel);
      }
  }

  @Override
  public String getDescription() {
    return "Allows drop to occur anywhere among the children of a supported <code>InsertPanel</code>.";
  }

  @Override
  public Class<?>[] getInvolvedClasses() {
    return new Class[] {
        InsertPanelExample.class, VerticalPanelDropController.class, VerticalPanelWithSpacer.class,
        HorizontalPanelDropController.class,
        PickupDragController.class,};
  }
}
