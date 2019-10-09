package no.hvl.multecore.common.hierarchy;

import no.hvl.multecore.common.Constants;

public class Potency {
	
	private int start;
	private int end;
	private int depth;
	
	
	public Potency() {
		start = 1;
		end = 1;
		depth = Constants.UNBOUNDED;
	}
	
	
	public Potency(int start, int end, int depth) {
		this.start = start;
		this.end = end;
		this.depth = depth;
	}


	public int getStart() {
		return start;
	}


	public void setStart(int start) {
		this.start = start;
	}


	public int getEnd() {
		return end;
	}
	
	
	public void setEnd(int end) {
		this.end = end;
	}


	public int getDepth() {
		return depth;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}


	public boolean compatibleWithReversePotency(int reversePotency) {
		if (depth == 0)
			return false;
		if (reversePotency == Constants.UNBOUNDED)
			return true;
		if (start > reversePotency)
			return false;
		if (end == Constants.UNBOUNDED)
			return true;
		if (end < reversePotency)
			return false;
		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + depth;
		result = prime * result + end;
		result = prime * result + start;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Potency))
			return false;
		Potency other = (Potency) obj;
		if (depth != other.depth)
			return false;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}
}
