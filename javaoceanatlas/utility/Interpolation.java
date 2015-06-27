/**
 * 
 */
package javaoceanatlas.utility;

import java.io.File;
import java.io.IOException;
import javaoceanatlas.classicdatamodel.Station;

/**
 * @author oz
 * 
 */
public interface Interpolation {
	public static int NO_MISSING_INTERPOLATION = 0;
	public static int LOCAL_INTERPOLATION = 1;
	public static int FAR_FIELD_INTERPOLATION = 2;
	public static int ZGRID_INTERPOLATION = 3;
	public double[][] getValues();
	public double[] getDistValues();
	public double[] getBottomDepths();
	public double[] getLatValues();
	public double[] getLonValues();
	public String[] getStnValues();
	public double getMaxValue();
	public double getMinValue();
	public NewInterpolationSurface getSurface();
	public String getParamName();
	public int getSurfParamNum();
	public String getName();
	public JOAVariable getParam();
	public int getLevels();
	public int getNumStns();
	public void writeToLog(String preamble) throws IOException;
	public void dereference();
	public boolean isResidualInterp();
	public int getInterpolationType();
	public void computeHorzGradient(boolean isVelocity);
	public void setName(String s);
	public boolean isBelowBottom(int lvl, double stnDepth);
	public void doInterp(boolean redim);
	public double getTotalDistance();
  public double getTotalLatitude();
  public double getTotalLongitude();
  public double getTotalTime();
  public int getNumUsedStations();
  public double getDX();
  public double getClosestValue(int level, Station sh);
  public boolean isLocked();
  public void setLocked(boolean b);
  public void exportJSON(File f);
}
