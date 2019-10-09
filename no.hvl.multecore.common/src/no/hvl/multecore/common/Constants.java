package no.hvl.multecore.common;

public abstract class Constants {

	public static boolean DEBUGGING = true;
	public static boolean USER_SAVED_CHANGES = false;

	public static final String ECLASS_ID = "EClass";
	public static final String EREFERENCE_ID = "EReference";

	public static final String METAMODELS_ATTRIBUTE_ID = "metamodels";
	public static final String SUPPLEMENTARY_METAMODELS_ATTRIBUTE_ID = "supplementaries";
	public static final String LEVEL_ATTRIBUTE_ID = "level";
	public static final String NAME_ATTRIBUTE_ID = "name";
	public static final String RELATION_NAMES_ATTRIBUTE_ID = "relationNames";
	public static final String POTENCY_ATTRIBUTE_ID = "potency";
	public static final String PARENT_NODES_ATTRIBUTE_ID = "parentNodes";
	public static final String IS_ABSTRACT_ATTRIBUTE_ID = "isAbstract";
	public static final String SUPPLEMENTARY_NODES_ATTRIBUTE_ID = "supplementaryNodes";
	public static final String IS_ID_ATTRIBUTE_ID = "isId";
	public static final String ATTRIBUTE_NAMES_ATTRIBUTE_ID = "attributeNames";
	public static final String ROOT_ID = "Root";

	private static final String SERIALIZATION_ROOT_CONTAINMENT_PREFIX_ID = "contains";

	private static final String REGEX_FORMAT_WORD = "%s";
	private static final String REGEX_PARSE_WORD = "([-\\\\\\w]+|\\\\\\*)";
	private static final String REGEX_FORMAT_NUMBER = "%d";
	private static final String REGEX_PARSE_NUMBER = "(\\\\\\W??\\\\\\d+)";

	public static final boolean DEBUG = true;

	public static final String TRUE = "true";
	public static final String FALSE = "false";

	public static final String ECORE_ID = "Ecore";
	public static final String CPN_ID = "CPN";

	public static final String EMPTY_STRING = "";
	public static final String SYNTHETIC_PREFIX = "__";
	public static final String METAMODELS_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + METAMODELS_ATTRIBUTE_ID;
	public static final String SUPPLEMENTARY_METAMODELS_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + SUPPLEMENTARY_METAMODELS_ATTRIBUTE_ID;
	public static final String LEVEL_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + LEVEL_ATTRIBUTE_ID;
	public static final String NAME_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + NAME_ATTRIBUTE_ID;
	public static final String RELATION_NAMES_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + RELATION_NAMES_ATTRIBUTE_ID;
	public static final String NAMES_ASSOCIATOR = "=";
	public static final String NAMES_SEPARATOR = ";";
	public static final String VALUES_SEPARATOR = ",";
	public static final String POTENCY_SEPARATOR = "-";
	public static final String SUPPLEMENTARY_SEPARATOR = ":";
	public static final String TYPE_POTENCY_SEPARATOR = "@";
	public static final String RELATION_NAMES_REGEX_FORMAT = REGEX_FORMAT_WORD + VALUES_SEPARATOR + REGEX_FORMAT_WORD + NAMES_ASSOCIATOR + REGEX_FORMAT_WORD + VALUES_SEPARATOR + REGEX_FORMAT_NUMBER + VALUES_SEPARATOR + REGEX_FORMAT_NUMBER + VALUES_SEPARATOR + REGEX_FORMAT_WORD + POTENCY_SEPARATOR + REGEX_FORMAT_WORD + POTENCY_SEPARATOR + REGEX_FORMAT_WORD + VALUES_SEPARATOR + REGEX_FORMAT_WORD;
	public static final String RELATION_NAMES_REGEX_PARSE = new String(RELATION_NAMES_REGEX_FORMAT.replaceAll(REGEX_FORMAT_WORD, REGEX_PARSE_WORD).replaceAll(REGEX_FORMAT_NUMBER, REGEX_PARSE_NUMBER)); 
	public static final String ATTRIBUTE_NAMES_REGEX_FORMAT = REGEX_FORMAT_WORD + NAMES_ASSOCIATOR + REGEX_FORMAT_WORD + VALUES_SEPARATOR + REGEX_FORMAT_NUMBER + VALUES_SEPARATOR + REGEX_FORMAT_NUMBER + VALUES_SEPARATOR + REGEX_FORMAT_WORD + POTENCY_SEPARATOR + REGEX_FORMAT_WORD + VALUES_SEPARATOR + REGEX_FORMAT_WORD;
	public static final String ATTRIBUTE_NAMES_REGEX_PARSE = new String(ATTRIBUTE_NAMES_REGEX_FORMAT.replaceAll(REGEX_FORMAT_WORD, REGEX_PARSE_WORD).replaceAll(REGEX_FORMAT_NUMBER, REGEX_PARSE_NUMBER));
	public static final String POTENCY_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + POTENCY_ATTRIBUTE_ID;
	public static final String PARENT_NODES_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + PARENT_NODES_ATTRIBUTE_ID;
	public static final String IS_ABSTRACT_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + IS_ABSTRACT_ATTRIBUTE_ID;
	public static final String SUPPLEMENTARY_NODES_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + SUPPLEMENTARY_NODES_ATTRIBUTE_ID;
	public static final String ATTRIBUTE_NAMES_ATTRIBUTE_NAME = SYNTHETIC_PREFIX + ATTRIBUTE_NAMES_ATTRIBUTE_ID;

