/*
 * OtterTune - DBParameterCollector.java
 *
 * Copyright (c) 2017-18, Carnegie Mellon University Database Group
 */

package com.company.controller.collectors;

public interface DBParameterCollector {

    //数据库参数
    //数据库的度量

    boolean hasParameters();

    boolean hasMetrics();

    String collectParameters();

    String collectMetrics();

    String collectVersion();
}
