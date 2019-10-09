package no.hvl.multecore.common.hierarchy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.hvl.multecore.common.Constants;

public class EcoreModel extends AbstractModel {
		
	public EcoreModel(MultilevelHierarchy multilevelHierarchy) {
		super();
		name = Constants.ECORE_ID;
		level = 0;
		EClassNode eClassNode = new EClassNode(this);
		nodes.add(eClassNode);
		EReferenceEdge eReferenceEdge = new EReferenceEdge(eClassNode, this);
		edges.add(eReferenceEdge);
		inEdges.computeIfAbsent(eClassNode, k -> new HashSet<IEdge>()).add(eReferenceEdge);
		outEdges.computeIfAbsent(eClassNode, k -> new HashSet<IEdge>()).add(eReferenceEdge);
		metamodels.add(this);
		this.multilevelHierarchy = multilevelHierarchy;
	}

	
	@Override
	public IModel getMainMetamodel() {
		return this;
	}

	
	@Override
	public Set<IModel> getAllMetamodels() {
		return metamodels;
	}


	@Override
	public Set<IModel> getSupplementaryModels() {
		return new HashSet<IModel>();
	}


	@Override
	public IModel getSupplementaryModel(String modelName) {
		return null;
	}
	
	
	@Override
	public IModel getSupplementaryModelForHierarchy(MultilevelHierarchy supplementaryHierarchy) {
		return null;
	}


	@Override
	public List<IModel> getBranch(boolean excludeEcore) {
		List<IModel> modelsInBranch = new ArrayList<IModel>();
		if (!excludeEcore)
			modelsInBranch.add(this);
		return modelsInBranch;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof EcoreModel)
			return true;
		return false;
	}
	
}
