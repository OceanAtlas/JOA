/**
 * 
 */
package javaoceanatlas.utility;

/**
 * @author oz
 *
 */

public enum Direction {
  UP ("Up"),
  DOWN ("Down");
  
	private final String mReadableName;
  
	Direction(String dName)  {
  	mReadableName = dName;
  }
  
  public String toString() {
  	return mReadableName;
  }
  
  public static Direction fromString(String filStr) {
    for (Direction direction : values()) {
      if (direction.mReadableName.equals(filStr)) {
        return direction;
      }
    }
    return DOWN;
  }
}
