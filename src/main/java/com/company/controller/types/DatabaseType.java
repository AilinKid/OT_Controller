/*
 * OtterTune - DatabaseType.java
 *
 * Copyright (c) 2017-18, Carnegie Mellon University Database Group
 */

package com.company.controller.types;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/** Database Type. */
public enum DatabaseType {

    //java 枚举类型，支持自定义方法和变量

    /** 四种枚举类型 Parameters: (1) JDBC Driver String */
    MYSQL("com.mysql.jdbc.Driver"),
    MYROCKS("com.mysql.jdbc.Driver"),
    POSTGRES("org.postgresql.Driver"),
    SAPHANA("com.sap.db.jdbc.Driver");

    /**
     * This is the suggested driver string to use in the configuration xml This corresponds to the
     * <B>'driver'</b> attribute.
     */
    private final String driver;


    private DatabaseType(String driver) {
    this.driver = driver;
  }



    // ---------------------------------------------------------------
    // ACCESSORS
    // ----------------------------------------------------------------

    /**
     * Returns the suggested driver string to use for the given database type
     *
     * @return
     */
    public String getSuggestedDriver() {
    return (this.driver);
  }

    // ----------------------------------------------------------------
    // STATIC METHODS + MEMBERS
    // ----------------------------------------------------------------

    protected static final Map<Integer, DatabaseType> idx_lookup =
        new HashMap<Integer, DatabaseType>();

    protected static final Map<String, DatabaseType> name_lookup =
        new HashMap<String, DatabaseType>();


    //static 静态代码块，是优先于构造函数执行的，用于static变量的赋值
    static {
        for (DatabaseType vt : EnumSet.allOf(DatabaseType.class)) {
            //EnumSet.allOf返回所有枚举类型的元素
            //对于每个元素的.ordinal()可返回，该元素的序号，vt是枚举类型
            DatabaseType.idx_lookup.put(vt.ordinal(), vt);
            //.name()返回string，vt仍然是枚举类型
            DatabaseType.name_lookup.put(vt.name().toUpperCase(), vt);
        }
    }
    public static DatabaseType get(String name) {
        DatabaseType ret = DatabaseType.name_lookup.get(name.toUpperCase());
        return (ret);
    }
}
