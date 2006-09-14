package de.lmu.ifi.dbs.wrapper;

import java.util.List;

import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;
import de.lmu.ifi.dbs.utilities.optionhandling.FileParameter;
import de.lmu.ifi.dbs.utilities.optionhandling.ParameterException;

/**
 * StandAloneInputWrapper extends StandAloneWrapper and
 * sets additionally the parameter in. <p/> Any
 * Wrapper class that makes use of these flags may extend this class. Beware to
 * make correct use of parameter settings via optionHandler as commented with
 * constructor and methods.
 *
 * @author Elke Achtert (<a
 *         href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
public abstract class StandAloneInputWrapper extends StandAloneWrapper {

  /**
   * Label for parameter input.
   */
  public final static String INPUT_P = "in";

  /**
   * Description for parameter input.
   */
  public static String INPUT_D = "input file";

  /**
   * The name of the input file.
   */
  private String input;

  /**
   * Sets additionally to the parameters set by the super class the
   * parameter in in the parameter map. Any extending
   * class should call this constructor, then add further parameters. 
   */
  protected StandAloneInputWrapper() {
    super();
    optionHandler.put(INPUT_P, new FileParameter(INPUT_P,INPUT_D));
  }

  /**
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#setParameters(String[])
   */
  public String[] setParameters(String[] args) throws ParameterException {
    String[] remainingParameters = super.setParameters(args);
    // input
    input = optionHandler.getOptionValue(INPUT_P);
    return remainingParameters;
  }

  /**
   * @see de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable#getAttributeSettings()
   */
  public List<AttributeSettings> getAttributeSettings() {
    List<AttributeSettings> settings = super.getAttributeSettings();
    AttributeSettings mySettings = settings.get(0);
    mySettings.addSetting(INPUT_P, input);
    return settings;
  }

  /**
   * Returns the input string.
   *
   * @return the input string
   */
  public final String getInput() {
    return input;
  }
}
