package de.lmu.ifi.dbs.elki.datasource.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;

import de.lmu.ifi.dbs.elki.data.BitVector;
import de.lmu.ifi.dbs.elki.data.LabelList;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.data.type.VectorFieldTypeInformation;
import de.lmu.ifi.dbs.elki.datasource.bundle.BundleMeta;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;

/**
 * Provides a parser for parsing one sparse BitVector per line, where the
 * indices of the one-bits are separated by whitespace. The first index starts
 * with zero.
 * <p/>
 * Several labels may be given per BitVector, a label must not be parseable as
 * an Integer. Lines starting with &quot;#&quot; will be ignored.
 * 
 * @author Elke Achtert
 */
@Title("Sparse Bit Vector Label Parser")
@Description("Parser for the lines of the following format:\n" + "A single line provides a single sparse BitVector. The indices of the one-bits are " + "separated by whitespace. The first index starts with zero. Any substring not containing whitespace is tried to be read as an Integer. " + "If this fails, it will be appended to a label. (Thus, any label must not be parseable as an Integer.) " + "Empty lines and lines beginning with \"#\" will be ignored.")
public class SparseBitVectorLabelParser extends AbstractParser implements Parser {
  /**
   * Class logger
   */
  private static final Logging logger = Logging.getLogger(SparseBitVectorLabelParser.class);

  /**
   * Constructor.
   * 
   * @param colSep
   * @param quoteChar
   */
  public SparseBitVectorLabelParser(Pattern colSep, char quoteChar) {
    super(colSep, quoteChar);
  }

  @Override
  public MultipleObjectsBundle parse(InputStream in) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    int lineNumber = 0;
    int dimensionality = -1;
    List<Object> vectors = new ArrayList<Object>();
    List<Object> lblc = new ArrayList<Object>();
    try {
      List<BitSet> bitSets = new ArrayList<BitSet>();
      List<LabelList> allLabels = new ArrayList<LabelList>();
      for(String line; (line = reader.readLine()) != null; lineNumber++) {
        if(!line.startsWith(COMMENT) && line.length() > 0) {
          List<String> entries = tokenize(line);
          BitSet bitSet = new BitSet();
          LabelList labels = new LabelList();

          for(String entry : entries) {
            try {
              Integer index = Integer.valueOf(entry);
              bitSet.set(index);
              dimensionality = Math.max(dimensionality, index);
            }
            catch(NumberFormatException e) {
              labels.add(entry);
            }
          }

          bitSets.add(bitSet);
          allLabels.add(labels);
        }
      }

      dimensionality++;
      for(int i = 0; i < bitSets.size(); i++) {
        BitSet bitSet = bitSets.get(i);
        List<String> labels = allLabels.get(i);
        vectors.add(new BitVector(bitSet, dimensionality));
        lblc.add(labels);
      }
    }
    catch(IOException e) {
      throw new IllegalArgumentException("Error while parsing line " + lineNumber + ".");
    }
    BundleMeta meta = new BundleMeta();
    List<List<Object>> columns = new ArrayList<List<Object>>(2);
    meta.add(getTypeInformation(dimensionality));
    columns.add(vectors);
    meta.add(TypeUtil.LABELLIST);
    columns.add(lblc);
    return new MultipleObjectsBundle(meta, columns);
  }

  protected VectorFieldTypeInformation<BitVector> getTypeInformation(int dimensionality) {
    return new VectorFieldTypeInformation<BitVector>(BitVector.class, dimensionality, new BitVector(new BitSet(), dimensionality));
  }
  
  @Override
  protected Logging getLogger() {
    return logger;
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParser.Parameterizer {
    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
    }

    @Override
    protected SparseBitVectorLabelParser makeInstance() {
      return new SparseBitVectorLabelParser(colSep, quoteChar);
    }
  }
}