package de.lmu.ifi.dbs.elki.index.preprocessed.localpca;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.query.DistanceResultPair;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.index.AbstractIndex;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.PCAFilteredResult;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.PCAFilteredRunner;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.exceptions.ExceptionMessages;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;

/**
 * Abstract base class for a local PCA based index.
 * 
 * @author Elke Achtert
 * @author Erich Schubert
 * 
 * @apiviz.has PCAFilteredRunner
 * @apiviz.has WritableDataStore
 * 
 * @param <NV> Vector type
 */
//TODO: loosen DoubleDistance restriction.
@Title("Local PCA Preprocessor")
@Description("Materializes the local PCA and the locally weighted matrix of objects of a database.")
public abstract class AbstractFilteredPCAIndex<NV extends NumberVector<?, ?>> extends AbstractIndex<NV> implements FilteredLocalPCAIndex<NV> {
  /**
   * Database we are attached to
   */
  final protected Database<NV> database;

  /**
   * PCA utility object.
   */
  final protected PCAFilteredRunner<NumberVector<?, ?>, DoubleDistance> pca;

  /**
   * Storage for the precomputed results.
   */
  private WritableDataStore<PCAFilteredResult> pcaStorage = null;
  
  /**
   * Constructor.
   * 
   * @param database Database to use
   * @param pca PCA runner to use
   */
  public AbstractFilteredPCAIndex(Database<NV> database, PCAFilteredRunner<NumberVector<?, ?>, DoubleDistance> pca) {
    super();
    this.database = database;
    this.pca = pca;
  }

  /**
   * Preprocessing step.
   */
  protected void preprocess() {
    if(database == null || database.size() <= 0) {
      throw new IllegalArgumentException(ExceptionMessages.DATABASE_EMPTY);
    }

    // Note: this is required for ERiC to work properly, otherwise the data is
    // recomputed for the partitions!
    if(pcaStorage != null) {
      return;
    }

    pcaStorage = DataStoreUtil.makeStorage(database.getIDs(), DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_TEMP, PCAFilteredResult.class);

    long start = System.currentTimeMillis();
    FiniteProgress progress = getLogger().isVerbose() ? new FiniteProgress("Performing local PCA", database.size(), getLogger()) : null;

    // TODO: use a bulk operation?
    for(DBID id : database) {
      List<DistanceResultPair<DoubleDistance>> objects = objectsForPCA(id);

      PCAFilteredResult pcares = pca.processQueryResult(objects, database);

      pcaStorage.put(id, pcares);

      if(progress != null) {
        progress.incrementProcessed(getLogger());
      }
    }
    if(progress != null) {
      progress.ensureCompleted(getLogger());
    }

    long end = System.currentTimeMillis();
    if(getLogger().isVerbose()) {
      long elapsedTime = end - start;
      getLogger().verbose(this.getClass().getName() + " runtime: " + elapsedTime + " milliseconds.");
    }
  }

  @Override
  public PCAFilteredResult get(DBID objid) {
    if(pcaStorage == null) {
      preprocess();
    }
    return pcaStorage.get(objid);
  }

  public abstract Logging getLogger();

  /**
   * Returns the objects to be considered within the PCA for the specified query
   * object.
   * 
   * @param id the id of the query object for which a PCA should be performed
   * @return the list of the objects (i.e. the ids and the distances to the
   *         query object) to be considered within the PCA
   */
  protected abstract List<DistanceResultPair<DoubleDistance>> objectsForPCA(DBID id);
  
  /**
   * Factory class
   * 
   * @author Erich Schubert
   * 
   * @apiviz.stereotype factory
   * @apiviz.uses AbstractFilteredPCAIndex oneway - - «create»
   */
  public static abstract class Factory implements Parameterizable {
    /**
     * OptionID for {@link #PCA_DISTANCE_PARAM}
     */
    public static final OptionID PCA_DISTANCE_ID = OptionID.getOrCreateOptionID("localpca.distancefunction", "The distance function used to select objects for running PCA.");

    /**
     * Parameter to specify the distance function used for running PCA.
     * 
     * Key: {@code -localpca.distancefunction}
     */
    protected final ObjectParameter<DistanceFunction<NumberVector<?, ?>, DoubleDistance>> PCA_DISTANCE_PARAM = new ObjectParameter<DistanceFunction<NumberVector<?, ?>, DoubleDistance>>(PCA_DISTANCE_ID, DistanceFunction.class, EuclideanDistanceFunction.class);

    /**
     * Holds the instance of the distance function specified by
     * {@link #PCA_DISTANCE_PARAM}.
     */
    protected DistanceFunction<NumberVector<?, ?>, DoubleDistance> pcaDistanceFunction;

    /**
     * PCA utility object.
     */
    protected PCAFilteredRunner<NumberVector<?, ?>, DoubleDistance> pca;
    
    /**
     * Constructor, adhering to
     * {@link de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable}
     * 
     * @param config Parameterization
     */
    public Factory(Parameterization config) {
      super();
      config = config.descend(this);

      // parameter pca distance function
      if(config.grab(PCA_DISTANCE_PARAM)) {
        pcaDistanceFunction = PCA_DISTANCE_PARAM.instantiateClass(config);
      }

      pca = new PCAFilteredRunner<NumberVector<?, ?>, DoubleDistance>(config);
    }
    
    /**
     * Abstract, since we can't instantiate the abstract index.
     * 
     * @param <V> Actual vector type
     * @param database Database type
     * 
     * @return Index
     */
    public abstract <V extends NumberVector<?, ?>> AbstractFilteredPCAIndex<V> instantiate(Database<V> database);
  }
}