package bu.ist.visreg.basket.filesystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import bu.ist.visreg.backstop.BackstopSplitter;
import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;
import bu.ist.visreg.basket.BasketItemSplitter;
import bu.ist.visreg.basket.BasketSystem;
import bu.ist.visreg.basket.BasketSystem.BasketType;
import bu.ist.visreg.job.VisRegJob;
import bu.ist.visreg.util.FileUtils;

public class FileBasket extends Basket {
	
	private FileUtils folder;
	
	public FileBasket(BasketEnum basketEnum, FileBasketSystem parent) {
		super.basketEnum = basketEnum;
		super.parent = parent;
	}
	
	@Override
	public void createIfNotExists() throws Exception {
		
		if(folder == null || !folder.isDirectory()) {
			System.out.println("Creating folder: " + folder.getAbsolutePath());
			
			if(folder == null) {
				folder = new FileUtils();
			}
			
			folder.setFile(new File(
					((FileBasketSystem) parent).getRootDirectory(), 
					basketEnum.getBasketRelativeLocation()));
			
			if(!folder.mkdirs()) {
				throw new RuntimeException(String.format(
						"ERROR! Cannot create: %s", 
						folder.getAbsolutePath()));
			}
		}
		else {
			System.out.println("Folder already exists: " + folder.getAbsolutePath());
		}
	}
	
	public File getFolder() {
		return folder.getFile();
	}
	public void setFolder(FileUtils folder) {
		this.folder = folder;
	}

	@Override
	public void load(BasketItemSplitter splitter) throws Exception {
		for(File f : folder.listFiles()) {
			BasketItem bi = new FileBasketItem(this, f.getAbsolutePath(), getFileContent(f));
			
			List<BasketItem> subitems = splitter.splitIntoPieces(bi);

			if(subitems.isEmpty()) {
				addBasketItem(bi);
			}
			else {
				for(BasketItem subitem : subitems) {
					subitem.persist();
					addBasketItem(subitem);
				}
				if(subitems.size() > 1) {
					System.out.println("Deleting: " + bi.getPathname());
					bi.delete();
				}
				break;
			}
		}
	}

	@Override
	public String getIdentifier() {
		return folder.getAbsolutePath();
	}
	
	private String getFileContent(File f) {
		return folder.newInstance(f).readFile();
	}

	
	
	
	public static void main(String[] args) throws Exception {
		
		FileBasket inbox = TEST_LoadInbox();
		
//		FileBasket processing = TEST_GoToProcessing(inbox);
//		
//		FileBasket completed = TEST_ProcessPending(processing);
		
	}
	public static FileBasket TEST_LoadInbox() throws Exception {
		
		// 1) Instantiate a FileBasket. NOTE: Can omit the FileBasketSystem parent as long as you set the folder the basket is based on.
		FileBasket inboxBasket = new FileBasket(BasketEnum.INBOX, null);
		
		// 2) Define file parameters
		final String inbox = System.getProperty("java.io.tmpdir") + "/FileBaskets/" + BasketEnum.INBOX.getBasketName();
		final String jobdefName = "JobDefinitionThreeScenario.json";
		final String jobdefFile = inbox + "/" + jobdefName;
		inboxBasket.setFolder(new FileUtils(inbox));
		
		// 3) Create the folder backing the basket if it does not already exist.
		inboxBasket.createIfNotExists();
		
		// 4) Copy a sample JobDefinition json file to the basket
		Files.copy(
				Paths.get(FileUtils.getClassPathResource(jobdefName).getAbsolutePath()), 
				Paths.get(new File(jobdefFile).getAbsolutePath()), 
				java.nio.file.StandardCopyOption.REPLACE_EXISTING);

		// 5) Trigger the JobDefinition splitting logic by loading what is found in the basket.
		inboxBasket.load(new BackstopSplitter() {
			@Override public BasketItem pieceToBasketItem(BasketItem bi, String json, String pathname) {
				return bi.getSplitItem(json, pathname);
			}
		});
		
		return inboxBasket;
	}	
	public static FileBasket TEST_GoToProcessing(FileBasket inboxBasket) throws Exception {
		
		// 1) Instantiate the processing basket and create its backing folder.
		FileBasket processingBasket = new FileBasket(BasketEnum.IN_PROCESS, null);
		final String inprocess = System.getProperty("java.io.tmpdir") + "/FileBaskets/" + BasketEnum.IN_PROCESS.getBasketName();
		processingBasket.setFolder(new FileUtils(inprocess));
		processingBasket.createIfNotExists();
		
		// 2) Move each item in the inbox basket to the processing basket.
		for(BasketItem basketItem : inboxBasket.getBasketItems()) {
			basketItem.gotoNextBasket();
		}
		
		return processingBasket;
	}	
	public static FileBasket TEST_ProcessPending(FileBasket processingBasket) throws Exception {
		
		// 1) Instantiate the completed basket and create its backing folder.
		FileBasket completedBasket  = new FileBasket(BasketEnum.COMPLETED, null);
		final String completed = System.getProperty("java.io.tmpdir") + "/FileBaskets/" + BasketEnum.COMPLETED.getBasketName();
		completedBasket.setFolder(new FileUtils(completed));
		completedBasket.createIfNotExists();
		
		// 2) Process each item in the processing basket
		for(BasketItem basketItem : processingBasket.getBasketItems()) {
			new VisRegJob(basketItem).process();
		}

		return completedBasket;
	}	
}
