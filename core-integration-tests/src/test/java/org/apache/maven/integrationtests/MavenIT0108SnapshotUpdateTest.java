package org.apache.maven.integrationtests;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Downloads a snapshot dependency that was deployed with uniqueVersion = false, and checks it can be
 * updated. See MNG-1908.
 */
public class MavenIT0108SnapshotUpdateTest
    extends AbstractMavenIntegrationTestCase
{
    private Verifier verifier;

    private File artifact;

    private File repository;

    private File localRepoFile;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/it0108-snapshotUpdate" );
        verifier = new Verifier( testDir.getAbsolutePath() );
        localRepoFile = getLocalRepoFile( verifier );
        deleteLocalArtifact( verifier, localRepoFile );

        repository = new File( testDir, "repository" );
        recreateRemoteRepository( repository );

        // create artifact in repository (TODO: into verifier)
        artifact = new File( repository,
                             "org/apache/maven/maven-core-it-support/1.0-SNAPSHOT/maven-core-it-support-1.0-SNAPSHOT.jar" );
        artifact.getParentFile().mkdirs();
        FileUtils.fileWrite( artifact.getAbsolutePath(), "originalArtifact" );

        verifier.assertArtifactNotPresent( "org.apache.maven", "maven-core-it-support", "1.0-SNAPSHOT", "jar" );
    }

    public void testSnapshotUpdated()
        throws Exception
    {
        verifier.executeGoal( "package" );

        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        assertArtifactContents( "originalArtifact" );

        // set in the past to ensure it is downloaded
        localRepoFile.setLastModified( System.currentTimeMillis() - 5000 );

        FileUtils.fileWrite( artifact.getAbsolutePath(), "updatedArtifact" );

        verifier.executeGoal( "package" );

        assertArtifactContents( "updatedArtifact" );

        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
    }

    public void testSnapshotUpdatedWithMetadata()
        throws Exception
    {
        File metadata =
            new File( repository, "org/apache/maven/maven-core-it-support/1.0-SNAPSHOT/maven-metadata.xml" );
        FileUtils.fileWrite( metadata.getAbsolutePath(), constructMetadata( "1", System.currentTimeMillis() - 5000 ) );

        verifier.executeGoal( "package" );

        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        assertArtifactContents( "originalArtifact" );

        FileUtils.fileWrite( artifact.getAbsolutePath(), "updatedArtifact" );
        metadata = new File( repository, "org/apache/maven/maven-core-it-support/1.0-SNAPSHOT/maven-metadata.xml" );
        FileUtils.fileWrite( metadata.getAbsolutePath(), constructMetadata( "2", System.currentTimeMillis() ) );

        verifier.executeGoal( "package" );

        assertArtifactContents( "updatedArtifact" );

        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
    }

    private void assertArtifactContents( String s )
        throws IOException
    {
        verifier.assertArtifactPresent( "org.apache.maven", "maven-core-it-support", "1.0-SNAPSHOT", "jar" );
        verifier.assertArtifactContents( "org.apache.maven", "maven-core-it-support", "1.0-SNAPSHOT", "jar", s );
    }

    private static File deleteLocalArtifact( Verifier verifier, File localRepoFile )
        throws IOException
    {
        verifier.deleteArtifact( "org.apache.maven", "maven-core-it-support", "1.0-SNAPSHOT", "jar" );
        // this is to delete metadata - TODO: incorporate into deleteArtifact in verifier
        FileUtils.deleteDirectory( localRepoFile.getParentFile() );
        return localRepoFile;
    }

    private static File getLocalRepoFile( Verifier verifier )
    {
        return new File(
            verifier.getArtifactPath( "org.apache.maven", "maven-core-it-support", "1.0-SNAPSHOT", "jar" ) );
    }

    private static void recreateRemoteRepository( File repository )
        throws IOException
    {
        // create a repository (TODO: into verifier)
        FileUtils.deleteDirectory( repository );
        assertFalse( repository.exists() );
        repository.mkdirs();
    }

    private String constructMetadata( String buildNumber, long timestamp )
    {
        String ts = new SimpleDateFormat( "yyyyMMddHHmmss", Locale.US ).format( new Date( timestamp ) );

        return "<?xml version='1.0' encoding='UTF-8'?><metadata>\n" + "<groupId>org.apache.maven</groupId>\n" +
            "<artifactId>maven-core-it-support</artifactId>\n" + "<version>1.0-SNAPSHOT</version>\n" +
            "<versioning>\n" + "<snapshot>\n" + "<buildNumber>" + buildNumber + "</buildNumber>\n" + "</snapshot>\n" +
            "<lastUpdated>" + ts + "</lastUpdated>\n" + "</versioning>\n" + "</metadata>";
    }
}