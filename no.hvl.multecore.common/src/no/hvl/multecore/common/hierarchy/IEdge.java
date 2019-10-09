package no.hvl.multecore.common.hierarchy;

public interface IEdge {
	
	public String getName();

	public IEdge getType();

	public INode getSource();
	
	public INode getTarget();

	public int getLowerBound();

	public int getUpperBound();
	
	public boolean isContainment();

	public IModel getModel();

	public Potency getPotency();

}
