/*
 * $Id: NdEditResources.java,v 1.18 2005/08/22 21:25:16 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */

package ndEdit;

import java.util.*;

/**
 * Resource Bundle of menu item tokens and associated
 * locale-specific labels.
 *
 * @author oz
 * @version 1.0
 */

public class NdEditResources extends ListResourceBundle {
		public Object[][] getContents() {
			return contents;
 		}

 		static final Object[][] contents = {
 			// American English Menu Strings
 			{"kOK", "OK"},
 			{"kCancel", "Cancel"},
 			{"kFile", "File"},
 			{"kEdit", "Edit"},
 			{"kPreferences", "Preferences..."},
 			{"kView", "View"},
 			{"kZoom", "Zoom"},
 			{"kWindow", "Window"},
 			{"kHelp", "Help..."},
 			{"kHelp2", "Help"},
 			{"kBrowseNetCDF", "Browse netCDF File..."},
 			{"kAppend", "Append Pointer Collection..."},
 			{"kSave", "Save"},
 			{"kSaveAs", "Save Selection As..."},
 			{"kQuit", "Quit"},
 			{"kExit", "Exit"},
 			{"kOptions", "Options..."},
 			{"kUndoAll", "Undo All"},
 			{"kUndo", "Undo"},
 			{"kRedo", "Redo"},
 			{"kLongitude", "Longitude"},
 			{"kRetainStns", "Retain Stations in Filter Region"},
 			{"kDeleteStns", "Remove Stations in Filter Region"},
 			{"kSelectAll", "Zoom Filter Region to Axes Ranges"},
 			{"kShowSelRgn", "Show Selected region"},
 			{"kShowAllData", "Show All Stations"},
 			{"kCtrOfSelRgn", "Center of selection region"},
 			{"kLatLon", "Latitude-Longitude (y-x)"},
 			{"kLonTim", "Longitude-Time (x-t)"},
 			{"kDepLon", "Depth-Longitude (z-x)"},
 			{"kDepTim", "Depth-Time (z-t)"},
 			{"kDepLat", "Depth-Latitude (z-y)"},
 			{"kLatTim", "Latitude-Time (y-t)"},
 			{"kZoomSelRgn", "Zoom Axes to Filter Region"},
 			{"kResetZoom", "Reset Zoom"},
 			{"kAbout", "About NdEdit..."},
 			{"kNews", "NdEdit News..."},
 			{"kSystemProperties", "System Properties..."},
 			{"kOptions", "Options"},
 			{"kColors", "Colors"},
 			{"kColor", "Color"},
 			{"kCoastline", "Coastline:"},
 			{"kLegend", "Legend"},
 			{"kColorPalettes", "Color palettes:"},
 			{"kPick", "Pick..."},
 			{"kBlend", "Blend"},
 			{"kSave", "Save..."},
 			{"kNew", "New..."},
 			{"kApply", "Apply"},
 			{"kDescription", "Description"},
 			{"kParameter2", "Parameter:"},
 			{"kTitle", "Title:"},
 			{"kStartValue", "Start value:"},
 			{"kEndValue", "End value:"},
 			{"kDelete", "Delete"},
 			{"kDone", "Done"},
 			{"kNone", "None"},
 			{"kMinimum", "Minimum:"},
 			{"kMaximum", "Maximum:"},
 			{"kOther", "Other"},
 			{"kSymbol", "Symbol:"},
 			{"kSize", "Size:"},
 			{"kPrint", "Print..."},
 			{"kSorting", "Sorting"},
 			{"kSort", "Sort"},
 			{"kDate:", "Date:"},
 			{"kLongitudeColon", "Longitude:"},
 			{"kBottom", "Bottom:"},
 			{"kMM", "MM:"},
 			{"kDD", "DD:"},
 			{"kYYYY", "YYYY:"},
 			{"kHH", "HH"},
 			{"kMM.M", "MM.M:"},
 			{"kLon", "Lon:"},
 			{"kLat", "Lat:"},
 			{"kUnits", "Units:"},
 			{"kName", "Name:"},
 			{"kWidth", "Width:"},
 			{"kKM", "(KM)"},
 			{"kSaveSelAsEPIC", "Save Selection as EPIC Pointer File..."},
 			{"kSaveSelAsArgo", "Save Selection as NODC Inventory/Location file..."},
 			{"kLatitude", "Latitude"},
 			{"kLongitude", "Longitude"},
 			{"kDate", "Date"},
 			{"kDepth", "Depth"},
 			{"kDepth:", "Depth:"},
 			{"kAscending", "Ascending"},
 			{"kDescending", "Descending"},
 			{"kSortOrder", "Sort order:"},
 			{"kPointerSort", "Pointer Sorting (optional)"},
 			{"kNewBrowser", "Browse File in New Window..."},
 			{"kTime", "Time"},
 			{"kClose", "Close"},
 			{"kCoastline", "Coastline"},
 			{"kBathymetry", "Bathymetry"},
 			{"kOverlay", "Overlay"},
 			{"kMapSettings", "Map Settings..."},
 			{"kBuiltin", "Built in"},
 			{"kCustom", "Custom:"},
 			{"kBrowseNetCDF", "Browse Pointer, Inventory, or netCDF File..."},
 			{"kTime", "Time:"},
 			{"kZ", "Z:"},
 			{"kRegrid", "Regrid..."},
 			{"kVariables", "Variables"},
 			{"kDimensions", "Dimensions"},
 			{"kNdEditAxes", "NdEdit Axes"},
 			{"kNdEditView", "NdEdit View"},
 			{"kName2", "Name"},
 			{"kAutoMap", "Auto Map"},
 			{"kNewName", "New Name:"},
 			{"kOriginalRange", "Original Range:"},
 			{"kNumPoints", "N = "},
 			{"kLinearTransform", "Linear Transform"},
 			{"kManualTransform", "Specify Individual Values"},
 			{"kNewRange", "New Range: "},
 			{"kOriginalName", "Original Name:"},
 			{"kOriginalUnits", "Original Units:"},
 			{"kNewUnits", "New Units:"},
 			{"kTransformFor", "Transform for"},
 			{"kAxis", "Axis"},
 			{"kName2", "Name"},
 			{"kIndex", "Index"},
 			{"kValue2", "Value"},
 			{"kNewValue", "New Value"},
 			{"kDelta", "Delta:"},
 			{"kApplyTransform", "Apply Transform"},
 			{"kMinErr", " start value "},
 			{"kValErr", "An error occured getting "},
 			{"kDeltaErr", " delta "},
 			{"kMaxErr", " end value "},
 			{"kto", " to "},
 			{"kTransform", "Transform"},
 			{"kExtract", "Browse"},
 			{"kRegridTo", "Regrid To:"},
 			{"kNoVariablesSelected", "No Variables Selected"},
 			{"kVariablesOnSameGrid", "On same grid"},
 			{"kIsTime", "Is time axis"},
 			{"kIsClimatology", "Is climatology"},
 			{"kStartTime", "Start Time:"},
 			{"kSetStartTime", "Set Start Time"},
 			{"kYears", "Years"},
 			{"kMonths", "Months"},
 			{"kDays", "Days"},
 			{"kHours", "Hours"},
 			{"kMinutes", "Minutes"},
 			{"kSeconds", "Seconds"},
 			{"kMulipleGridError", "Multiple Grids Found"},
 			{"kMulipleGridPromptLine1", "The selected variables are not on the same grid."},
 			{"kMulipleGridPromptLine2", "Select the variable to use for regridding."},
 			{"kVariablesOnSameGrid", "On Same Grid"},
 			{"kTreeView", "Tree View..."},
 			{"kTableView", "Table View..."},
 			{"kConvertLon", "0:360 -> -180:180"},
 			{"kClear", "Clear"},
 			{"kMin2", "Min:"},
 			{"kMax2", "Max:"},
 			{"kInvLbl", "total/visible/selected:"},
 			{"kNameOfPtrFile", "Name of Pointer File:"},
 			{"kNameOfInvFile", "Name of Inventory File:"},
 			{"kNameOfArgoLocFile", "Name of Location File:"},
 			{"kNumYears", "Length (years):"},
 			{"kLengthErr", " Length of climatology is invalid"},
 			{"kClimatologyError", "Error specifying climatology"},
 			{"kResetAll", "Reset All"},
 			{"kChoosePrefs", "Cut Panel Preferences"},
 			{"kPanelSize", "Panel Size:"},
 			{"kIndHandles", "Independent filter handles"},
 			{"kDisplayAxes", "Display axes"},
 			{"kDatasetName", "Dataset Name:"},
 			{"kCoastColor", "Coastline color: "},
 			{"kCoastWeight", "Line weight: "},
 			{"kSelectionInspector", "Selection Inspector..."},
 			{"kMin2", "Min:"},
 			{"kMax2", "Max:"},
 			
			{"kQBSetupHelp",
						   "The Quality Byte Setup Feature is used to assign a full column of default " +
						   "quality bytes for a parameter if a column of quality bytes for the parameter" +
						   "in question is not already present." +
					 	  "\n" +
					 	  "\n" +
						 "Check all the boxes where you wish default values of Quality Byte 1 " +
						 "and/or Quality Byte 2 added to the file. If a quality byte is already " +
						 "present, no change will be made. The default values for QB 1 are 2 " +
						 "where there are no data reported for that parameter. The default values " +
						 "for QB 2 is the existing or new value of QB 1. Note that QB 2 can only  " +
						 "be added only when QB 1 is either already present or is generated here. "},
 			
			{"kQBEditorHelp",
						   "The Quality Byte Editor Feature is used to assign or change the value " +
						   "of individual values of Quality Byte 1 (assigned by the data originator) " +
						   "or Quality Byte 2 (assigned by WOCE data quality expert.)" +
					 	  "\n" +
					 	  "\n" +
						 "Click the Setup button to establish which parameters, and how many lines of " +
						 "the data will be displayed on the screen. Click the Find button to move around " +
						 "in the data set. " +
					 	  "\n"}
		};
}
