package no.hvl.multecore.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

public interface ITransformerFromHierarchy extends ITransformer {

    public void save() throws IOException, TransformerException;
	
}
