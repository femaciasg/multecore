package no.hvl.multecore.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Constants {
	
	public static final String PROJECT_EXPLORER_VIEW_URI = "org.eclipse.ui.navigator.ProjectExplorer";

	public static final String URI_SEPARATOR = File.separator;
	public static final String URI_SEPARATOR_SERIALIZED = "/";
	public static final String FILE_EXTENSION_SEPARATOR = ".";
	public static final String IMPORTED_TYPE_SEPARATOR = "__";

	public static final String FILE_EXTENSION_MEF = "mef";
	public static final String FILE_EXTENSION_MEF_WITH_SEPARATOR = FILE_EXTENSION_SEPARATOR + FILE_EXTENSION_MEF;
	public static final String SERIALIZATION_FILE_NAME_PREFIX = ".";

	public static final String FILE_EXTENSION_MODEL = "xmi";
	public static final String FILE_EXTENSION_METAMODEL = "ecore";
	public static final String FILE_EXTENSION_MODEL_WITH_SEPARATOR = FILE_EXTENSION_SEPARATOR + FILE_EXTENSION_MODEL;
	public static final String FILE_EXTENSION_METAMODEL_WITH_SEPARATOR = FILE_EXTENSION_SEPARATOR + FILE_EXTENSION_METAMODEL;
	public static final String DESERIALIZATION_ATTRIBUTE_NAME_POTENCY = no.hvl.multecore.common.Constants.POTENCY_ATTRIBUTE_ID;
	public static final String DESERIALIZARION_FILE_SYNTHETIC_METAMODEL_SUFFIX = "_metamodel";
	
	public static final List<String> MLM_RELEVANT_EXTENSIONS_LIST = Arrays.asList(
			Constants.FILE_EXTENSION_MEF.toLowerCase(),
			Constants.FILE_EXTENSION_METAMODEL.toLowerCase(),
			Constants.FILE_EXTENSION_MODEL.toLowerCase());

}
