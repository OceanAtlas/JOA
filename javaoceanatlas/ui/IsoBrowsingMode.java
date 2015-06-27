/**
 * 
 */
package javaoceanatlas.ui;

/**
 * @author oz
 *
 */
public enum IsoBrowsingMode {
  STN_SYMBOL("Station Symbol"),
  OVERLAY_CONTOUR("Contour");
  private final String label;

  IsoBrowsingMode(String label) {
    this.label = label;
  }

  public String toString() {
    return label;
  }
}
