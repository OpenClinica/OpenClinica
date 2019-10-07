package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.rule.expression.Context;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.IntegerType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.internal.util.ReflectHelper;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A generic UserType that handles String-based JDK 5.0 Enums.
 *
 * @author Gavin King
 */
public class IntegerEnumUserType implements EnhancedUserType, ParameterizedType {

    private Class<Context> enumClass;

    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassname");
        try {
            enumClass = ReflectHelper.classForName(enumClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }
    }

    public Class returnedClass() {
        return enumClass;
    }

    public int[] sqlTypes() {
        return new int[] { IntegerType.INSTANCE.sqlType() };
    }

    public boolean isMutable() {
        return false;
    }

    public Object deepCopy(Object value) {
        return value;
    }

    public Serializable disassemble(Object value) {
        return (Context) value;
    }

    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    public boolean equals(Object x, Object y) {
        return x == y;
    }

    public int hashCode(Object x) {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {

    }

    public Object fromXMLString(String xmlValue) {
        return Enum.valueOf(enumClass, xmlValue);
    }

    public String objectToSQLString(Object value) {
        return '\'' + ((Context) value).getCode().toString() + '\'';
    }

    public String toXMLString(Object value) {
        return ((Context) value).getCode().toString();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException {
        String name = rs.getString(names[0]);
        // return rs.wasNull() ? null : Enum.valueOf(enumClass, name);
        return rs.wasNull() ? null : Context.getByCode(Integer.parseInt(name));
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException {
        if (value == null) {
            st.setNull(index, IntegerType.INSTANCE.sqlType());
        } else {
            // st.setString(index, ((Enum) value).name());
            st.setInt(index, ((Context) value).getCode());
        }
    }

}
