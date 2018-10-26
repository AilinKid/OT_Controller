/*
 * OtterTune - DBCollector.java
 *
 * Copyright (c) 2017-18, Carnegie Mellon University Database Group
 */

package com.company.controller.collectors;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;


public class DBCollector implements DBParameterCollector {

    private static final Logger LOG = Logger.getLogger(DBCollector.class);
    //java util logger
    //private static final Logger LOG = Logger.getLogger("string");

    protected static final String JSON_GLOBAL_KEY = "global";
    protected static final String JSON_LOCAL_KEY = "local";


    //内存中使用两个treemap维护参数和度量
    protected final Map<String, String> dbParameters = new TreeMap<String, String>();

    protected final Map<String, String> dbMetrics = new TreeMap<String, String>();

    protected final StringBuilder version = new StringBuilder();

    @Override
    public boolean hasParameters() {
    return (dbParameters.isEmpty() == false);
  }

    @Override
    public boolean hasMetrics() {
    return (dbMetrics.isEmpty() == false);
  }

    //返回json格式化
    @Override
    public String collectParameters() {
    //return JSONUtil.format(JSONUtil.toJSONString(dbParameters));
        return "";
  }

    @Override
    public String collectMetrics() {

        //return JSONUtil.format(JSONUtil.toJSONString(dbMetrics));
        return "";
  }

    @Override
    public String collectVersion() {

        //return version.toString();
        return "";
  }
}
