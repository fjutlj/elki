package de.lmu.ifi.dbs.elki.index.tree.metrical;

import java.util.List;

import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;
import de.lmu.ifi.dbs.elki.index.tree.Entry;
import de.lmu.ifi.dbs.elki.index.tree.IndexTree;
import de.lmu.ifi.dbs.elki.index.tree.Node;
import de.lmu.ifi.dbs.elki.persistent.PageFile;

/**
 * Abstract super class for all metrical index classes.
 * 
 * @author Elke Achtert
 * 
 * @apiviz.has MetricalNode oneway - - contains
 * 
 * @param <O> the type of objects stored in the index
 * @param <D> the type of Distance used in the metrical index
 * @param <N> the type of nodes used in the metrical index
 * @param <E> the type of entries used in the metrical index
 */
public abstract class MetricalIndexTree<O, D extends Distance<D>, N extends Node<N, E>, E extends Entry> extends IndexTree<N, E> {
  /**
   * Constructor.
   * 
   * @param pagefile Page file
   */
  public MetricalIndexTree(PageFile<N> pagefile) {
    super(pagefile);
  }

  /**
   * Returns the distance function of this metrical index.
   * 
   * @return the distance function of this metrical index
   */
  public abstract DistanceFunction<? super O, D> getDistanceFunction();

  /**
   * Returns the distance function of this metrical index.
   * 
   * @return the distance function of this metrical index
   */
  public abstract DistanceQuery<O, D> getDistanceQuery();

  /**
   * Returns a list of entries pointing to the leaf nodes of this spatial index.
   * 
   * @return a list of entries pointing to the leaf nodes of this spatial index
   */
  public abstract List<E> getLeaves();
}