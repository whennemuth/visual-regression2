package bu.ist.visreg.basket.filesystem;

import java.io.File;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;

public class FileBasketItem extends BasketItem {
	
	public FileBasketItem(Basket basket, String pathname, String content) {
		super(basket, pathname, content);
	}

	@Override
	public boolean commitBasketMove(Basket nextBasket) {
		
		File f = new File(pathname);
		if(f.isFile()) {
			File targetFolder = ((FileBasket) nextBasket).getFolder();
			File targetFile = new File(targetFolder.getAbsolutePath() + "\\" + f.getName());
			boolean commited = f.renameTo(targetFile);
			if(!commited) {
				System.out.println(String.format(
						"ERROR! Failed to move %s to %s",
						f.getAbsolutePath(),
						targetFolder));
				return false;
			}
			else {
				this.pathname = targetFile.getAbsolutePath();
				System.out.println(String.format(
						"Moved %s to %s",
						f.getAbsolutePath(),
						targetFolder));
				return true;
			}
		}
		return false;
	}
	
}
