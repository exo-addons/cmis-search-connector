package org.exoplatform.search.cmis;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.easymock.EasyMock;
import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.LinkedList;

public class CmisDownloadRestServiceTest {

  private SearchService searchService;
  private InitParams initParams;
  private Session session;

  public CmisDownloadRestServiceTest() {
  }

  @Before
  public void setup() throws NoSuchMethodException {
    // Set eXo implementation for JAX-RS
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());

    initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    propertiesParam.setProperty("searchType", "test");
    propertiesParam.setProperty("displayName", "Test");
    initParams.addParameter(propertiesParam);

    Document document = EasyMock.createNiceMock(Document.class);
    EasyMock.expect(document.getContentStream()).andReturn(new ContentStreamImpl()).anyTimes();
    EasyMock.replay(document);

    session = EasyMock.createNiceMock(Session.class);
    EasyMock.expect(session.getObject(EasyMock.isA(ObjectIdImpl.class))).andReturn(document).anyTimes();
    EasyMock.replay(session);
  }

  @Test
  public void testDownload() {
    // Mock SearchService with a CmisSearchConnector
    searchService = EasyMock.createNiceMock(SearchService.class);
    LinkedList<SearchServiceConnector> searchServiceConnectors = new LinkedList<SearchServiceConnector>();
    CmisSearchConnector cmisSearchConnector = new CmisSearchConnector(initParams);
    searchServiceConnectors.add(cmisSearchConnector);
    EasyMock.expect(searchService.getConnectors()).andReturn(searchServiceConnectors);
    EasyMock.replay(searchService);

    cmisSearchConnector.setSession(session);

    CmisDownloadRestService cmisDownloadRestService = new CmisDownloadRestService(searchService);
    Response response = cmisDownloadRestService.download("test", "123", "name");

    Assert.assertNotNull(response);
    Assert.assertEquals(response.getStatus(), 200);
  }

  @Test
  public void testDownloadNoConnector() {
    // Mock SearchService with no connector
    searchService = EasyMock.createNiceMock(SearchService.class);
    LinkedList<SearchServiceConnector> searchServiceConnectors = new LinkedList<SearchServiceConnector>();
    EasyMock.expect(searchService.getConnectors()).andReturn(searchServiceConnectors);
    EasyMock.replay(searchService);

    CmisDownloadRestService cmisDownloadRestService = new CmisDownloadRestService(searchService);
    Response response = cmisDownloadRestService.download("test", "123", "name");

    Assert.assertNotNull(response);
    Assert.assertEquals(response.getStatus(), 500);
  }
}
