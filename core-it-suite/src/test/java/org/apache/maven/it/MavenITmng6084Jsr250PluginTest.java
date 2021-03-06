package org.apache.maven.it;

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

import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;

/**
 * This is a test set for <a href="https://issues.apache.org/jira/browse/MNG-6084">MNG-6084</a>.
 *
 * @author Dan Tran
 */
public class MavenITmng6084Jsr250PluginTest
    extends AbstractMavenIntegrationTestCase
{

    private File testDir;

    public MavenITmng6084Jsr250PluginTest()
    {
        super( "[3.4.0-SNAPSHOT,)" );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-6084" );

    }

    protected void tearDown()
        throws Exception
    {

        super.tearDown();
    }

    public void testJsr330PluginExecution()
        throws Exception
    {
        //
        // Build a plugin that uses a JSR250 plugin
        //
        Verifier v0 = newVerifier( testDir.getAbsolutePath(), "remote" );
        v0.setAutoclean( false );
        v0.deleteDirectory( "target" );
        v0.deleteArtifacts( "org.apache.maven.its.mng6084" );
        v0.executeGoal( "install" );
        v0.verifyErrorFreeLog();
        v0.resetStreams();

        //
        // Execute the JSR250 plugin
        //
        Verifier v1 = newVerifier( testDir.getAbsolutePath(), "remote" );
        v1.setAutoclean( false );
        v1.executeGoal( "org.apache.maven.its.mng6084:jsr250-maven-plugin:0.0.1-SNAPSHOT:hello" );
        v1.verifyErrorFreeLog();
        v1.resetStreams();
        v1.verifyTextInLog( "Hello! I am a component using JSR250 with @PostConstruct support" );

    }

}
