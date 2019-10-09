package no.hvl.multecore.common.hierarchy;

import java.util.List;
import java.util.Set;

public interface IModel extends Comparable<IModel> {

	public String getName();

	public int getLevel();

	// The main metamodel is supposed to be the closest in number of levels
	public IModel getMainMetamodel();

	public Set<IModel> getDirectMetamodels();
	
	public Set<IModel> getAllMetamodels();

	public IModel getMetamodelInLevel(int level);

	public Set<IModel> getSupplementaryModels();
	
	public IModel getSupplementaryModel(String modelName);
	
	public IModel getSupplementaryModelForHierarchy(MultilevelHierarchy supplementaryHierarchy);
	
	public INode getNode(String nodeName);
	
	public Set<INode> getNodes();
	
	public IEdge getEdge(String edgeName, String sourceName, String targetName);

	public IEdge getEdgePlusInherited(String edgeName, String sourceName, String targetName);
	
	public Set<IEdge> getEdges();

	public Set<IEdge> getInEdges(INode node);

	public Set<INode> getInEdgesSources(INode node);

	public Set<IEdge> getOutEdges(INode node);

	public Set<INode> getOutEdgesTargets(INode node);

	public MultilevelHierarchy getHierarchy();
	
	public List<IModel> getBranch(boolean excludeEcore);

	
}