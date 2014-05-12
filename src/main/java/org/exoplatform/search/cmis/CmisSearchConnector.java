package org.exoplatform.search.cmis;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

import java.text.SimpleDateFormat;
import java.util.*;

public class CmisSearchConnector extends SearchServiceConnector {

  private String host;
  private String port;
  private String atomUrl;
  private String user;
  private String password;
  private String thumbnailUrl;

  private Session session;

  public CmisSearchConnector(InitParams initParams) {
    super(initParams);

    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.host = param.getProperty("cmisProviderHost");
    this.port = param.getProperty("cmisProviderPort");
    this.atomUrl = param.getProperty("cmisProviderAtomUrl");
    this.user = param.getProperty("cmisProviderUser");
    this.password = param.getProperty("cmisProviderPassword");
    this.thumbnailUrl = param.getProperty("cmisProviderThumbnailUrl");
  }

  public Session getSession() {
    if(session == null) {
      session = openSession();
    }
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  @Override
  public Collection<SearchResult> search(SearchContext context, String query, Collection<String> sites, int offset, int limit, String sort, String order) {
    Collection<SearchResult> results = new ArrayList<SearchResult>();

    if(query != null) {
      int fuzzyIndex = query.indexOf("~");
      if(fuzzyIndex >= 0) {
        query = query.substring(0, fuzzyIndex).trim();
      }
    } else {
      query = "";
    }

    ItemIterable<QueryResult> cmisResults = getSession().query("SELECT * FROM cmis:document WHERE cmis:name like '%" + query + "%'", false);

    for (QueryResult cmisResult : cmisResults) {
      results.add(convertToSearchResult(cmisResult));
    }

    return results;
  }

  public Session openSession() {
    // Create a SessionFactory and set up the SessionParameter map
    SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
    Map<String, String> parameters = new HashMap<String, String>();

    // connection settings - we are connecting to the cmis repo, using the AtomPUB binding
    parameters.put(SessionParameter.ATOMPUB_URL, "http://" + this.host + ":" + this.port + this.atomUrl);
    parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

    parameters.put(SessionParameter.USER, this.user);
    parameters.put(SessionParameter.PASSWORD, this.password);

    // create session
    return sessionFactory.getRepositories(parameters).get(0).createSession();
  }

  private SearchResult convertToSearchResult(QueryResult cmisResult) {
    Object nameProperty = cmisResult.getPropertyById("cmis:name").getFirstValue();
    String name = nameProperty != null ? nameProperty.toString() : "";
    Object descriptionProperty = cmisResult.getPropertyById("cmis:description").getFirstValue();
    String description = descriptionProperty != null ? descriptionProperty.toString() : "";
    Object createdByProperty = cmisResult.getPropertyById("cmis:createdBy").getFirstValue();
    String createdBy = createdByProperty != null ? createdByProperty.toString() : "";
    Object versionLabelProperty = cmisResult.getPropertyById("cmis:versionLabel").getFirstValue();
    String versionLabel = versionLabelProperty != null ? versionLabelProperty.toString() : "";
    Object lastModificationDateProperty = cmisResult.getPropertyById("cmis:lastModificationDate").getFirstValue();
    Date lastModificationDate = lastModificationDateProperty != null ? ((Calendar)lastModificationDateProperty).getTime() : null;
    Object objectIdProperty = cmisResult.getPropertyById("cmis:objectId").getFirstValue();
    String objectId = objectIdProperty != null ? objectIdProperty.toString() : "";

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");

    return new SearchResult("/rest/private/cmissearch/download?searchType="  + this.getSearchType() + "&contentId=" + objectId + "&name=" + name,
            name,
            description,
            createdBy + " - " + versionLabel + " - " + dateFormatter.format(lastModificationDate),
            decodeThumbnailUrl(cmisResult),
            lastModificationDate != null ? lastModificationDate.getTime() : 0,
            1);
  }

  private String decodeThumbnailUrl(QueryResult cmisResult) {
    if(this.thumbnailUrl == null || this.thumbnailUrl.trim().isEmpty()) {
      return null;
    } else if(this.thumbnailUrl.startsWith("http://")
          || this.thumbnailUrl.startsWith("https://")) {
      return this.thumbnailUrl;
    } else {
      return "http://" + this.host + ":" + this.port + this.thumbnailUrl;
    }
  }
}