	public static final int UNBOUNDED = -1;
	public static final int UNSPECIFIED = -2;
	public static final String METAMODELS_ATTRIBUTE_DEFAULT_VALUE = "";
	public static final String NAME_ATTRIBUTE_DEFAULT_VALUE = "";
	public static final String RELATION_NAMES_ATTRIBUTE_DEFAULT_VALUE = "";
	public static final String ATTRIBUTE_NAMES_ATTRIBUTE_DEFAULT_VALUE = "";
	public static final String SUPPLEMENTARY_NODES_ATTRIBUTE_DEFAULT_VALUE = "";
	public static final int POTENCY_DEFAULT_VALUE = 1;
	public static final int TYPE_REVERSE_POTENCY_DEFAULT_VALUE = POTENCY_DEFAULT_VALUE;
	public static final int START_POTENCY_DEFAULT_VALUE = POTENCY_DEFAULT_VALUE;
	public static final int END_POTENCY_DEFAULT_VALUE = POTENCY_DEFAULT_VALUE;
	public static final int DEPTH_POTENCY_DEFAULT_VALUE = UNBOUNDED;
	public static final int RELATION_LOWERBOUND_DEFAULT_VALUE = 0;
	public static final int RELATION_UPPERBOUND_DEFAULT_VALUE = UNBOUNDED;
	public static final int ATTRIBUTE_LOWERBOUND_DEFAULT_VALUE = 0;
	public static final int ATTRIBUTE_UPPERBOUND_DEFAULT_VALUE = UNSPECIFIED;
	public static final Boolean RELATION_IS_CONTAINMENT_DEFAULT_VALUE = new Boolean(false);
	public static final Boolean IS_ABSTRACT_DEFAULT_VALUE = new Boolean(false);
	public static final Boolean ATTRIBUTE_IS_ID_DEFAULT_VALUE = new Boolean(false);
	public static final int LEVEL_DEFAULT_VALUE = 1;
	
	public static final String POTENCY_ATTRIBUTE_DEFAULT_VALUE = String.valueOf(POTENCY_DEFAULT_VALUE);
	public static final String POTENCY_ATTRIBUTE_UNBOUNDED_VALUE = "*";
	public static final String START_POTENCY_ATTRIBUTE_DEFAULT_VALUE = String.valueOf(START_POTENCY_DEFAULT_VALUE);
	public static final String END_POTENCY_ATTRIBUTE_DEFAULT_VALUE = String.valueOf(END_POTENCY_DEFAULT_VALUE);
	public static final String NODE_TYPE_ATTRIBUTE_DEFAULT_VALUE = ECLASS_ID;
	public static final String RELATION_TYPE_ATTRIBUTE_DEFAULT_VALUE = EREFERENCE_ID;
	public static final String RELATION_LOWERBOUND_ATTRIBUTE_DEFAULT_VALUE = String.valueOf(RELATION_LOWERBOUND_DEFAULT_VALUE);
	public static final String RELATION_UPPERBOUND_ATTRIBUTE_DEFAULT_VALUE = String.valueOf(RELATION_UPPERBOUND_DEFAULT_VALUE);
	public static final String PARENT_NODES_ATTRIBUTE_DEFAULT_VALUE = "";
	public static final String IS_ABSTRACT_ATTRIBUTE_DEFAULT_VALUE = IS_ABSTRACT_DEFAULT_VALUE.toString();
	public static final String IS_ID_ATTRIBUTE_DEFAULT_VALUE = ATTRIBUTE_IS_ID_DEFAULT_VALUE.toString();
	public static final String LEVEL_PREFIX = "level";
	public static final String ONTOLOGICAL_METAMODEL_PREFIX = "om";
	public static final String LINGUISTIC_METAMODEL_PREFIX = "lm";
	
	public static final String NODE_NAME_ROOT = ROOT_ID;  // The root node is considered synthetic at the moment, but EMF does not accept the name "__Root"
	public static final String NODE_NAME_MODEL = "Model";
	public static final String NODE_NAME_ELEMENTS = "Elements";
	public static final String NODE_NAME_RELATIONS = "Relations";

	public static final String SERIALIZATION_ROOT_CONTAINMENT_PREFIX = SYNTHETIC_PREFIX + SERIALIZATION_ROOT_CONTAINMENT_PREFIX_ID;
	public static final String SERIALIZATION_ATTRIBUTE_NAME_TYPE = "type";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_SUPTYPES = "suptypes";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_POTENCY = POTENCY_ATTRIBUTE_ID;
	public static final String SERIALIZATION_ATTRIBUTE_NAME_VALUE = "value";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_PARENT_NODES = PARENT_NODES_ATTRIBUTE_ID;
	public static final String SERIALIZATION_ATTRIBUTE_NAME_IS_ABSTRACT = IS_ABSTRACT_ATTRIBUTE_ID;
	public static final String SERIALIZATION_ATTRIBUTE_NAME_IS_ID = IS_ID_ATTRIBUTE_ID;
	public static final String SERIALIZATION_ATTRIBUTE_NAME_CONTAINMENT = "containment";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_LOWERBOUND = "lowerBound";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_UPPERBOUND = "upperBound";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_SOURCE = "source";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_TARGET = "target";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_POTENCY_START = "potencyStart";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_POTENCY_END = "potencyEnd";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_POTENCY_DEPTH = "potencyDepth";
	public static final String SERIALIZATION_ATTRIBUTE_NAME_POTENCY_REVERSE = "reversePotency";
	
	// Hierarchy and model names for the primitive types supplementary hierarchy
	public static final String PRIMITIVE_TYPES_HIERARCHY = "supplementaryPrimitiveTypes";
	
}
