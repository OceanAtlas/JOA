/*
 * $Id: ConfigCalcDialog.java,v 1.5 2004/09/14 19:11:26 oz Exp $
 *
 * This software is provided by NOAA for full, free and open release.  It is
 * understood by the recipient/user that NOAA assumes no liability for any
 * errors contained in the code.  Although this software is released without
 * conditions or restrictions in its use, it is expected that appropriate
 * credit be given to its author and to the National Oceanic and Atmospheric
 * Administration should the software be included by the recipient as an
 * element in other product development.
 */
package gov.noaa.pmel.nquery.ui;

/**
 * <code>CalculatedVariableNode</code> Class that represents a node in the
 * the JTree for a variable calculated from observed variables
 *
 * @author oz
 * @version 1.0
 */

public interface ConfigNQCalcDialog {
  abstract public Object getCalculation();

  abstract public Object getSpecification();
}
