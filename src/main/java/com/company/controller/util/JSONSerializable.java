/*
 * OtterTune - JSONSerializable.java
 *
 * Copyright (c) 2017-18, Carnegie Mellon University Database Group
 */

package com.company.controller.util;

import com.company.controller.util.json.JSONException;
import com.company.controller.util.json.JSONObject;
import com.company.controller.util.json.JSONString;
import com.company.controller.util.json.JSONStringer;

import java.io.IOException;

public interface JSONSerializable extends JSONString {
  public void save(String outputPath) throws IOException;

  public void load(String inputPath) throws IOException;

  public void toJSON(JSONStringer stringer) throws JSONException;

  public void fromJSON(JSONObject jsonObject) throws JSONException;
}
