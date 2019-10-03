package com.maciejkozuch.dna;
import com.maciejkozuch.dna.c4.AppPropersties;
import com.structurizr.api.StructurizrClient;

/**
 * Structurizr API Client builder for a DNA projects / tasks.
 */
public class StructurizrClientBuilder {

  
  private AppPropersties appPropersties;

  public StructurizrClientBuilder() {
    appPropersties = AppPropersties.get();
  }

  public StructurizrClient build() {
    return new StructurizrClient(appPropersties.getApiKey(), appPropersties.getApiSecret());
  }
}