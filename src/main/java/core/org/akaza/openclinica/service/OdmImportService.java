package core.org.akaza.openclinica.service;

import java.util.List;
import java.util.Map;

import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.Page;
import org.cdisc.ns.odm.v130.ODM;

public interface OdmImportService {

	/**
	 * @param publicStudy
	 */
	public void updatePublicStudyPublishedFlag(Study publicStudy);

	/**
	 * @param map
	 * @param accessToken
	 */
	public void setPublishedVersionsInFM(Map<String, Object> map, String accessToken);

	/**
	 * @param odm
	 * @param page
	 * @param boardId
	 * @param accessToken
	 * @return
	 */
	public Map<String, Object> importOdm(ODM odm, List<Page> pages, String boardId, String accessToken, UserAccount userAccount) throws Exception;

	/**
	 * This method will be used to change the Status of Site Definition into DELETED
	 * 
	 * @param edcId
	 * @param updateId
	 */
	public void removeSiteDefinitions(Integer edcId, Integer updateId);

	/**
	 * This method will be used to change the Status of Site Definition into AVAILABLE
	 * 
	 * @param edcId
	 * @param updateId
	 */
	public void restoreSiteDefinitions(Integer edcId, Integer updateId);

}
