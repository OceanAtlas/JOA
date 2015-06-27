/**
 * 
 */
package javaoceanatlas.utility;


import java.io.Serializable;

/**
 * @author clint pells
 * An interface for units
 */

public interface Unit<T extends Unit<T>> extends Serializable {
  
  /**
   * A typical symbol for the unit ex: meter => m, second => sec
   * @return
   */
  public String getSymbol();

  /**
   * The SI base type or a reference type.  Other units "factors" are stated in relationship
   * to this unit.  For example Length has a base unit of meter.  The factor of meter = 1, the factor of millimeter = .001, an inch = .0254.
   * @return
   */
  public T getBase();
  
  /**
   * This is the numerical relationship of this unit to the base unit.  The base unit is always 1. Smaller units will have smaller factors.
   * @return
   */
  public double getFactor();

  /**
   * The numeric relationship between another unit and this.  For example Length.meter.relationTo( Length.millimeter ) = 1000.  A Unit compared to itself = 1;
   * @param t the Unit to which this unit is compared
   * @return
   */
  public double relationTo(T t);

  /**
   * Convert the value d assumed to be in this unit to the unit t.  For Example Length.meter.convertTo( Length.millimeter, 1 ) = 1000;
   * If the target unit == this unit the original value is returned.
   * @param t the target unit
   * @param d the value in terms of this unit
   * @return a value in terms of the target unit
   */
  public double convertTo(T t, double d);
  
  /**
   * Like convertTo( T, double ) except for an array.  This copies the values to a new array which is returned.  
   * If the target unit == this unit a copy of the input array is returned.
   * @param t
   * @param d
   * @return
   */
  public double[] copyConvertTo(T t, double[] d);

  
  /**
   * Like convertTo( T, double[] ) except the values in the input array are replace with the converted values.  This returns the input array.  
   * If the target unit == this no conversion is done.
   * @param t
   * @param d
   * @return
   */
  public double[] inPlaceConvertTo(T t, double[] d);

  /**
   * So that name() can be called on the Unit interface
   * @return
   */
  public String name();
  
  
  
  
}
