package no.hvl.multecore.core;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import no.hvl.multecore.common.exceptions.MultEcoreException;
import no.hvl.multecore.common.hierarchy.IModel;

import org.xml.sax.SAXException;

public interface ITransformer {

	public String getModelName();
	
	public IModel getIModel();
    
    public void transform() throws ParserConfigurationException, SAXException, IOException, MultEcoreException;

}
