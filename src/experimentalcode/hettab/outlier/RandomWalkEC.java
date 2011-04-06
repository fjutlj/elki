package experimentalcode.hettab.outlier;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.OutlierAlgorithm;
import de.lmu.ifi.dbs.elki.data.DatabaseObject;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.AssociationID;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.NumberDistance;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.MinMax;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.AnnotationFromDataStore;
import de.lmu.ifi.dbs.elki.result.AnnotationResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.QuotientOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;
import experimentalcode.shared.outlier.generalized.neighbors.NeighborSetPredicate;

/**
 * 
 * @author Ahmed Hettab
 *
 * @param <V>
 * @param <D>
 */
public class RandomWalkEC<V extends NumberVector<?, ?>, D extends NumberDistance<D, ?>> extends AbstractDistanceBasedAlgorithm<V, D, OutlierResult> implements OutlierAlgorithm<V, OutlierResult> {
   
  /**
   * 
   */
  private static final Logging logger = Logging.getLogger(RandomWalkEC.class);
  /**
   * Parameter to specify the neighborhood predicate to use.
   */
  public static final OptionID NEIGHBORHOOD_ID = OptionID.getOrCreateOptionID("neighborhood", "The neighborhood predicate to use.");
  /**
   * Holds the alpha value
   */
  private static final OptionID  ALPHA_ID = OptionID.getOrCreateOptionID("rwec.alpha","parameter for similarity computing");
  /**
   * 
   * Holds the z value
   */
  private static final OptionID Z_ID = OptionID.getOrCreateOptionID("rwec.z", "the position of z attribut");
  /**
   * 
   * Holds the c value
   */
  private static final OptionID C_ID = OptionID.getOrCreateOptionID("rwec.c", "the damping factor");
  /**
   * parameter alpha
   */
  private double alpha ;
  /**
   * parameter z 
   */
  private int z;
  /**
   * parameter c
   */
  private double c ;
  /**
   * Our predicate to obtain the neighbors
   */
  NeighborSetPredicate.Factory<DatabaseObject> npredf = null;
  /**
   * The association id to associate the SCORE of an object for the RandomWalkEC algorithm
   * algorithm.
   */
  public static final AssociationID<Double> RW_EC_SCORE = AssociationID.getOrCreateAssociationID("outlier-score", Double.class);
  /**
   * Constructor
   * @param distanceFunction
   * @param npredf
   * @param alpha
   * @param c
   * @param z
   */
  protected RandomWalkEC(DistanceFunction< V, D> distanceFunction ,NeighborSetPredicate.Factory<DatabaseObject> npredf, double alpha ,double c, int z) {
    super(distanceFunction);
    this.npredf = npredf ;
    this.alpha = alpha ;
    this.z = z ;
    this.c = c ;
  }

