package no.hvl.multecore.common.hierarchy;

public abstract class AbstractEdge implements IEdge {

	protected String name;
	protected IEdge type;
	protected INode sourceNode;
	protected INode targetNode;
	protected int lowerBound;
	protected int upperBound;
	protected IModel model;
	protected Potency potency;
	
	
	@Override
	public String getName() {
		return name;
	}

	
	@Override
	public IEdge getType() {
		return type;
	}

	
	@Override
	public INode getSource() {
		return sourceNode;
	}

	
	@Override
	public INode getTarget() {
		return targetNode;
	}

	
	@Override
	public int getLowerBound() {
		return lowerBound;
	}

	
	@Override
	public int getUpperBound() {
		return upperBound;
	}

	
	@Override
	public IModel getModel() {
		return model;
	}

	
	@Override
	public Potency getPotency() {
		return potency;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
}
