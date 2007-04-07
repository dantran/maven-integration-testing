package org.apache.maven.integrationtests;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class MavenIT0114ExtensionThatProvidesResources
    extends AbstractMavenIntegrationTestCase
{
    public void testit0114()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( getClass(), "/it0114-extensionThatProvidesResources" );

        Verifier verifier;

        // Install the parent POM, extension and the plugin 
        verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.deleteArtifact( "org.apache.maven.its.it0114", "it0114-plugin-runner", "1.0", "pom" );                
        verifier.deleteArtifact( "org.apache.maven.its.it0114", "it0114-extension", "1.0", "jar" );
        verifier.deleteArtifact( "org.apache.maven.its.it0114", "it0114-plugin", "1.0", "jar" );
        verifier.deleteArtifact( "org.apache.maven.its.it0114", "it0114-parent", "1.0", "pom" );
        
        List cliOptions = new ArrayList();        
        verifier.executeGoal( "install" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
        
        //now run the test
        testDir =
            ResourceExtractor.simpleExtractResources( getClass(), "/it0114-extensionThatProvidesResources/test-project" );
        verifier = new Verifier( testDir.getAbsolutePath() );
        cliOptions = new ArrayList();
        verifier.executeGoal( "verify" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
        
    }
}