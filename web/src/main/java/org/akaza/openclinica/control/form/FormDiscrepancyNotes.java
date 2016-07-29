/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;

/**
 * @author jxu
 *
 * 
 */
public class FormDiscrepancyNotes implements Serializable{
    private HashMap fieldNotes;
    private HashMap numExistingFieldNotes;
    private HashMap idNotes;

    public FormDiscrepancyNotes() {
        fieldNotes = new HashMap();
        numExistingFieldNotes = new HashMap();
        idNotes = new HashMap();
    }

    public void addNote(String field, DiscrepancyNoteBean note) {
        ArrayList notes;
        if (fieldNotes.containsKey(field)) {
            notes = (ArrayList) fieldNotes.get(field);
        } else {
            notes = new ArrayList();
        }

        notes.add(note);
        //System.out.println("after adding note:" + notes.size());
        fieldNotes.put(field, notes);
    }

    /** want to map entity Id with field names
     * So we know if an entity has discrepancy note giving entity id
     * @param entityId
     * @param field
     */
    public void addIdNote(int entityId, String field) {
        ArrayList notes;
        if (idNotes.containsKey(entityId)) {
            notes = (ArrayList) fieldNotes.get(entityId);
        } else {
            notes = new ArrayList();
        }
        if (notes != null) {
            notes.add(field);
        }
        idNotes.put(new Integer(entityId), notes);
    }

    public boolean hasNote(String field) {
        ArrayList notes;
        if (fieldNotes.containsKey(field)) {
            notes = (ArrayList) fieldNotes.get(field);
            return notes != null && notes.size() > 0;
        }
        return false;
    }

    public ArrayList getNotes(String field) {
        ArrayList notes;
        if (fieldNotes.containsKey(field)) {
            notes = (ArrayList) fieldNotes.get(field);
        } else {
            notes = new ArrayList();
        }
        return notes;
    }

    public void setNumExistingFieldNotes(String field, int num) {
        numExistingFieldNotes.put(field, new Integer(num));
    }

    public int getNumExistingFieldNotes(String field) {
        if (numExistingFieldNotes.containsKey(field)) {
            Integer numInt = (Integer) numExistingFieldNotes.get(field);
            if (numInt != null) {
                return numInt.intValue();
            }
        }
        return 0;
    }

    /**
     * @return Returns the numExistingFieldNotes.
     */
    public HashMap getNumExistingFieldNotes() {
        return numExistingFieldNotes;
    }

    /**
     * @return the fieldNotes
     */
    public HashMap getFieldNotes() {
        return fieldNotes;
    }

    /**
     * @param fieldNotes the fieldNotes to set
     */
    public void setFieldNotes(HashMap fieldNotes) {
        this.fieldNotes = fieldNotes;
    }

    /**
     * @param numExistingFieldNotes the numExistingFieldNotes to set
     */
    public void setNumExistingFieldNotes(HashMap numExistingFieldNotes) {
        this.numExistingFieldNotes = numExistingFieldNotes;
    }

    /**
     * @return the idNotes
     */
    public HashMap getIdNotes() {
        return idNotes;
    }

    /**
     * @param idNotes the idNotes to set
     */
    public void setIdNotes(HashMap idNotes) {
        this.idNotes = idNotes;
    }
}