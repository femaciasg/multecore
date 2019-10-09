package no.hvl.multecore.common.hierarchy;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EcorePackage;

import no.hvl.multecore.common.Constants;

public class DeclaredAttribute extends AbstractAttribute implements Comparable<DeclaredAttribute> {

	private boolean isId;
	private int lowerBound;
	private int upperBound;
	
	
	public DeclaredAttribute(String name, IAttribute type, INode containingNode) {
		this.nameOrValue = name;
		this.type = type;
		this.containingNode = containingNode;
		this.potency = new Potency(1,1,1);
	}
	
	
	public DeclaredAttribute(String name, IAttribute type, INode containingNode, Potency potency) {
		this.nameOrValue = name;
		this.type = type;
		this.containingNode = containingNode;
		this.potency = potency;
	}
	
	
	public boolean isId() {
		return isId;
	}
	
	
	public void setId(boolean isId) {
		this.isId = isId;
	}

	
	public int getLowerBound() {
		return lowerBound;
	}


	public int getUpperBound() {
		return upperBound;
	}

	
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}


	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}


	@Override
	public int compareTo(DeclaredAttribute otherAttribute) {
		return nameOrValue.compareTo(otherAttribute.getNameOrValue());
	}
	

	// Better way of doing this?
	public enum NativeType implements IAttribute {
		Integer,
		Real,
		Boolean,
		String;
		
		private static Map<NativeType, List<EDataType>> dataTypeEquivalences;
		static {
			EcorePackage ePackageInstance = EcorePackage.eINSTANCE;
			dataTypeEquivalences = new LinkedHashMap<DeclaredAttribute.NativeType, List<EDataType>>();
			dataTypeEquivalences.put(Integer, Arrays.asList(ePackageInstance.getEInt()));
			dataTypeEquivalences.put(Real, Arrays.asList(ePackageInstance.getEFloat(),ePackageInstance.getEDouble(),ePackageInstance.getELong()));
			dataTypeEquivalences.put(Boolean, Arrays.asList(ePackageInstance.getEBoolean()));
			dataTypeEquivalences.put(String, Arrays.asList(ePackageInstance.getEString()));
		};
		

		@Override
		public String getNameOrValue() {
			return name();
		}


		@Override
		public IAttribute getType() {
			return null;
		}

		
		@Override
		public INode getContainingNode() {
			return null;
		}

		
		@Override
		public IModel getModel() {
			return null;
		}

		
		@Override
		public Potency getPotency() {
			return new Potency(1, Constants.UNBOUNDED, 2);
		}

		
		public EDataType getEDataType() {
			return dataTypeEquivalences.get(this).get(0);
		}
		
		
		public static NativeType getNativeType(EDataType eDataType) {
			for (Entry<NativeType, List<EDataType>> entry : dataTypeEquivalences.entrySet() ) {
				if (entry.getValue().contains(eDataType))
					return entry.getKey();
			}
			return String; // "Safe" default, but this should never happen
		}
		
		
		public Object getValueWithCorrectType(String value) {
			switch(this) {
			case Integer:
				return new java.lang.Integer(value);
			case Real:
				return new Float(value);
			case Boolean:
				return new java.lang.Boolean(value);
			case String:
			default:
				return value;
			}
		}
		
	}
	
}
