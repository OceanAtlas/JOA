//
//  OpenForecast - open source, general-purpose forecasting package.
//  Copyright (C) 2002-2004  Steven R. Gould
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package net.sourceforge.openforecast;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import net.sourceforge.openforecast.models.MovingAverageModel;
import net.sourceforge.openforecast.models.MultipleLinearRegressionModel;
import net.sourceforge.openforecast.models.PolynomialRegressionModel;
import net.sourceforge.openforecast.models.RegressionModel;
import net.sourceforge.openforecast.models.SimpleExponentialSmoothingModel;
import net.sourceforge.openforecast.models.DoubleExponentialSmoothingModel;
import net.sourceforge.openforecast.models.TripleExponentialSmoothingModel;



/**
 * The Forecaster class is a factory class that obtains the best
 * ForecastingModel for the given data set. The best forecasting model is
 * defined as the one that gives the lowest sum of absolute errors (SAE)
 * when reapplying the model to the historical or observed data.
 * @author Steven R. Gould
 */
public class Forecaster
{
		private static String fcOutput = "";
    /**
     * Make constructor private to prevent this class from being instantiated
     * directly.
     */
    private Forecaster()
    {
    }
    
    /**
     * Obtains the best forecasting model for the given DataSet. There is
     * some intelligence built into this method to help it determine which
     * forecasting model is best suited to the data. In particular, it will
     * try applying various forecasting models, using different combinations
     * of independent variables and select the one with the least Sum of
     * Absolute Errors (SAE); i.e. the most accurate one based on historical
     * data.
     * @param dataSet a set of observations on which the given model should be
     *        based.
     * @return the best ForecastingModel for the given data set.
     */
    public static ForecastingModel getBestForecast( DataSet dataSet, Vector<ForecastingModel> allModels)
    {
        String independentVariable[] = dataSet.getIndependentVariables();
        ForecastingModel bestModel = null;
        String bestRegressionVariable = null;
        fcOutput = System.getProperty("line.separator");
        
        // Try single variable models
        for ( int i=0; i<independentVariable.length; i++ )
            {
                // Try the Regression Model
                ForecastingModel model1 = new RegressionModel( independentVariable[i] );
                allModels.add(model1);
                fcOutput += ("Setting up single variable linear regression model for " + independentVariable[i] + System.getProperty("line.separator"));
                model1.init( dataSet );
                fcOutput +=  ("Computed model is: " + model1.toString() + System.getProperty("line.separator"));
                fcOutput +=  ("Comparing model to Best Model" + System.getProperty("line.separator"));
                if ( betterThan( model1, bestModel ) )
                    {
                        bestModel = model1;
                        bestRegressionVariable = independentVariable[i];
                        fcOutput += ("!! Best model changed to: " + bestModel.toString() + System.getProperty("line.separator"));
                    }
                
                // Try the Polynomial Regression Model
                // Note: if order is about the same as dataSet.size() then
                //  we'll get a good/great fit, but highly variable forecasts
                int order = 5;
                if ( dataSet.size()/2 < order )
                    order = dataSet.size()/2;
                ForecastingModel model2 = new PolynomialRegressionModel( independentVariable[i],
                                                       order );
                allModels.add(model2);
                fcOutput += ("Setting up single variable polynomial regression model for " + independentVariable[i] + System.getProperty("line.separator"));
                model2.init( dataSet );
                fcOutput += ("Computed model is: " + model2.toString() + System.getProperty("line.separator"));
                if ( betterThan( model2, bestModel ) )
                    bestModel = model2;
                fcOutput += ("!! Best model changed to: " + bestModel.toString() + System.getProperty("line.separator"));
            }
        
        
        // Try multiple variable models
        
        // Create a list of available variables
        ArrayList<String> availableVariables
            = new ArrayList<String>(independentVariable.length);
        for ( int i=0; i<independentVariable.length; i++ ) {
            availableVariables.add( independentVariable[i] );
        }
        
        // Create a list of variables to use - initially empty
        ArrayList<String> bestVariables = new ArrayList<String>(independentVariable.length);
        
        // While some variables still available to consider
        while ( availableVariables.size() > 0 )
            {
                int count = bestVariables.size();
                String workingList[] = new String[count+1];
                if ( count > 0 )
                    for ( int i=0; i<count; i++ )
                        workingList[i] = (String)bestVariables.get(i);
                
                String bestAvailVariable = null;
                
                // For each available variable
                Iterator it = availableVariables.iterator();
                while ( it.hasNext() )
                    {
                        // Get current variable
                        String currentVar = (String)it.next();
                        
                        // Add variable to list to use for regression
                        workingList[count] = currentVar;
                        
                        // Do multiple variable linear regression
                        String wlString = "";
                        int c = 0;
                        for (String cvs :workingList) {
                        	if (c > 0)
                        		wlString += ",";
                        	else
                        		wlString += cvs;
                        	c++;
                        }
                        fcOutput += ("Setting up MLR  model for " + wlString + System.getProperty("line.separator"));
                        ForecastingModel model3 = new MultipleLinearRegressionModel( workingList );
                        allModels.add(model3);
                        model3.init( dataSet );
                        fcOutput += ("Computed model is: " + model3.toString() + System.getProperty("line.separator"));
                        
                        //  If best so far, then save best variable
                        if ( betterThan( model3, bestModel ) )
                            {
                                bestModel = model3;
                                fcOutput += ("!! Best model changed to: " + bestModel.toString() + System.getProperty("line.separator"));
                                bestAvailVariable = currentVar;
                            }
                        
                        // Remove the current variable from the working list
                        workingList[count] = null;
                    }
                
                // If no better model could be found (by adding another
                //     variable), then we're done
                if ( bestAvailVariable == null )
                    break;
                
                // Remove best variable from list of available vars
                int bestVarIndex = availableVariables.indexOf( bestAvailVariable );
                availableVariables.remove( bestVarIndex );
                
                // Add best variable to list of vars. to use
                bestVariables.add( count, bestAvailVariable );
                
                count++;
            }
        
        
        // Try time-series models
        if ( dataSet.getTimeVariable() != null ) {
                String timeVariable = dataSet.getTimeVariable();
                
                // Try moving average model
                ForecastingModel model = new MovingAverageModel();
                model.init( dataSet );
                if ( betterThan( model, bestModel ) )
                    bestModel = model;
                
                // Try moving average model using periods per year if avail.
                if ( dataSet.getPeriodsPerYear() > 0 )
                    {
                        model = new MovingAverageModel( dataSet.getPeriodsPerYear() );
                        model.init( dataSet );
                        if ( betterThan( model, bestModel ) )
                            bestModel = model;
                    }
                
                // TODO: Vary the period and try other MA models
                // TODO: Consider appropriate use of time period in this
                
                // Try the best fit simple exponential smoothing model
                model = SimpleExponentialSmoothingModel.getBestFitModel(dataSet);
                if ( betterThan( model, bestModel ) )
                    bestModel = model;
                
                // Try the best fit double exponential smoothing model
                model = DoubleExponentialSmoothingModel.getBestFitModel(dataSet);
                if ( betterThan( model, bestModel ) )
                    bestModel = model;
                
                // Try the best fit triple exponential smoothing model
                model = TripleExponentialSmoothingModel.getBestFitModel(dataSet);
                if ( betterThan( model, bestModel ) )
                    bestModel = model;
                
                
            }
        
        return bestModel;
    }
    
