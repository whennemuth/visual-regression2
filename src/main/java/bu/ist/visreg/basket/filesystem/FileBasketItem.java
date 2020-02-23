package bu.ist.visreg.basket.filesystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;

public class FileBasketItem extends BasketItem {
	
	public FileBasketItem(Basket basket, String pathname, String content) {
		super(basket, pathname, content);
	}

	@Override
	public void commitBasketMove(Basket nextBasket) throws Exception {
		
		File f = new File(pathname);
		if(f.isFile()) {
			File targetFolder = ((FileBasket) nextBasket).getFolder();
			File targetFile = new File(targetFolder.getAbsolutePath() + "\\" + f.getName());
			boolean commited = f.renameTo(targetFile);
			if(!commited) {
				throw new RuntimeException(String.format(
						"ERROR! Failed to move %s to %s",
						f.getAbsolutePath(),
						targetFolder));
			}
			else {
				this.pathname = targetFile.getAbsolutePath();
				System.out.println(String.format(
						"Moved %s to %s",
						f.getAbsolutePath(),
						targetFolder));
			}
		}
	}

	@Override
	public BasketItem getSplitItem(String json, String id) {
		if(id.equals(pathname)) {
			// The id IS the existing pathname. So, just change the content of this basket item to json and return it.
			return new FileBasketItem(basket, pathname, json);
		}
		else {
			// The id designates a portion of a new pathname to be based on the existing one.
			// Build the new pathname and return a corresponding basket item with json as the content.
			File directory = new File(pathname).getParentFile();
			String basename = new File(pathname).getName();			
			String filename = basename + "_" + id;
			File f = new File(directory, filename);
			return new FileBasketItem(basket, f.getAbsolutePath(), json);
		}		
	}

	@Override
	public boolean persist() throws Exception {
		StandardOpenOption opt = new File(pathname).exists() ? 
			StandardOpenOption.TRUNCATE_EXISTING : 
			StandardOpenOption.CREATE_NEW;		
		Files.write(Paths.get(pathname), getContent().getBytes(), opt);
		return new File(pathname).isFile();
	}

	@Override
	public boolean delete() {
		return new File(pathname).delete();
	}
	
	
	public static void main(String[] args) {
		// RESUME NEXT: Write something here to test split()
	}
}
