package org.akaza.openclinica.domain.enumsupport;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.IntegerType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 
 * A UserType to handle Coded Enumerations. Any Enum that is added to the
 * application and needs to be persisted using this method needs to do the 
 * following 
 * 
 * 1. Implement CodedEnum Interface
 * 2. A static method needs to be added "public static EnumType getByCode(Integer code) {}"
 * 3. Add the definition to typedefs.xml
 * 
 * @author Krikor Krumlian
 */
public class CodedEnumType implements EnhancedUserType, ParameterizedType {

    private Class<CodedEnum> enumClass;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @SuppressWarnings("unchecked")
    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassname");
        try {
            enumClass = ReflectHelper.classForName(enumClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }
    }

    /* 
     * Tells Hibernate what Java value type class is mapped by this userType
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class<CodedEnum> returnedClass() {
        return enumClass;
    }

    /* 
     * Tells Hibernate what SQL column types to use for DDL schema generation.
     * @see org.hibernate.usertype.UserType#sqlTypes()
     * 
     */
    public int[] sqlTypes() {
        return new int[] { IntegerType.INSTANCE.sqlType() };
    }

    /* 
     * Hibernate can make some minor performance optimizations for immutable types. This method 
     * tells Hibernate that this type is immutable.
     * @see org.hibernate.usertype.UserType#isMutable()
     * 
     */
    public boolean isMutable() {
        return false;
    }

    public Object deepCopy(Object value) {
        return value;
    }

    /* 
     * This method is called when Hibernate puts the object into a second-level cache.
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    /* This method does the opposite of what disassemble does. It can transform cached data into
     * an instance. 
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    /* 
     * Handles merging of detached object state.
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    /* 
     * This method compares the current property value to a previous snapshot and determines
     * whether the property is dirty and must be saved to the database.
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object x, Object y) {
        return x == y;
    }

    public int hashCode(Object x) {
        return x.hashCode();
    }

    /*
        * Retrieves the property value from the JDBC Result-Set. You can also access the owner of the component
        * if you need it for the conversion.
        * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
        */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        String key = rs.getString(strings[0]);
        return rs.wasNull() ? null : getByCode(key);
    }

    /*
    * This method writes the property value to the JDBC Prepared-Statement.
    * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
    */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, IntegerType.INSTANCE.sqlType());
        } else {
            Integer code = getCode(value);
            logger.debug("Binding '{}' to parameter: {}", code, index);
            st.setInt(index, code);
        }
    }

    public Object fromXMLString(String xmlValue) {
        return getByCode(xmlValue);
    }

    public String objectToSQLString(Object value) {
        return '\'' + getCodeAsString(value) + '\'';
    }

    public String toXMLString(Object value) {
        return getCodeAsString(value);
    }


    private Integer getCode(Object value) {
        return ((CodedEnum) value).getCode();
    }

    private String getCodeAsString(Object value) {
        return getCode(value).toString();
    }

    private Object getByCode(String key) {
        Object value = null;
        Method method = null;
        Integer theKey = null;
        try {
            theKey = Integer.valueOf(key);
            method = enumClass.getMethod("getByCode", Integer.class);
            value = method.invoke(null, theKey);
        } catch (NumberFormatException e) {
            throw new CodedEnumPersistenceException("Value passed in to this Method has wrong type " + method + " being passed " + theKey + " on value "
                + value, e);
        } catch (SecurityException e) {
            throw new CodedEnumPersistenceException("SecurityException on Method " + method + " being passed " + theKey + " on value " + value, e);
        } catch (NoSuchMethodException e) {
            throw new CodedEnumPersistenceException("Method not found " + method + " being passed " + theKey + " on value " + value, e);
        } catch (IllegalArgumentException e) {
            throw new CodedEnumPersistenceException("Could not call Method " + method + " being passed " + theKey + " on value " + value, e);
        } catch (IllegalAccessException e) {
            throw new CodedEnumPersistenceException("Don't have access to Method " + method + " being passed " + theKey + " on value " + value, e);
        } catch (InvocationTargetException e) {
            throw new CodedEnumPersistenceException("InvocationTargetException on Method " + method + " being passed " + theKey + " on value " + value, e);
        }
        return value;
    }

}
