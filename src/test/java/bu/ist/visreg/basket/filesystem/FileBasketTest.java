package bu.ist.visreg.basket.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import bu.ist.visreg.BackstopSplitterMocker;
import bu.ist.visreg.backstop.BackstopJson;
import bu.ist.visreg.backstop.Scenario;
import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;
import bu.ist.visreg.util.FileUtils;
import bu.ist.visreg.util.TestUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FileBasketTest {

	@Mock private File rootDirectory;
	@Mock private FileUtils basketFolder;
	@Mock private FileBasketSystem fileBasketSystem;
	@Mock private File backstopJson1;
	@Mock private FileUtils basktopJson1Utils;
	
	private FileBasket fb;
	
	@Before
	public void setUp() throws Exception {
		when(basketFolder.isDirectory()).thenReturn(true);		
		fb = new FileBasket(Basket.BasketEnum.INBOX, fileBasketSystem);
		fb.setFolder(basketFolder);
	}

	
	@Test
	public void testCreateIfNotExists() {		
		try {
			fb.createIfNotExists();
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			fail("Not expecting exception");
		}
	}
	
	
	@Test
	public void testLoad() {
		when(backstopJson1.isFile()).thenReturn(true);
		when(backstopJson1.getAbsolutePath()).thenReturn("/JobDefinitionThreeScenarios.json");
		when(basketFolder.listFiles()).thenReturn(
			new File[] { backstopJson1 }
		);
		String backstopJsonString = TestUtils.getClassPathResourceContent("job-definitions/JobDefinitionThreeScenarios.json");
		when(basktopJson1Utils.readFile()).thenReturn(backstopJsonString);
		when(basketFolder.newInstance(same(backstopJson1))).thenReturn(basktopJson1Utils);
		
		try {
			fb.load(BackstopSplitterMocker.getInstance());			
			List<BasketItem> items = fb.getBasketItems();
			assertEquals(3, items.size());
			
			List<BackstopJson> backstops = new ArrayList<BackstopJson>(items.size());
			for(int i=0; i<items.size(); i++) {
				String content = items.get(i).getContent();
				assertNotNull(content);
				backstops.add(i, BackstopJson.getInstance(content));				
			}
			
			// Order the backstopJson collection by the labels of the Scenario in their single member scenarios collection.
			backstops.sort(new Comparator<BackstopJson>() {
				@Override public int compare(BackstopJson bs1, BackstopJson bs2) {
					return bs1.getScenarios().get(0).getLabel().compareTo(bs2.getScenarios().get(0).getLabel());
				}				
			});
			
			int counter = 1;
			for(BackstopJson bj : backstops) {
				assertEquals(bj.getId(), "MyJob");
				assertNotNull(bj.getScenarios());
				assertEquals(1, bj.getScenarios().size());
				Scenario scenario = bj.getScenarios().get(0);
				assertEquals("scenario.label"+String.valueOf(counter), scenario.getLabel());
				assertEquals("backstop_data/engine_scripts/cookies.json", scenario.getCookiePath());
				switch(counter) {
				case 1:
					assertEquals("https://ref-domain.bu.edu/main-test.htm", scenario.getUrl());
					assertEquals("https://ref-domain.bu.edu/main.htm", scenario.getReferenceUrl());
					assertEquals("https://ref-domain.bu.edu/login.htm", scenario.getLoginUrl());
					break;
				case 2:
					assertEquals("https://ref-domain.bu.edu/some/other/page-test.htm", scenario.getUrl());
					assertEquals("https://ref-domain.bu.edu/some/other/page.htm", scenario.getReferenceUrl());
					assertEquals("https://login/url/inherited/from/JobDefinition/login.htm", scenario.getLoginUrl());
					break;
				case 3:
					assertEquals("https://ref-domain.bu.edu/another/web/page-test.htm", scenario.getUrl());
					assertEquals("https://ref-domain.bu.edu/another/web/page.htm", scenario.getReferenceUrl());
					assertEquals("https://login/url/inherited/from/JobDefinition/login.htm", scenario.getLoginUrl());
					break;
				}
				
				counter++;
			}
			
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail("Not expecting exception");
		}
	}

}
