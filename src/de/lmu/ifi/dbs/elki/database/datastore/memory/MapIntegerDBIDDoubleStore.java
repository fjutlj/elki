package de.lmu.ifi.dbs.elki.database.datastore.memory;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBID;

/**
 * Writable data store for double values.
 * 
 * @author Erich Schubert
 */
public class MapIntegerDBIDDoubleStore implements WritableDoubleDataStore {
  /**
   * Data storage
   */
  private TIntDoubleMap map;
  
  /**
   * Constructor.
   *
   * @param size Expected size
   */
  public MapIntegerDBIDDoubleStore(int size) {
    super();
    map = new TIntDoubleHashMap(size, 0.5f, Integer.MIN_VALUE, Double.NaN);
  }

  @Override
  @Deprecated
  public Double get(DBID id) {
    return map.get(id.getIntegerID());
  }

  @Override
  public double doubleValue(DBID id) {
    return map.get(id.getIntegerID());
  }

  @Override
  public String getLongName() {
    return "raw";
  }

  @Override
  public String getShortName() {
    return "raw";
  }

  @Override
  @Deprecated
  public Double put(DBID id, Double value) {
    return map.put(id.getIntegerID(), value);
  }

  @Override
  public void destroy() {
    map.clear();
    map = null;
  }

  @Override
  public void delete(DBID id) {
    map.remove(id.getIntegerID());
  }

  @Override
  public double putDouble(DBID id, double value) {
    return map.put(id.getIntegerID(), value);
  }

  @Override
  public double put(DBID id, double value) {
    return map.put(id.getIntegerID(), value);
  }
}