package com.cloudera.hadoop.cloud.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

public class IniUtil {

  private IniUtil() {
  }

  public static Map<String, Properties> parseIni(Reader reader, Map<String, Properties> configs) throws IOException {
    new Properties() {
      private static final long serialVersionUID = 1L;
      
      private Properties section;

      @Override
      public Object put(Object key, Object value) {
        String header = (key + " " + value).trim();
        if (header.startsWith("[") && header.endsWith("]"))
          return configs.put(header.substring(1, header.length() - 1),
            section = new Properties());
        else
          return section.put(key, value);
      }
    }.load(reader);
    return configs;
  }

}
