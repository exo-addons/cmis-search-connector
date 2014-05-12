package org.exoplatform.search.cmis;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.services.rest.resource.ResourceContainer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/cmissearch")
public class CmisDownloadRestService implements ResourceContainer {

  private SearchService searchService;

  public CmisDownloadRestService(SearchService searchService) {
    this.searchService = searchService;
  }

  @GET
  @Path("/download")
  public Response download(@QueryParam("searchType") String searchType, @QueryParam("contentId") String contentId, @QueryParam("name") String name) {
    SearchServiceConnector usedConnector = null;

    // search for the connector used
    List<SearchServiceConnector> connectors = this.searchService.getConnectors();
    for(SearchServiceConnector connector : connectors) {
      if(connector.getSearchType().equals(searchType)) {
        usedConnector = connector;
        break;
      }
    }

    // the connector must be a CMIS search connector
    if(!(usedConnector instanceof CmisSearchConnector)) {
      //
      return Response.serverError().build();
    }

    // use CMIS to get the content
    Session session = ((CmisSearchConnector) usedConnector).getSession();
    ContentStream contentStream = ((Document) session.getObject(new ObjectIdImpl(contentId))).getContentStream();

    return Response.ok(contentStream.getStream()).header("Content-Disposition", "attachment; filename=" + name).build();
  }

}
