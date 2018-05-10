/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.service;

import java.io.Serializable;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class DiscrepancyNotesSummary implements Serializable {

    private static final long serialVersionUID = 3456220240618564309L;

    private int total = 0;

    private final Integer[][] table;

    private final Integer[] resolutionStatusSum;

    private final Integer[] discrepancyNoteTypeSum;

    public DiscrepancyNotesSummary(Integer[][] table) {
        this.table = table;
        this.resolutionStatusSum = new Integer[table.length];
        this.discrepancyNoteTypeSum = new Integer[table[0].length];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                Integer sum = table[i][j];
                if (sum != null) {
                    resolutionStatusSum[i] = zeroIfNull(resolutionStatusSum[i]) + sum;
                    discrepancyNoteTypeSum[j] = + zeroIfNull(discrepancyNoteTypeSum[j]) + sum;
                    total = zeroIfNull(total) + sum;
                }
            }
        }
    }

    public int getSum(ResolutionStatus r, DiscrepancyNoteType t) {
        return zeroIfNull(table[r.getId()][t.getId()]);
    }

    public int getSum(ResolutionStatus r) {
        return zeroIfNull(resolutionStatusSum[r.getId()]);
    }

    public int getSum(DiscrepancyNoteType t) {
        return zeroIfNull(discrepancyNoteTypeSum[t.getId()]);
    }

    public int getTotal() {
        return total;
    }

    private int zeroIfNull(Integer i) {
        return (i == null) ? 0 : i;
    }

}
