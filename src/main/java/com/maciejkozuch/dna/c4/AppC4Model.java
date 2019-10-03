package com.maciejkozuch.dna.c4;

import com.maciejkozuch.dna.StructurizrClientBuilder;
import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.model.Container;
import com.structurizr.model.Enterprise;
import com.structurizr.model.Location;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;

/**
 * A tool to create App C4 model.
 */
public class AppC4Model {

  private AppPropersties appPropersties;

  public AppC4Model() {
    appPropersties = AppPropersties.get();
  }

  public void create() {

    Workspace workspace = new Workspace("DNA", "A model for the DNA course.");
    Model model = workspace.getModel();
    model.setEnterprise(new Enterprise("PB"));

    // System context (C1)
    Person user = model.addPerson(Location.External, "Użytkownik", "Osoba posiadająca dostęp do aplikacji.");
    Person companyAdmin = model.addPerson(Location.External, "Administrator firmy",
        "Użytkownik aplikacji posiadający uprawnienia do zarządzania kontem firmy.");
    Person administator = model.addPerson(Location.Internal, "Administrator", "Administrator aplikacji.");

    SoftwareSystem cognito = model.addSoftwareSystem(Location.External, "AWS Cognito",
        "Usługa AWS do zarządzania użytkownikami i ich uwierzytelnienia.");
    administator.uses(cognito, "Uwierzytelnia się w", "html/https");
    SoftwareSystem dynamoDB = model.addSoftwareSystem(Location.External, "AWS DynamoDB",
        "Usługa AWS do zarządzania obiektową bazą danych.");

    SoftwareSystem pb = model.addSoftwareSystem(Location.Internal, "System PB", "System główny");
    user.uses(pb, "Używa", "mobile/html/https");
    companyAdmin.uses(pb, "Zarządza kontem", "html/https");
    administator.uses(pb, "Administruje", "html/https");
    pb.uses(cognito, "Uwierzytelnia użytkowników w", "api/json/https");
    pb.uses(dynamoDB, "Zapisuje i odczytuje dane w/z", "api/json/https");

    // Container context (C2)
    Container mobile = pb.addContainer("PB Mobile", "Aplikacja mobilna dla użytkowników systemu na Androida i iOS.",
        "Flutter/Dart");
    Container webapp = pb.addContainer("PB WebApp", "Aplikacja WWW dla użytkowników systemu i administratorów firmy.",
        "Angular/TypeScript");
    Container api = pb.addContainer("PB API", "Zapewnia usługu API dla aplikacji użytkowników.", "Spring MVC/Java");
    mobile.uses(api, "Używa", "json/https");
    webapp.uses(api, "Używa", "json/https");
    Container admin = pb.addContainer("PB Admin", "Aplikacja WWW do zarządzania systemem.", "Spring Boot/Java");
    admin.uses(cognito, "Uwierzytelnienie tokena", "jwt/json/https");
    Container core = pb.addContainer("PB Core",
        "Podstawowa biblioteka Java wykorzystywana przez aplikacje napisane w Java. "
            + "Implementuje usługi dotyczące uwierzytelnienia oraz dostępu do danych.",
        "Spring/Java");
    admin.uses(core, "Używa", "Java API");
    api.uses(core, "Używa", "Java API");
    core.uses(cognito, "Dane użytkowników", "json/https");
    core.uses(dynamoDB, "Odczyt/zapis danych", "json/https");

    user.uses(mobile, "Używa", "mobile");
    user.uses(webapp, "Używa", "html/https");
    companyAdmin.uses(webapp, "Używa", "html/https");
    administator.uses(admin, "Używa", "html/https");

    // Component context

    StructurizrClient structurizrClient = new StructurizrClientBuilder().build();
    try {
      structurizrClient.putWorkspace(appPropersties.getWorkspaceId(), workspace);
    } catch (StructurizrClientException e) {
      e.printStackTrace();
    }
  }
}