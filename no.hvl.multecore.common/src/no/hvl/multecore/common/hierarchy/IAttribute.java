package no.hvl.multecore.common.hierarchy;

public interface IAttribute {

	public String getNameOrValue();
	
	public IAttribute getType();
	
	public INode getContainingNode();

	public IModel getModel();

	public Potency getPotency();

}
