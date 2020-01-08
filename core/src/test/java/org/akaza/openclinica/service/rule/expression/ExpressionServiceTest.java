/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.service.rule.expression;

import junit.framework.TestCase;

public class ExpressionServiceTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    public void testStatement() {

        org.apache.commons.dbcp.BasicDataSource ds = new org.apache.commons.dbcp.BasicDataSource();
        ExpressionService expressionService = new ExpressionService(ds);

        // Syntax
        assertEquals(false, expressionService.checkSyntax("StudyEventName[ALL].FormName1.ItemGroupName[ALL].ItemName11."));
        assertEquals(false, expressionService.checkSyntax(".StudyEventName[ALL].FormName1.ItemGroupName[ALL].ItemName11."));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL]..ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].ITEM_GROUP_OID[ALL]..ITEM_OID_11"));

        // STUDY_EVENT_DEFINITION_OID
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[123].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[10].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[1].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[10004].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID_12[123].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY12_EVENT_OID_12[123].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID.FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[.FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[Krikor].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[KK].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVE[NT_OID[].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("[].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("[.FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID$[12].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[0].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[123].FORM_OID.ITEM_GROUP_OID[0].ITEM_OID_11"));

        // CRF_OID or CRF_VERSION_OID
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID_.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID_123.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM123_2__OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID[.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID[ALL].ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID[].ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID].ITEM_GROUP_OID[ALL].ITEM_OID_11"));

        // ITEM_GROUP_OID
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[23].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[2].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[100].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID.ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID__[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID_123[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.12_ITEM_GROUP_12_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.12_iTEM_GROUP_12_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALLL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL]$.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL]_KK.ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[0].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[0].ITEM_OID_11"));

        // ITEM_OID
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OI123D_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITE123M_OID_11"));
        assertEquals(true, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11["));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11]"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11[]"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OID_11$"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM_OId_11"));
        assertEquals(false, expressionService.checkSyntax("STUDY_EVENT_OID[ALL].FORM_OID.ITEM_GROUP_OID[ALL].ITEM-_OID_11"));

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // MockContextFactory.revertSetAsInitial();
    }
}
