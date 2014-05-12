package org.exoplatform.search.cmis;

import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetcher;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.client.runtime.util.EmptyItemIterable;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.easymock.EasyMock;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class CmisSearchConnectorTest {

  private SearchContext searchContext;
  private InitParams initParams;

  public CmisSearchConnectorTest() {
  }

  @Before
  public void setup() {
    Router router = null;
    try {
      router = new Router(new ControllerDescriptor());
    } catch (RouterConfigException e) {
      Assert.fail(e.getMessage());
    }
    searchContext = new SearchContext(router, "intranet");

    initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    propertiesParam.setProperty("searchType", "test");
    propertiesParam.setProperty("displayName", "Test");
    initParams.addParameter(propertiesParam);
  }

  @Test
  public void testSearchWithNoResults() {
    Session session = EasyMock.createNiceMock(Session.class);
    EasyMock.expect(session.query(EasyMock.isA(String.class), EasyMock.anyBoolean())).andReturn(new EmptyItemIterable<QueryResult>()).anyTimes();
    EasyMock.replay(session);

    CmisSearchConnector cmisSearchConnector = new CmisSearchConnector(initParams);
    cmisSearchConnector.setSession(session);

    String query = "";
    Collection<String> sites = new ArrayList<String>();
    sites.add("intranet");
    int offset = 0;
    int limit = 10;
    String sort = "title";
    String order = "ASC";

    Collection<SearchResult> searchResults = cmisSearchConnector.search(searchContext, query, sites, offset, limit, sort, order);

    Assert.assertNotNull(searchResults);
    Assert.assertTrue(searchResults.size() == 0);
  }

  @Test
  public void testSearchWithResults() {
    Session session = EasyMock.createNiceMock(Session.class);
    CollectionIterable<QueryResult> queryResults = new CollectionIterable<QueryResult>(new AbstractPageFetcher<QueryResult>(10) {

      @Override
      protected AbstractPageFetcher.Page<QueryResult> fetchPage(long skipCount) {

        List<QueryResult> page = new ArrayList<QueryResult>();
        QueryResult queryResult = new QueryResult() {
          List<PropertyData<?>> propertyDatas = Arrays.asList(new PropertyData<?>[] {
                  new PropertyStringImpl("cmis:name", "myName"),
                  new PropertyStringImpl("cmis:description", "My Description"),
                  new PropertyStringImpl("cmis:createdBy", "My Author"),
                  new PropertyStringImpl("cmis:versionLabel", "My Version Label"),
                  new PropertyDateTimeImpl("cmis:lastModificationDate", (GregorianCalendar) GregorianCalendar.getInstance()),
                  new PropertyStringImpl("cmis:objectId", "123")
          });

          @Override
          public List<PropertyData<?>> getProperties() {
            return propertyDatas;
          }

          @Override
          public <T> PropertyData<T> getPropertyById(String id) {
            for(PropertyData propertyData : propertyDatas) {
              if(propertyData.getId().equals(id)) {
                return propertyData;
              }
            }
            return null;
          }

          @Override
          public <T> PropertyData<T> getPropertyByQueryName(String queryName) {
            throw new RuntimeException("Not Yet Implemented");
          }

          @Override
          public <T> T getPropertyValueById(String id) {
            for(PropertyData propertyData : propertyDatas) {
              if(propertyData.getId().equals(id)) {
                return (T) propertyData.getFirstValue();
              }
            }
            return null;
          }

          @Override
          public <T> T getPropertyValueByQueryName(String queryName) {
            throw new RuntimeException("Not Yet Implemented");
          }

          @Override
          public <T> List<T> getPropertyMultivalueById(String id) {
            throw new RuntimeException("Not Yet Implemented");
          }

          @Override
          public <T> List<T> getPropertyMultivalueByQueryName(String queryName) {
            throw new RuntimeException("Not Yet Implemented");
          }

          @Override
          public AllowableActions getAllowableActions() {
            throw new RuntimeException("Not Yet Implemented");
          }

          @Override
          public List<Relationship> getRelationships() {
            throw new RuntimeException("Not Yet Implemented");
          }

          @Override
          public List<Rendition> getRenditions() {
            throw new RuntimeException("Not Yet Implemented");
          }
        };

        page.add(queryResult);

        return new AbstractPageFetcher.Page<QueryResult>(page, 1, false);
      }
    });

    EasyMock.expect(session.query(EasyMock.isA(String.class), EasyMock.anyBoolean())).andReturn(queryResults).anyTimes();
    EasyMock.replay(session);

    CmisSearchConnector cmisSearchConnector = new CmisSearchConnector(initParams);
    cmisSearchConnector.setSession(session);

    String query = "";
    Collection<String> sites = new ArrayList<String>();
    sites.add("intranet");
    int offset = 0;
    int limit = 10;
    String sort = "title";
    String order = "ASC";

    Collection<SearchResult> searchResults = cmisSearchConnector.search(searchContext, query, sites, offset, limit, sort, order);

    Assert.assertNotNull(searchResults);
    Assert.assertTrue(searchResults.size() == 1);
    SearchResult searchResult = searchResults.iterator().next();
    Assert.assertEquals(searchResult.getTitle(), "myName");
    Assert.assertEquals(searchResult.getExcerpt(), "My Description");
  }
}
