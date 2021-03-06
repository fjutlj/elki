package de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2014
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.utilities.FormatUtil;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.ArrayLikeUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.WrongParameterValueException;

/**
 * Parameter class for a parameter specifying a list of double values.
 * 
 * @author Steffi Wanka
 * @author Erich Schubert
 */
public class DoubleListParameter extends ListParameter<DoubleListParameter, Double> {
  /**
   * Constructs a list parameter with the given optionID and optional flag.
   * 
   * @param optionID Option ID
   * @param optional Optional flag
   */
  public DoubleListParameter(OptionID optionID, boolean optional) {
    super(optionID, optional);
  }

  /**
   * Constructs a list parameter with the given optionID.
   * 
   * @param optionID Option ID
   */
  public DoubleListParameter(OptionID optionID) {
    super(optionID);
  }

  @Override
  public String getValueAsString() {
    return FormatUtil.format(ArrayLikeUtil.toPrimitiveDoubleArray(getValue()), LIST_SEP);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<Double> parseValue(Object obj) throws ParameterException {
    try {
      List<?> l = List.class.cast(obj);
      // do extra validation:
      for(Object o : l) {
        if(!(o instanceof Double)) {
          throw new WrongParameterValueException("Wrong parameter format for parameter \"" + getName() + "\". Given list contains objects of different type!");
        }
      }
      // TODO: can we use reflection to get extra checks?
      // TODO: Should we copy the list?
      return (List<Double>) l;
    }
    catch(ClassCastException e) {
      // continue with others
    }
    if(obj instanceof String) {
      String[] values = SPLIT.split((String) obj);
      ArrayList<Double> doubleValue = new ArrayList<>(values.length);
      for(String val : values) {
        doubleValue.add(FormatUtil.parseDouble(val));
      }
      return doubleValue;
    }
    if(obj instanceof Double) {
      ArrayList<Double> doubleValue = new ArrayList<>(1);
      doubleValue.add((Double) obj);
      return doubleValue;
    }
    throw new WrongParameterValueException("Wrong parameter format! Parameter \"" + getName() + "\" requires a list of Double values!");
  }

  /**
   * Sets the default value of this parameter.
   * 
   * @param allListDefaultValue default value for all list elements of this
   *        parameter
   */
  // Unused?
  /*
   * public void setDefaultValue(double allListDefaultValue) { for(int i = 0; i
   * < defaultValue.size(); i++) { defaultValue.set(i, allListDefaultValue); } }
   */

  /**
   * Returns a string representation of the parameter's type.
   * 
   * @return &quot;&lt;double_1,...,double_n&gt;&quot;
   */
  @Override
  public String getSyntax() {
    return "<double_1,...,double_n>";
  }
}
