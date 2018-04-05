package org.akaza.openclinica.bean.admin;



public enum JDBCType {

    
    BIT(Types.BIT),
    
    TINYINT(Types.TINYINT),
    
    SMALLINT(Types.SMALLINT),
   
    INTEGER(Types.INTEGER),
   
    BIGINT(Types.BIGINT),
   
    FLOAT(Types.FLOAT),
    
    REAL(Types.REAL),
   
    DOUBLE(Types.DOUBLE),
    
    NUMERIC(Types.NUMERIC),
   
    DECIMAL(Types.DECIMAL),
   
    CHAR(Types.CHAR),
    
    VARCHAR(Types.VARCHAR),
   
    LONGVARCHAR(Types.LONGVARCHAR),
    
    DATE(Types.DATE),
   
    TIME(Types.TIME),
    
    TIMESTAMP(Types.TIMESTAMP),
    
    BINARY(Types.BINARY),
   
    VARBINARY(Types.VARBINARY),
    
    LONGVARBINARY(Types.LONGVARBINARY),
   
    NULL(Types.NULL),
    /**
     * Indicates that the SQL type
     * is database-specific and gets mapped to a Java object that can be
     * accessed via the methods getObject and setObject.
     */
    OTHER(Types.OTHER),
    /**
     * Indicates that the SQL type
     * is database-specific and gets mapped to a Java object that can be
     * accessed via the methods getObject and setObject.
     */
    JAVA_OBJECT(Types.JAVA_OBJECT),
    /**
     * Identifies the generic SQL type {@code DISTINCT}.
     */
    DISTINCT(Types.DISTINCT),
    /**
     * Identifies the generic SQL type {@code STRUCT}.
     */
    STRUCT(Types.STRUCT),
    /**
     * Identifies the generic SQL type {@code ARRAY}.
     */
    ARRAY(Types.ARRAY),
    /**
     * Identifies the generic SQL type {@code BLOB}.
     */
    BLOB(Types.BLOB),
    /**
     * Identifies the generic SQL type {@code CLOB}.
     */
    CLOB(Types.CLOB),
    /**
     * Identifies the generic SQL type {@code REF}.
     */
    REF(Types.REF),
    /**
     * Identifies the generic SQL type {@code DATALINK}.
     */
    DATALINK(Types.DATALINK),
    /**
     * Identifies the generic SQL type {@code BOOLEAN}.
     */
    BOOLEAN(Types.BOOLEAN),

    /* JDBC 4.0 Types */

    /**
     * Identifies the SQL type {@code ROWID}.
     */
    ROWID(Types.ROWID),
    /**
     * Identifies the generic SQL type {@code NCHAR}.
     */
    NCHAR(Types.NCHAR),
    /**
     * Identifies the generic SQL type {@code NVARCHAR}.
     */
    NVARCHAR(Types.NVARCHAR),
    /**
     * Identifies the generic SQL type {@code LONGNVARCHAR}.
     */
    LONGNVARCHAR(Types.LONGNVARCHAR),
    /**
     * Identifies the generic SQL type {@code NCLOB}.
     */
    NCLOB(Types.NCLOB),
    /**
     * Identifies the generic SQL type {@code SQLXML}.
     */
    SQLXML(Types.SQLXML),

    /* JDBC 4.2 Types */

    /**
     * Identifies the generic SQL type {@code REF_CURSOR}.
     */
    REF_CURSOR(Types.REF_CURSOR),

    /**
     * Identifies the generic SQL type {@code TIME_WITH_TIMEZONE}.
     */
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE),

    /**
     * Identifies the generic SQL type {@code TIMESTAMP_WITH_TIMEZONE}.
     */
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE);

    /**
     * The Integer value for the JDBCType.  It maps to a value in
     * {@code Types.java}
     */
    private Integer type;

    /**
     * Constructor to specify the data type value from {@code Types) for
     * this data type.
     * @param type The value from {@code Types) for this data type
     */
    JDBCType(final Integer type) {
        this.type = type;
    }

    /**
     *{@inheritDoc }
     * @return The name of this {@code SQLType}.
     */
    public String getName() {
        return name();
    }
   
    /**
     * Returns the vendor specific type number for the data type.
     * @return  An Integer representing the data type. For {@code JDBCType},
     * the value will be the same value as in {@code Types} for the data type.
     */
    public Integer getVendorTypeNumber() {
        return type;
    }
    /**
     * Returns the {@code JDBCType} that corresponds to the specified
     * {@code Types} value
     * @param type {@code Types} value
     * @return The {@code JDBCType} constant
     * @throws IllegalArgumentException if this enum type has no constant with
     * the specified {@code Types} value
     * @see Types
     */
    public static JDBCType valueOf(int type) {
        for( JDBCType sqlType : JDBCType.class.getEnumConstants()) {
            if(type == sqlType.type)
                return sqlType;
        }
        throw new IllegalArgumentException("Type:" + type + " is not a valid "
                + "Types.java value.");
    }
}
