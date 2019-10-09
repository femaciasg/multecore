package no.hvl.multecore.common.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class AbstractModel implements IModel {

	protected String name;
	protected int level;
	protected Set<INode> nodes;
	protected Set<IEdge> edges;
	protected Map<INode, Set<IEdge>> inEdges;
	protected Map<INode, Set<IEdge>> outEdges;
	protected Set<IModel> metamodels;
	protected MultilevelHierarchy multilevelHierarchy;

	protected AbstractModel() {
		nodes = new HashSet<INode>();
		edges = new HashSet<IEdge>();
		inEdges = new HashMap<INode, Set<IEdge>>();
		outEdges = new HashMap<INode, Set<IEdge>>();
		metamodels = new HashSet<IModel>();
	}


	@Override
	public String getName() {
		return name;
	}
	

	@Override
	public int getLevel() {
		return level;
	}


	@Override
	public INode getNode(String nodeName) {
		for (INode node : nodes) {
			if (node.getName().equals(nodeName))
				return node;
		}
		return null;
	}
	
	
	@Override
	public Set<INode> getNodes() {
		return nodes;
	}

	
	@Override
	public IEdge getEdge(String edgeName, String sourceName, String targetName) {
		INode sourceNode = null;
		if (null == (sourceNode = getNode(sourceName)))
			return null;
		if (null == (getNode(targetName)))
			return null;
		for (IEdge edge : outEdges.get(sourceNode)) {
			if (edge.getName().equals(edgeName))
				return edge;
		}
		return null;
	}

	
	@Override
	public IEdge getEdgePlusInherited(String edgeName, String sourceName, String targetName) {
		INode sourceNode = null, targetNode = null;
		if (null == (sourceNode = getNode(sourceName)))
			return null;
		if (null == (targetNode = getNode(targetName)))
			return null;
		IEdge edge = getEdge(edgeName, sourceName, targetName);
		if (null != edge)
			return edge;
		Set<INode> sourceParentNodes = sourceNode.getAllParentNodes();
		Set<INode> targetParentNodes = targetNode.getAllParentNodes();
		for (INode sourceParentNode : sourceParentNodes) {
			if (null != (edge = getEdge(edgeName, sourceParentNode.getName(), targetName)))
				return edge;
			for (INode targetParentNode : targetParentNodes) {
				if (null != (edge = getEdge(edgeName, sourceName, targetParentNode.getName())))
					return edge;
				if (null != (edge = getEdge(edgeName, sourceParentNode.getName(), targetParentNode.getName())))
					return edge;
			}
		}
		for (INode targetParentNode : targetParentNodes) {
			if (null != (edge = getEdge(edgeName, sourceName, targetParentNode.getName())))
				return edge;
			for (INode sourceParentNode : sourceParentNodes) {
				if (null != (edge = getEdge(edgeName, sourceParentNode.getName(),targetName)))
					return edge;
			}
		}		
		return null;
	}
	
	
	@Override
	public Set<IEdge> getEdges() {
		return edges;
	}
	

	@Override
	public Set<IEdge> getInEdges(INode node) {
		Set<IEdge> nodeInEdges = inEdges.get(node);
		return (null==nodeInEdges)? new HashSet<IEdge>() : nodeInEdges;
	}
	
	
	@Override
	public Set<INode> getInEdgesSources(INode node) {
		Set<INode> inEdgesSources = new HashSet<INode>();
		for (IEdge iEdge : getInEdges(node))
			inEdgesSources.add(iEdge.getSource());
		return inEdgesSources;
	}


	@Override
	public Set<IEdge> getOutEdges(INode node) {
		Set<IEdge> nodeOutEdges = outEdges.get(node);
		return (null==nodeOutEdges)? new HashSet<IEdge>() : nodeOutEdges;
	}
	
	
	@Override
	public Set<INode> getOutEdgesTargets(INode node) {
		Set<INode> outEdgesTargets = new HashSet<INode>();
		for (IEdge iEdge : getOutEdges(node))
			outEdgesTargets.add(iEdge.getTarget());
		return outEdgesTargets;
	}
	
	
	@Override
	public Set<IModel> getDirectMetamodels() {
		return metamodels;
	}

	
	@Override
	public IModel getMetamodelInLevel(int level) {
		for (IModel metamodel : getAllMetamodels()) {
			if (metamodel.getLevel() == level)
				return metamodel;
		}
		return null;
	}
	
	
	@Override
	public MultilevelHierarchy getHierarchy() {
		return multilevelHierarchy;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	
	@Override
	public int compareTo(IModel other) {
		return this.getLevel() - other.getLevel();
	}
	
	
	@Override
	public String toString() {
		return name;
	}
}
