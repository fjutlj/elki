package de.lmu.ifi.dbs.elki.datasource.filter;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.data.SparseFloatVector;

/**
 * Perform full TF-IDF Normalization as commonly used in text mining.
 * 
 * Each record is first normalized using "term frequencies" to sum up to 1. Then
 * it is globally normalized using the Inverse Document Frequency, so rare terms
 * are weighted stronger than common terms.
 * 
 * Restore will only undo the IDF part of the normalization!
 * 
 * @author Erich Schubert
 */
public class TFIDFNormalization extends InverseDocumentFrequencyNormalization {
  /**
   * Constructor.
   */
  public TFIDFNormalization() {
    super();
  }

  @Override
  protected SparseFloatVector filterSingleObject(SparseFloatVector featureVector) {
    BitSet b = featureVector.getNotNullMask();
    double sum = 0.0;
    for(int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) {
      sum += featureVector.doubleValue(i);
    }
    if(sum <= 0) {
      sum = 1.0;
    }
    Map<Integer, Float> vals = new HashMap<Integer, Float>();
    for(int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) {
      vals.put(i, (float) (featureVector.doubleValue(i) / sum * idf.get(i).doubleValue()));
    }
    return new SparseFloatVector(vals, featureVector.getDimensionality());
  }
}