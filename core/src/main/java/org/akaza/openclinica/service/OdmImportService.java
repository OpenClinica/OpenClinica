package org.akaza.openclinica.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.domain.datamap.Study;
import org.cdisc.ns.odm.v130.ODM;

public interface OdmImportService {

	/**
	 * @param publicStudy
	 */
	public void updatePublicStudyPublishedFlag(Study publicStudy);

	/**
	 * @param map
	 * @param request
	 */
	public void setPublishedVersionsInFM(Map<String, Object> map, HttpServletRequest request);

	/**
	 * @param publishDTO
	 * @param boardId
	 * @param request
	 * @return
	 */
	public Map<String, Object> importOdm(ODM odm, Page page, String boardId, HttpServletRequest request);

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
