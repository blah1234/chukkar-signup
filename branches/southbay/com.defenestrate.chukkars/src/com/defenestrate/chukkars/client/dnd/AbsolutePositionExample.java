/*
 * Copyright 2009 Fred Sauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.defenestrate.chukkars.client.dnd;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;
import com.defenestrate.chukkars.shared.Day;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController} example.
 */
public final class AbsolutePositionExample extends Example {

  private static final String CSS_DEMO_ABSOLUTE_POSITION_EXAMPLE = "demo-AbsolutePositionExample";
  public static final int DROP_TARGET_WIDTH = 800;
  public static final int DROP_TARGET_HEIGHT = 200;

  private AbsolutePositionDropController absolutePositionDropController;
  private AbsolutePanel positioningDropTarget;


  /**
   * Basic constructor.
   *
   * @param dragController the drag controller to use
   */
  public AbsolutePositionExample(PickupDragController dragController) {
    super(dragController);
    addStyleName(CSS_DEMO_ABSOLUTE_POSITION_EXAMPLE);

    // use the drop target as this composite's widget
    positioningDropTarget = new AbsolutePanel();
    positioningDropTarget.setPixelSize(DROP_TARGET_WIDTH, DROP_TARGET_HEIGHT);
    setWidget(positioningDropTarget);

    // instantiate our drop controller
    absolutePositionDropController = new AbsolutePositionDropController(positioningDropTarget);
    dragController.registerDropController(absolutePositionDropController);
  }

  @Override
  public String getDescription() {
    return "Draggable widgets can be placed anywhere on the gray drop target.";
  }

  @Override
  public Class<?>[] getInvolvedClasses() {
    return new Class[] {AbsolutePositionExample.class, AbsolutePositionDropController.class,};
  }

  	@Override
  	protected void onInitialLoad() {
	    Day[] allDays = Day.getAll();
	    int activeX = 10;

	    for(Day currDay : allDays)
	    {
	    		Widget draggable;

	    		if( currDay.isEnabled() )
	    		{
	    			draggable = createDraggable( currDay.toString() );

		    		absolutePositionDropController.drop(draggable, activeX, 30);

		    		activeX += draggable.getOffsetWidth();
		    		activeX += 10;
	    		}
	    }
  	}

  	public List<Widget> getAllWidgetsInDropTarget()
  	{
  		ArrayList<Widget> ret = new ArrayList<Widget>( positioningDropTarget.getWidgetCount() );

  		for(int i=0, n=positioningDropTarget.getWidgetCount(); i<n; i++)
  		{
  			ret.add( positioningDropTarget.getWidget(i) );
  		}

  		return ret;
  	}
}
