package no.hvl.multecore.common.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Model extends AbstractModel {
	
	private Set<IModel> supplementaryModels;
	

	public Model(String name, IModel metamodel) {
		super();
		this.name = name;
		this.metamodels.add(metamodel);
		this.multilevelHierarchy = metamodel.getHierarchy();
		supplementaryModels = new HashSet<IModel>();
	}
	
	
	public Model(String name, IModel metamodel, int level) {
		super();
		this.name = name;
		this.level = level;
		this.metamodels.add(metamodel);
		this.multilevelHierarchy = metamodel.getHierarchy();
		supplementaryModels = new HashSet<IModel>();
	}
	
	
	public Model(String name, Collection<IModel> metamodels, int level) {
		super();
		this.name = name;
		this.level = level;
		this.metamodels.addAll(metamodels);
		this.multilevelHierarchy = metamodels.iterator().next().getHierarchy();
		supplementaryModels = new HashSet<IModel>();
	}
	

	public void addOrReplaceNode(INode node) {
		nodes.remove(node);
		nodes.add(node);
		inEdges.computeIfAbsent(node, k -> new HashSet<IEdge>());
		outEdges.computeIfAbsent(node, k -> new HashSet<IEdge>());
	}
	

	public void addOrReplaceEdge(IEdge edge) {
		edges.remove(edge);
		edges.add(edge);
		inEdges.get(edge.getTarget()).add(edge);
		outEdges.get(edge.getSource()).add(edge);
	}
	

	@Override
	public IModel getMainMetamodel() {
		List<IModel> reverseSortedMetamodels = new ArrayList<IModel>(metamodels);
		Collections.sort(reverseSortedMetamodels, Collections.reverseOrder());
		return reverseSortedMetamodels.get(0);
	}


	@Override
	public Set<IModel> getAllMetamodels() {
		Set<IModel> allMetamodels = new HashSet<IModel>(metamodels);
		for (IModel metamodel : metamodels) {
			allMetamodels.addAll(metamodel.getAllMetamodels());
		}
		return allMetamodels;
	}
	
	
	@Override
	public Set<IModel> getSupplementaryModels() {
		return supplementaryModels;
	}
	
	
	@Override
	public IModel getSupplementaryModel(String modelName) {
		for (IModel iModel : supplementaryModels) {
			if (iModel.getName().equals(modelName))
				return iModel;
		}
		return null;
	}
	
	
	@Override
	public IModel getSupplementaryModelForHierarchy(MultilevelHierarchy supplementaryHierarchy) {
		for (IModel iModel : supplementaryModels) {
			if (iModel.getHierarchy().equals(supplementaryHierarchy))
				return iModel;
		}
		return null;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}


	public void setLevel(int level) {
		this.level = level;
	}
	
	
	public void addOrReplaceMetamodel(IModel metamodel) {
		metamodels.remove(metamodel);
		metamodels.add(metamodel);
	}
	
	
	public void removeMetamodel(IModel metamodel) {
		metamodels.remove(metamodel);
	}


	public void addSupplementaryModels(Set<IModel> supplementaryModels) {
		this.supplementaryModels = supplementaryModels;
	}

	
	public void addSupplementaryModel(IModel supplementaryModel) {
		supplementaryModels.add(supplementaryModel);
	}
	

	@Override
	public List<IModel> getBranch(boolean excludeEcore) {
		List<IModel> modelsInBranch = getMainMetamodel().getBranch(excludeEcore);
		modelsInBranch.add(this);
		return modelsInBranch;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj.getClass().equals(this.getClass())))
			return false;
		AbstractModel other = (AbstractModel) obj;
		if (level != other.level)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!other.multilevelHierarchy.getName().equals(this.multilevelHierarchy.name)) {
			return false;
		}
		return true;
	}
	
}