    public static String getOutput() {
    	return fcOutput;
    }
    
    /**
     * A helper method to determine, based on the existing accuracy indicators,
     * whether one model is "better than" a second model. This is done using
     * the accuracy indicators exposed by each model, as defined in the
     * ForecastingModel interface.
     *
     * <p>Generally, model2 should be the model that you expect to be worse. It
     * can also be <code>null</code> if no model2 has been selected. model1
     * cannot be <code>null</code>. If model2 is <code>null</code>, then
     * betterThan will return true on the assumption that some model, any
     * model, is better than no model.
     *
     * <p>The determination of which model is "best" is definitely subjective
     * when the two models are close. The approach implemented here is to
     * consider all current accuracy indicators (which admittedly are not
     * independent of each other), and if more indicators are in favor of one
     * model, then betterThan will return true.
     *
     * <p>It is expected that this implementation may change over time, so do
     * not depend on the approach described here. Rather just consider that
     * this method will implement a reasonable comparison of two models.
     * @param model1 the first model to compare.
     * @param model2 the second model to compare. If model1 is determined to
     *        be "better than" model2, then true is returned. model2 can be
     *        <code>null</code> representing the absence of a model.
     * @return true if model1 is "better than" model2; otherwise false.
     */
    private static boolean betterThan( ForecastingModel model1, ForecastingModel model2 )
    {
        // Special case. Any model is better than no model!
        if ( model2 == null ) {
          	fcOutput +=  ("First model is always Best!" + System.getProperty("line.separator"));
          	model1.setBestModel(true);
            return true;
        }
        
        double tolerance = 0.00000001;
        int score = 0;
        fcOutput +=  ("Comparing Bias Estimate" + System.getProperty("line.separator"));
        if ( model1.getBias()-model2.getBias() <= tolerance ) {
            score++;
            fcOutput +=  (model1.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        else if ( model1.getBias()-model2.getBias() >= tolerance ) {
            score--;
            fcOutput +=  (model2.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }

        fcOutput +=  ("Comparing MAD Estimate" + System.getProperty("line.separator"));
        if ( model1.getMAD()-model2.getMAD() <= tolerance ) {
            score++;
            fcOutput +=  (model1.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        else if ( model1.getMAD()-model2.getMAD() >= tolerance ) {
          fcOutput +=  (model2.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
            score--;
        }
        
        fcOutput +=  ("Comparing MAPE Estimate" + System.getProperty("line.separator"));
        if ( model1.getMAPE()-model2.getMAPE() <= tolerance ) {
            score++;
            fcOutput +=  (model1.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        else if ( model1.getMAPE()-model2.getMAPE() >= tolerance ) {
            score--;
            fcOutput +=  (model2.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }

        fcOutput +=  ("Comparing MSE Estimate" + System.getProperty("line.separator"));
        if ( model1.getMSE()-model2.getMSE() <= tolerance ) {
            score++;
            fcOutput +=  (model1.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        else if ( model1.getMSE()-model2.getMSE() >= tolerance ) {
            score--;
            fcOutput +=  (model2.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        
        fcOutput +=  ("Comparing SAE Estimate" + System.getProperty("line.separator"));
        if ( model1.getSAE()-model2.getSAE() <= tolerance ) {
            score++;
        		fcOutput +=  (model1.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        else if ( model1.getSAE()-model2.getSAE() >= tolerance ) {
            score--;
        		fcOutput +=  (model2.getForecastType() + " Wins (" + score + ")" + System.getProperty("line.separator"));
        }
        
        if ( score == 0 )
            {
          	fcOutput +=  ("It's a Tie!" + System.getProperty("line.separator"));

                // At this point, we're still unsure which one is best
                //  so we'll take another approach
                double diff = model1.getBias() - model2.getBias()
                    + model1.getMAD()  - model2.getMAD()
                    + model1.getMAPE() - model2.getMAPE()
                    + model1.getMSE()  - model2.getMSE()
                    + model1.getSAE()  - model2.getSAE();
                boolean result = diff < 0;
                if (result) {
                  model1.setBestModel(true);
                  model2.setBestModel(false);
                }
                else {
                  model1.setBestModel(false);
                  model2.setBestModel(true);
                }
                return ( result );
            }
        
        // if score > 0 then model1 is better than model2
        boolean result = score > 0;
        if (result) {
          model1.setBestModel(true);
          model2.setBestModel(false);
        }
        else {
          model1.setBestModel(false);
          model2.setBestModel(true);
        }
        return ( result );
    }
}
// Local Variables:
// tab-width: 4
// End:
