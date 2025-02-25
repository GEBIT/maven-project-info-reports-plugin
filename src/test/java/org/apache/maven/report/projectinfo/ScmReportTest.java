package org.apache.maven.report.projectinfo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.Mojo;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.TextBlock;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Edwin Punzalan
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class ScmReportTest
    extends AbstractProjectInfoTestCase
{
    /**
     * WebConversation object
     */
    private static final WebConversation WEB_CONVERSATION = new WebConversation();

    /**
     * Test report
     *
     * @throws Exception if any
     */
    public void testReport()
        throws Exception
    {
        generateReport( "scm", "scm-plugin-config.xml" );
        assertTrue( "Test html generated", getGeneratedReport( "scm.html" ).exists() );

        URL reportURL = getGeneratedReport( "scm.html" ).toURI().toURL();
        assertNotNull( reportURL );

        // HTTPUnit
        WebRequest request = new GetMethodWebRequest( reportURL.toString() );
        WebResponse response = WEB_CONVERSATION.getResponse( request );

        // Basic HTML tests
        assertTrue( response.isHTML() );
        assertTrue( response.getContentLength() > 0 );

        // Test the Page title
        String expectedTitle = prepareTitle( "scm project info",
            getString( "report.scm.title" ) );
        assertEquals( expectedTitle, response.getTitle() );

        // Test the texts
        TextBlock[] textBlocks = response.getTextBlocks();
        assertEquals( 6, textBlocks.length );
        assertEquals( getString( "report.scm.overview.title" ), textBlocks[0].getText() );
        assertEquals( getString( "report.scm.general.intro" ), textBlocks[1].getText() );
        assertEquals( getString( "report.scm.webaccess.title" ), textBlocks[2].getText() );
        assertEquals( getString( "report.scm.webaccess.nourl" ), textBlocks[3].getText() );
        assertEquals( getString( "report.scm.accessbehindfirewall.title" ), textBlocks[4].getText() );
        assertEquals( getString( "report.scm.accessbehindfirewall.general.intro" ), textBlocks[5].getText() );
    }

    /**
     * Test report with wrong URL
     *
     * @throws Exception if any
     */
    public void testReportWithWrongUrl()
        throws Exception
    {
        File pluginXmlFile = new File( getBasedir(), "src/test/resources/plugin-configs/"
                + "scm-wrong-url-plugin-config.xml" );
        Mojo mojo = createReportMojo( "scm", pluginXmlFile );

        setVariableValueToObject( mojo, "anonymousConnection", "scm:svn" );
        try
        {
            mojo.execute();
            fail( "IllegalArgumentException NOT catched" );
        }
        catch ( IllegalArgumentException e )
        {
            assertTrue( "IllegalArgumentException catched", true );
        }

        tearDown();
        setUp();

        mojo = lookupMojo( "scm", pluginXmlFile );
        assertNotNull( "Mojo found.", mojo );
        setVariableValueToObject( mojo, "anonymousConnection", "scm:svn:http" );
        try
        {
            mojo.execute();
            fail( "IllegalArgumentException NOT catched" );
        }
        catch ( Exception e )
        {
            assertTrue( "IllegalArgumentException catched", true );
        }

        tearDown();
        setUp();

        mojo = lookupMojo( "scm", pluginXmlFile );
        assertNotNull( "Mojo found.", mojo );
        setVariableValueToObject( mojo, "anonymousConnection", "scm" );
        try
        {
            mojo.execute();
            fail( "IllegalArgumentException NOT catched" );
        }
        catch ( Exception e )
        {
            assertTrue( "IllegalArgumentException catched", true );
        }
    }
}
