/**
 * 
 */
package net.fredncie.hemleditor.editors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.Files;

/**
 * Test for the {@link HemlElement} class.
 */
public class TestHemlElement {

	/**
	 * Test the parsing of a heml document.
	 * @throws IOException 
	 */
	@Test
	public void testCreationHemlElement() throws IOException {		
		HemlElement document = HemlElement.create(new File("src/test/resources/test.heml"));
		Assert.assertNotNull(document);
		
		Assert.assertEquals(document.getQualifier(), "document");
		Assert.assertEquals(document.getChildren().length, 2);
		
		HemlElement toto1 = document.getChildren()[0];
		HemlElement section2 = document.getChildren()[1];
		Assert.assertEquals(toto1.getLabel(), "section (Toto1)");
        Assert.assertEquals(section2.getLabel(), "section (Section 2)");
        
        Assert.assertEquals(toto1.getChildren().length, 8);
        Assert.assertEquals(toto1.getChildren()[0].getLabel(), "code block");
        Assert.assertEquals(toto1.getChildren()[1].getLabel(), "definitions (Sigles)");
        Assert.assertEquals(toto1.getChildren()[2].getLabel(), "em (tptp)");
        Assert.assertEquals(toto1.getChildren()[3].getLabel(), "comment");
        Assert.assertEquals(toto1.getChildren()[4].getLabel(), "section (too1)");
        HemlElement caoLeGrand = toto1.getChildren()[5];
        Assert.assertEquals(caoLeGrand.getLabel(), "section (cao le grand)");
        Assert.assertEquals(toto1.getChildren()[6].getLabel(), "comment");
        HemlElement nouvellSect = toto1.getChildren()[7];
        Assert.assertEquals(nouvellSect.getLabel(), "section (Nouvelle section)");
        
        Assert.assertEquals(caoLeGrand.getChildren().length, 3);
        Assert.assertEquals(caoLeGrand.getChildren()[0].getLabel(), "section (Sub1)");
        Assert.assertEquals(caoLeGrand.getChildren()[1].getLabel(), "code (lua)");
        HemlElement code2 = caoLeGrand.getChildren()[2];
        Assert.assertEquals(code2.getLabel(), "code (bash)");
        Assert.assertEquals(code2.getChildren().length, 1);
        Assert.assertEquals(code2.getChildren()[0].getLabel(), "code block");
        
        Assert.assertEquals(nouvellSect.getChildren().length, 2);
        HemlElement subSection = nouvellSect.getChildren()[0];
        Assert.assertEquals(subSection.getLabel(), "section (Sub Section)");
        Assert.assertEquals(nouvellSect.getChildren()[1].getLabel(), "section (Response)");
                
        Assert.assertEquals(subSection.getChildren().length, 1);
        Assert.assertEquals(subSection.getChildren()[0].getLabel(), "table");
        Assert.assertEquals(subSection.getChildren()[0].getChildren()[1].getLabel(), "?table");
                
        Assert.assertEquals(section2.getChildren().length, 5);
        Assert.assertEquals(section2.getChildren()[0].getLabel(), "section (SubSection number 1)");
        Assert.assertEquals(section2.getChildren()[1].getLabel(), "section (SubSection number 2)");
        Assert.assertEquals(section2.getChildren()[2].getLabel(), "section (SubSection number 3)");
        Assert.assertEquals(section2.getChildren()[3].getLabel(), "section (SubSection number 4)");
        Assert.assertEquals(section2.getChildren()[4].getLabel(), "?include");

        List<Position> positions = new ArrayList<Position>();
        document.generatePosition(positions);
        Assert.assertEquals(positions.size(), 35);
	}
	
	@Test
	public void testHemlWithoutFirstLine() throws IOException {        
        HemlElement document = HemlElement.create(new File("src/test/resources/test2.heml"));
        Assert.assertNotNull(document);
        
        Assert.assertEquals(document.getQualifier(), "document");
        Assert.assertEquals(document.getChildren().length, 2);
        
        HemlElement toto1 = document.getChildren()[0];
        HemlElement section2 = document.getChildren()[1];
        Assert.assertEquals(toto1.getLabel(), "section (Toto1)");
        Assert.assertEquals(section2.getLabel(), "section (Section 2)");	   

        List<Position> positions = new ArrayList<Position>();
        document.generatePosition(positions);
        Assert.assertEquals(positions.size(), 9);
	}
	
	@Test
	public void testHemlIncomplet() throws IOException {
        HemlElement document = HemlElement.create(new File("src/test/resources/testIncomplet.heml"));
        Assert.assertNotNull(document);

        Assert.assertEquals(document.getChildren().length, 1);
        
        HemlElement toto1 = document.getChildren()[0];
        Assert.assertEquals(toto1.getLabel(), "section (Toto1)");
        
        List<Position> positions = new ArrayList<Position>();
        document.generatePosition(positions);
        Assert.assertEquals(positions.size(), 8);
	}

    @Test
    public void testEmptyHeml() throws IOException {
        HemlElement document = HemlElement.create(new File("src/test/resources/emptyFile.heml"));
        Assert.assertNotNull(document);

        Assert.assertEquals(document.getChildren().length, 0);
    }
    
    @Test
    public void testWriteHeml() throws IOException {
        HemlElement document = HemlElement.create(new File("src/test/resources/test.heml"));
        Assert.assertNotNull(document);

        File outputFile = new File("output.heml");
        StringBuilder output = new StringBuilder();
        document.write(output, new HemlIndenter());
        Files.write(output.toString(), outputFile, StandardCharsets.UTF_8);
        
        Assert.assertEquals(java.nio.file.Files.readAllBytes(outputFile.toPath()),
                java.nio.file.Files.readAllBytes(new File("src/test/resources/expectedFormatted.heml").toPath()));
    }
}
