package com.maciejkozuch.dna.c4;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Resources;

public class AppPropersties extends Properties {
  
  private static final long serialVersionUID = 1L;
  private static final String propertiesFileName = "app.properties";
  private static AppPropersties properties = null;

  private AppPropersties() {
    super();
    URL propertiesFileUrl = Resources.getResource(propertiesFileName);
    try {
      load(new FileReader(new File(propertiesFileUrl.toURI())));
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public int getWorkspaceId() {
    return Integer.parseInt(properties.getProperty("structurizr.workspace.id"));
  }

  public String getApiKey() {
    return getProperty("structurizr.api.key");
  }

  public String getApiSecret() {
    return getProperty("structurizr.api.secret");
  }

  public static AppPropersties get() {
    if(properties == null) {
      properties = new AppPropersties();
    }
    return properties;
  }
  
}