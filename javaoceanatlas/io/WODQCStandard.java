/**
 * 
 */
package javaoceanatlas.io;


/**
 * @author oz
 *
 */
public enum WODQCStandard {
	NONE("NONE", -1, new NullQCTranslator()),
	NODC("NODC", 0, new NODCQCTranslator()),
	WOCE("WOCE", 1, new WOCEQCTranslator()),
	GTSPP("GTSPP", 3, new GTSPPQCTranslator()),
	GEOSECS("GEOSECS", 5, new GEOSECSQCTranslator()),
	CalCOFI("CalCOFI", 6, new CalCOFIQCTranslator()), 
	WILKES("Wilkes Land Expedition", 7, new WilkesQCTranslator()),
	OMEXA0001018("OMEX & Accession: 0001018", 8, new OMEXQCTranslator()),
	A0000440("Accession:0000440", 9, new A0000440QCTranslator()),
	A0001086("Accession:0001086", 10, new A0001086QCTranslator()),
	TAO_PIRATA("TAO/PIRATA", 11, new TAOQCTranslator()),
	ARGO("Argo", 12, new ArgoQCTranslator()),
	WMO("WMO", 13, new WMOQCTranslator());
	
	private final String mQCName;
	private final int mNODCCode;
	private QCTranslator mInstalledTranslator;

	WODQCStandard(String qcName, int NODCCode, QCTranslator qcTrans) {
		this.mQCName = qcName;
		this.mNODCCode = NODCCode;
		this.mInstalledTranslator = qcTrans;
	}
	
	public static QCTranslator translatorFromString(String qcCode) {
    for (WODQCStandard code : values()) {
      if (code.mQCName.equalsIgnoreCase(qcCode)) {
        return code.mInstalledTranslator;
      }
    }
		return new NullQCTranslator();
	}
	
  public static WODQCStandard fromString(String qcCode) {
    for (WODQCStandard code : values()) {
      if (code.mQCName.equals(qcCode)) {
        return code;
      }
    }
    return NONE;
  }
	
  public static WODQCStandard fromIntCode(int qcCode) {
    for (WODQCStandard code : values()) {
      if (code.mNODCCode == qcCode) {
        return code;
      }
    }
    return NONE;
  }
}

