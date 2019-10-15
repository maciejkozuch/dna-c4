package com.maciejkozuch.dna.c4;

import com.maciejkozuch.dna.StructurizrClientBuilder;
import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Enterprise;
import com.structurizr.model.Location;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ContainerView;
import com.structurizr.view.PaperSize;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;

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
    ViewSet views = workspace.getViews();

    // System context (C1)
    // =======================
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
    
    // Relations (C1)
    user.uses(pb, "Używa", "mobile/html/https");
    companyAdmin.uses(pb, "Zarządza kontem", "html/https");
    administator.uses(pb, "Administruje", "html/https");
    administator.uses(cognito, "Uwierzytelnia się w", "html/https");
    pb.uses(cognito, "Uwierzytelnienie i autoryzacja, dane użytkowników", "api/json/https");
    pb.uses(dynamoDB, "Zapisuje i odczytuje dane w/z", "api/json/https");

    // Container context (C2)
    // =======================
    Container mobile = pb.addContainer("PB Mobile", "Aplikacja mobilna dla użytkowników systemu na Androida i iOS.",
        "Flutter/Dart");
    Container webapp = pb.addContainer("PB WebApp", "Aplikacja WWW dla użytkowników systemu i administratorów firmy.",
        "Angular/TypeScript");
    Container api = pb.addContainer("PB API", "Zapewnia usługu API dla aplikacji użytkowników.", "Spring MVC/Java");
    Container admin = pb.addContainer("PB Admin", "Aplikacja WWW do zarządzania systemem.", "Spring Boot/Java");
    Container core = pb.addContainer("PB Core",
        "Podstawowa biblioteka Java wykorzystywana przez aplikacje napisane w Java. "
            + "Implementuje usługi dotyczące uwierzytelnienia oraz dostępu do danych.",
        "Spring/Java");
    
    // Relations (C2)
    user.uses(webapp, "Używa", "html/https");
    user.uses(mobile, "Używa", "mobile");
    companyAdmin.uses(webapp, "Używa", "html/https");
    administator.uses(admin, "Używa", "html/https");

    webapp.uses(api, "Używa", "json/https");
    webapp.uses(cognito, "Uwierzytelnienie", "amazon sdk/json/https");

    mobile.uses(api, "Używa", "json/https");
    mobile.uses(cognito, "Uwierzytelnienie", "amazon sdk/json/https");

    api.uses(core, "Używa", "Java API");

    admin.uses(core, "Używa", "Java API");
    admin.uses(cognito, "Uwierzytelnienie tokena", "aws sdk/json/https");
    
    core.uses(cognito, "Dane użytkowników", "aws sdk/json/https");
    core.uses(dynamoDB, "Odczyt/zapis danych", "aws sdk/json/https");

    // Component context (C3)
    // =======================
    Component webappCore = webapp.addComponent("WebApp Core", "Moduł zapewniający usługi dla pozostałych modułów kontenera.", "Angular Module, TypeScript");
    Component webappDictionary = webapp.addComponent("WebApp Dictionary", "Moduł zapewniający usługi do prezentacji danych ze słowników.", "Angular Module, TypeScript");
    Component webappDocuments = webapp.addComponent("WebApp Documents", "Moduł zapewniający obsługę zarządzania dokumentami.", "Angular Module, TypeScript");
    Component webappTemplates = webapp.addComponent("WebApp Templates", "Moduł zapewniający obsługę szablonów dokumentów.", "Angular Module, TypeScript");
    Component webappCompany = webapp.addComponent("WebApp Company", "Moduł do zarządzania kontem firmy. W ramach modułu administrator firmy może m.in. zarządzać użytkownikami i licencjami.", "Angular Module, TypeScript");

    user.uses(webappDictionary, "Używa", "html/https");
    user.uses(webappDocuments, "Używa", "html/https");
    user.uses(webappTemplates, "Używa", "html/https");
    companyAdmin.uses(webappCompany, "Używa", "html/https");

    webappCore.uses(cognito, "Uwierzytelnienie", "jwt/json/https");
    webappDictionary.uses(core, "Używa", "angluar/typescript api");
    webappDocuments.uses(core, "Używa", "angluar/typescript api");
    webappTemplates.uses(core, "Używa", "angluar/typescript api");
    webappCompany.uses(core, "Używa", "angluar/typescript api");

    model.addImplicitRelationships();

    // View (C1)
    SystemContextView systemContextView = views.createSystemContextView(pb, "PBSystemContext", "Diagram kontekstowy systemu PB");
    systemContextView.addNearestNeighbours(pb);
    systemContextView.addAnimation(pb);
    systemContextView.addAnimation(user);
    systemContextView.addAnimation(companyAdmin);
    systemContextView.addAnimation(administator);
    systemContextView.addAnimation(cognito, dynamoDB);

    // View (C2)
    ContainerView pbContainersView = views.createContainerView(pb, "PBContainers", "Diagram kontenerów systemu PB");
    pbContainersView.add(user);
    pbContainersView.add(companyAdmin);
    pbContainersView.add(administator);
    
    pbContainersView.addAllContainers();
    pbContainersView.add(dynamoDB);
    pbContainersView.add(cognito);

    pbContainersView.addAnimation(user, companyAdmin, administator, dynamoDB, cognito);
    pbContainersView.addAnimation(webapp);
    pbContainersView.addAnimation(mobile);
    pbContainersView.addAnimation(api);
    pbContainersView.addAnimation(admin);
    pbContainersView.addAnimation(core);

    // View (C3)
    ComponentView webappComponentView = views.createComponentView(webapp, "WebAppComponents", "Diagram komponentów aplikacji WebApp");
    webappComponentView.add(user);
    webappComponentView.add(companyAdmin);
    webappComponentView.add(api);
    webappComponentView.add(cognito);
    webappComponentView.addAllComponents();
    webappComponentView.setPaperSize(PaperSize.A4_Landscape);

    webappComponentView.addAnimation(api);
    webappComponentView.addAnimation(webappCore, cognito);
    webappComponentView.addAnimation(webappDictionary, webappTemplates, webappDictionary, user);
    webappComponentView.addAnimation(webappCompany, companyAdmin);

    StructurizrClient structurizrClient = new StructurizrClientBuilder().build();
    try {
      structurizrClient.putWorkspace(appPropersties.getWorkspaceId(), workspace);
    } catch (StructurizrClientException e) {
      e.printStackTrace();
    }
  }
}