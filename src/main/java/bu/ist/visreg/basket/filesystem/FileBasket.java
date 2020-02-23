package bu.ist.visreg.basket.filesystem;

import java.io.File;
import java.util.List;

import bu.ist.visreg.backstop.BackstopSplitter;
import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;
import bu.ist.visreg.basket.BasketItemSplitter;

public class FileBasket extends Basket {
	
	private File folder;
	
	public FileBasket(BasketEnum basketEnum, FileBasketSystem parent) {
		super.basketEnum = basketEnum;
		super.parent = parent;
	}
	
	@Override
	public void createIfNotExists() throws Exception {
		
		this.folder = new File(
				((FileBasketSystem) parent).getRootDirectory(), 
				basketEnum.getBasketRelativeLocation());
		
		if(!folder.isDirectory()) {
			System.out.println("Creating folder: " + folder.getAbsolutePath());
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
		return folder;
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
				}
				bi.delete();
				load(splitter);
				break;
			}
		}
	}

	@Override
	public String getIdentifier() {
		return folder.getAbsolutePath();
	}
	
	private String getFileContent(File f) {
		return null;
	}

}