  @Override
  protected OutlierResult runInTime(Database<V> database) throws IllegalStateException {
    
    final NeighborSetPredicate npred = npredf.instantiate(database);
    DistanceQuery<V, D> distFunc = database.getDistanceQuery(getDistanceFunction());
    WritableDataStore<Matrix> similarityVectors = DataStoreUtil.makeStorage(database.getIDs(),DataStoreFactory.HINT_TEMP, Matrix.class);
    WritableDataStore<List<Pair<DBID,Double>>> simScores =  DataStoreUtil.makeStorage(database.getIDs(),DataStoreFactory.HINT_TEMP, List.class);
    
    //construct the relation Matrix of the ec-graph
    Matrix E = new Matrix(database.size(),database.size());
    int i = 0 ;
    for(DBID id : database){
      npred.getNeighborDBIDs(id);
      int j = 0 ;
        for(DBID n : database){
          double e ;
          if(n.getIntegerID() == id.getIntegerID()){
            e = 0 ;
          }
          else{
          double dist = distFunc.distance(id, n).doubleValue();
          double diff = Math.abs(database.get(id).doubleValue(z)-database.get(n).doubleValue(z));
          diff = (Math.pow(diff, alpha));
          diff = (Math.exp(diff));
          diff = (1/diff);
           e = diff*dist ;
          }
          E.set(i, j, e);
          j++;
        }
        i++;
    }
    //normalize the adjacent Matrix
    E.normalizeColumns();
    
    //compute similarity vector for each Object
    int count = 0 ;
    for(DBID id : database){
      Matrix Si = new Matrix(1,database.size());
      Matrix Ei = new Matrix(database.size(),1);
      Si.transpose();
        //construct Ei
        for(int l = 0 ; l<database.size();l++){
          if( l == count){
            Ei.set(l, 0, 1.0);
          }
          else{
            Ei.set(l, 0, 0.0);
          }
        }   
      Ei.transpose();
      //compute similarity vector
      Matrix I = Matrix.unitMatrix(database.size());
      Matrix R = E.times(c);
      R = I.minus(R);
      R = R.times((1-c));
      R = R.inverse() ;
      Si = R.times(Ei);
      similarityVectors.put(id, Si);
      count++;
    }
    
    //compute the relevance scores between specified objects and its neighbors
    

    for(DBID id : database){
      DBIDs neighbors = npred.getNeighborDBIDs(id);
      ArrayList<Pair<DBID,Double>> sim = new ArrayList<Pair<DBID,Double>>();
        for(DBID n : neighbors){
          Pair<DBID,Double> p = new Pair<DBID,Double>(n.getID(),cosineSimilarity(similarityVectors.get(id).getColumnVector(0), similarityVectors.get(n).getColumnVector(0)));
          sim.add(p);
        }
        simScores.put(id,sim);
        System.out.println(sim);
    }
    
    MinMax<Double> minmax = new MinMax<Double>();
    WritableDataStore<Double> scores = DataStoreUtil.makeStorage(database.getIDs(), DataStoreFactory.HINT_STATIC, Double.class);
    for(DBID id : database){
       List<Pair<DBID,Double>> simScore = simScores.get(id);
       GeometricMean gm = new GeometricMean();
        for(Pair<DBID,Double> pair : simScore){
          gm.increment(pair.second);
        }
       scores.put(id, gm.getResult());
       minmax.put(gm.getResult());
    }
    AnnotationResult<Double> scoreResult = new AnnotationFromDataStore<Double>("LOM", "LOM-outlier", RW_EC_SCORE, scores);
    OutlierScoreMeta scoreMeta = new QuotientOutlierScoreMeta(minmax.getMin(), minmax.getMax(), 0.0, Double.POSITIVE_INFINITY, 0.17);
    return new OutlierResult(scoreMeta, scoreResult);
  }
  
  /**
   * Computes the cosine similarity for two given feature vectors.
   */
  private static double cosineSimilarity(Vector v1 , Vector v2){
    v1.normalize();
    v2.normalize() ;
    double d = 1 - v1.transposeTimes(v2);
    if(d < 0) {
      d = 0;
    }
    return d ;
  }
  /**
   * 
   */
  @Override
  protected Logging getLogger() {
    return logger;
  }
  /**
   * 
   * @param <V>
   * @param <D>
   * @param config
   * @return
   */
  public static <V extends NumberVector<V, ?>, D extends NumberDistance<D, ?>> RandomWalkEC<V,D> parameterize(Parameterization config) {
    final NeighborSetPredicate.Factory<DatabaseObject> npred = getNeighborPredicate(config);
    final double alpha = getParameterALPHA(config);
    final int z = getParameterZ(config);
    final double c = getParameterC(config);
    DistanceFunction<V, D> distanceFunction = getParameterDistanceFunction(config);
    if(config.hasErrors()) {
      return null;
    }
    return new RandomWalkEC<V, D>(distanceFunction,npred, alpha ,c, z );
  }
  
  /**
   * Get the alpha parameter
   * 
   * @param config Parameterization
   * @return alpha parameter
   */
  protected static double getParameterALPHA(Parameterization config) {
    final DoubleParameter param = new DoubleParameter(ALPHA_ID);
    if(config.grab(param)) {
      return param.getValue();
    }
    return 0;
  }
  /**
   * get the c parameter
   * @param config
   * @return
   */
  protected static double getParameterC(Parameterization config) {
    final DoubleParameter param = new DoubleParameter(C_ID);
    if(config.grab(param)) {
      return param.getValue();
    }
    return 0;
  }
  /**
   * Get the z parameter
   * 
   * @param config Parameterization
   * @return z parameter
   */
  protected static int getParameterZ(Parameterization config) {
    final IntParameter param = new IntParameter(Z_ID);
    if(config.grab(param)) {
      return param.getValue();
    }
    return 0;
  }
  
  /**
   * 
   * @param config
   * @return
   */
  public static NeighborSetPredicate.Factory<DatabaseObject> getNeighborPredicate(Parameterization config) {
    final ObjectParameter<NeighborSetPredicate.Factory<DatabaseObject>> param = new ObjectParameter<NeighborSetPredicate.Factory<DatabaseObject>>(NEIGHBORHOOD_ID, NeighborSetPredicate.Factory.class, true);
    if(config.grab(param)) {
      return param.instantiateClass(config);
    }
    return null;
  }


}