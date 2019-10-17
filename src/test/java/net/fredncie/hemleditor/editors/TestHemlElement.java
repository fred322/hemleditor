/**
 * 
 */
package net.fredncie.hemleditor.editors;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 * @author Fred
 *
 */
public class TestHemlElement {

	/**
	 * Test the parsing of a heml document.
	 * @throws IOException 
	 */
	@Test
	public void testCreationHemlElement() throws IOException {
		String data = Files.readFile(new File("src/test/resources/test.heml"));
		Assert.assertNotNull(data);
		
		HemlElement document = HemlElement.create(data);
		Assert.assertNotNull(document);
		
		Assert.assertEquals(document.getQualifier(), "document");
		Assert.assertEquals(document.getChildren().length, 2);
		
		HemlElement toto1 = document.getChildren()[0];
		HemlElement section2 = document.getChildren()[1];
		Assert.assertEquals(toto1.getLabel(), "section (Toto1)");
        Assert.assertEquals(section2.getLabel(), "section (Section 2)");
        
        Assert.assertEquals(toto1.getChildren().length, 6);
        Assert.assertEquals(toto1.getChildren()[0].getLabel(), "!");
        Assert.assertEquals(toto1.getChildren()[1].getLabel(), "em (tptp)");
        Assert.assertEquals(toto1.getChildren()[2].getLabel(), "#");
        Assert.assertEquals(toto1.getChildren()[3].getLabel(), "section (too1)");
        HemlElement caoLeGrand = toto1.getChildren()[4];
        Assert.assertEquals(caoLeGrand.getLabel(), "section (cao le grand)");
        Assert.assertEquals(toto1.getChildren()[5].getLabel(), "section (Nouvelle section)");
        
        Assert.assertEquals(caoLeGrand.getChildren().length, 3);
        Assert.assertEquals(caoLeGrand.getChildren()[0].getLabel(), "section (Sub1)");
        Assert.assertEquals(caoLeGrand.getChildren()[1].getLabel(), "code");
        HemlElement code2 = caoLeGrand.getChildren()[2];
        Assert.assertEquals(code2.getLabel(), "code");
        Assert.assertEquals(code2.getChildren().length, 1);
        Assert.assertEquals(code2.getChildren()[0].getLabel(), "!");
        
        
        Assert.assertEquals(section2.getChildren().length, 4);
        Assert.assertEquals(section2.getChildren()[0].getLabel(), "section (SubSection number 1)");
        Assert.assertEquals(section2.getChildren()[1].getLabel(), "section (SubSection number 2)");
        Assert.assertEquals(section2.getChildren()[2].getLabel(), "section (SubSection number 3)");
        Assert.assertEquals(section2.getChildren()[3].getLabel(), "section (SubSection number 4)");
	}
	
}
