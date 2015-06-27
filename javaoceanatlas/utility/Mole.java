/**
 * 
 */
package javaoceanatlas.utility;

import java.io.Serializable;
import java.util.Arrays;


/**
 * @author oz inspired by clint
 * 
 */
public enum Mole implements Unit<Mole>, Serializable {
  mole(1, "mol"),
  millimole(1.0E-3, "mmol"),
  micromole(1.0E-6, "umol"),
  picomole(1.0E9, "pmol");

  public final double factor;
  public final String symbol;
  public static final Mole base = mole;

  public Mole getBase() {
    return mole;
  }

  private Mole(double f, String s) {
    this.factor = f;
    this.symbol = s;
  }

  /* (non-Javadoc)
   * @see gov.noaa.tsunami.util.units.Unit#getSymbol()
   */
  public String getSymbol() {
    return symbol;
  }

  public double relationTo(Mole m) {
    return m == this ? 1 : factor / m.factor;
  }

  public String toString() {
    return symbol;
  }

  public double convertTo(Mole m, double d) {
    return m == this? d :d * relationTo(m);
  }
  
  public double[] copyConvertTo(Mole m, double[] d) {
    if (d == null) { return null; }
    final double[] ret = Arrays.copyOf(d, d.length);
    if (m != this) {
      final double rel = relationTo(m);
      for (int i = 0; i < ret.length; i++) {
        ret[i] *= rel;
      }
    }
    return ret;
  }

 

  /* (non-Javadoc)
   * @see gov.noaa.tsunami.utility.units.Unit#inPlaceConvertTo(gov.noaa.tsunami.utility.units.Unit, double[])
   */
  public double[] inPlaceConvertTo(Mole m, double[] d) {
    if (d == null || m == this) { return d; }
    final double rel = relationTo(m);
    for (int i = 0; i < d.length; i++) {
      d[i] *= rel;
    }
    return d;
  }
  
  /**
   * @param prop
   * @return
   */
  public static Mole fromSymbol(String prop) {
  	if ( prop == null ) { return null;}
    for (Mole m : values()) {
      if (m.symbol.equalsIgnoreCase(prop.trim()) || m.name().equalsIgnoreCase(prop.trim())) { return m; }
    }
    return null;
  }

  /**
   * @return the factor
   */
  public double getFactor() {
    return this.factor;
  }
}
