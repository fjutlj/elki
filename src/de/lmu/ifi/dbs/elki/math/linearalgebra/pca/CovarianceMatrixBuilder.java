package de.lmu.ifi.dbs.elki.math.linearalgebra.pca;

import java.util.Collection;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.query.DistanceResultPair;
import de.lmu.ifi.dbs.elki.distance.distancevalue.NumberDistance;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;

public interface CovarianceMatrixBuilder<V extends NumberVector<?, ?>, D extends NumberDistance<D, ?>> {

  /**
   * Compute Covariance Matrix for a complete database
   * 
   * @param database the database used
   * @return Covariance Matrix
   */
  public Matrix processDatabase(Database<? extends V> database);

  /**
   * Compute Covariance Matrix for a collection of database IDs
   * 
   * @param ids a collection of ids
   * @param database the database used
   * @return Covariance Matrix
   */
  public Matrix processIds(DBIDs ids, Database<? extends V> database);

  /**
   * Compute Covariance Matrix for a QueryResult Collection
   * 
   * By default it will just collect the ids and run processIds
   * 
   * @param results a collection of QueryResults
   * @param database the database used
   * @param k the number of entries to process
   * @return Covariance Matrix
   */
  public Matrix processQueryResults(Collection<DistanceResultPair<D>> results, Database<? extends V> database, int k);

  /**
   * Compute Covariance Matrix for a QueryResult Collection
   * 
   * By default it will just collect the ids and run processIds
   * 
   * @param results a collection of QueryResults
   * @param database the database used
   * @return Covariance Matrix
   */
  public Matrix processQueryResults(Collection<DistanceResultPair<D>> results, Database<? extends V> database);

}