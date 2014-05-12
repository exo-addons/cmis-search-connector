CMIS Search Connector
=====================

This addon provides a eXo Search connector for CMIS repositories.

Getting Started
===============

Step 1 :  Build
----------------

Prerequisite : install [Maven 3](http://maven.apache.org/download.html)

Clone the project with

    git clone https://github.com/exo-addons/cmis-search-connector.git
    cd cmis-search-connector

Build it with

    mvn clean package

Step 2 : Deploy
---------------

Prerequisite : install [eXo Platform 4.0 Tomcat bundle](http://www.exoplatform.com/company/en/download-exo-platform) (EXO\_TOMCAT\_ROOT\_FOLDER will be used to designate the eXo Tomcat root folder).

Copy the extension binary :

    cp target/cmis-search-connector-*.jar EXO_TOMCAT_ROOT_FOLDER/lib

Step 3 : Configure
------------------

In your extension, declare the CMIS Search connector with the parameters of your CMIS repository. Examples are provided in src/main/resources/conf/portal/configuration.xml :

    <!-- Alfresco CMIS example -->
    <external-component-plugins>
      <target-component>org.exoplatform.commons.api.search.SearchService</target-component>
      <component-plugin>
        <name>CmisSearchConnector</name>
        <set-method>addConnector</set-method>
        <type>org.exoplatform.search.cmis.CmisSearchConnector</type>
        <description>CMIS Search Connector</description>
        <init-params>
          <properties-param>
            <name>constructor.params</name>
            <property name="searchType" value="alfresco"/>
            <property name="displayName" value="Alfresco"/>
            <property name="cmisProviderHost" value="localhost"/>
            <property name="cmisProviderPort" value="8080"/>
            <property name="cmisProviderAtomUrl" value="/alfresco/cmisatom"/>
            <property name="cmisProviderUser" value="admin"/>
            <property name="cmisProviderPassword" value="alfresco"/>
            <property name="cmisProviderThumbnailUrl" value="https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcScSv7LiwhO23tccPs2v4anv0KSNDT2DeR6vOy-qoHLgQJ0JvDh"/>
          </properties-param>
        </init-params>
      </component-plugin>
    </external-component-plugins>

Step 4 : Run
------------

    cd EXO_TOMCAT_ROOT_FOLDER
    ./start_eXo.sh