package de.lmu.ifi.dbs.elki.database.query.knn;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.DistanceResultPair;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.distance.distancefunction.SpatialPrimitiveDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialIndex;

/**
 * Instance of a KNN query for a particular spatial index.
 * 
 * @author Erich Schubert
 */
public class SpatialIndexKNNQueryInstance<O extends NumberVector<?, ?>, D extends Distance<D>> extends DatabaseKNNQuery.Instance<O, D> {
  /**
   * The index to use
   */
  SpatialIndex<O, ?, ?> index;

  /**
   * Spatial primitive distance function
   */
  SpatialPrimitiveDistanceFunction<? super O, D> distanceFunction;

  /**
   * Constructor.
   * 
   * @param index Index to use
   * @param distanceQuery Distance query to use
   * @param distanceFunction Distance function
   */
  public SpatialIndexKNNQueryInstance(Database<O> database, SpatialIndex<O, ?, ?> index, DistanceQuery<O, D> distanceQuery, SpatialPrimitiveDistanceFunction<? super O, D> distanceFunction) {
    super(database, distanceQuery);
    this.index = index;
    this.distanceFunction = distanceFunction;
  }

  @Override
  public List<DistanceResultPair<D>> getKNNForObject(O obj, int k) {
    return index.kNNQuery(obj, k, distanceFunction);
  }

  @Override
  public List<DistanceResultPair<D>> getKNNForDBID(DBID id, int k) {
    return getKNNForObject(database.get(id), k);
  }
  
  @Override
  public List<List<DistanceResultPair<D>>> getKNNForBulkDBIDs(ArrayDBIDs ids, int k) {
    return index.bulkKNNQueryForIDs(ids, k, distanceFunction);
  }

  @Override
  public D getDistanceFactory() {
    return distanceQuery.getDistanceFactory();
  }
}