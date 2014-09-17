package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

/**
 * Utility operations for Hibernate manipulation
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class HibernateUtil {

    /**
     * Executes a query to retrieve a list of identifiers (normally primary keys). As Hibernate may return different
     * data types, this method converts all IDs to {@link Integer}.
     *
     * @param query Query to fetch a list of IDs
     * @return Filtered list of IDs
     * @throws IllegalArgumentException When query returns an object that is not a {@link Number}.
     */
    public static List<Integer> queryIDsList(Query query) {
        //TODO - Doug - change return type to List<Long>

        // queryResult may contain Int, Long or BigDecimal
        @SuppressWarnings("rawtypes")
        List queryResult = query.list();

        List<Integer> result = new ArrayList<Integer>(queryResult.size());
        for (Object o: queryResult) {
            if (o instanceof Number) {
                Number n = (Number) o;
                result.add(n.intValue());
            } else {
                throw new IllegalArgumentException("Query returned " + o.getClass() + ", which is not a valid ID");
            }
        }

        return result;
    }

}
