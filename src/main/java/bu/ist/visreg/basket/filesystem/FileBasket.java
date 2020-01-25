package bu.ist.visreg.basket.filesystem;

import java.io.File;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;

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
	public void load() {
		for(File f : folder.listFiles()) {
			BasketItem bi = new FileBasketItem(this, f.getAbsolutePath(), getFileContent(f));
			addBasketItem(bi);
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
