package bu.ist.visreg.basket.filesystem;

import java.io.File;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.Basket.BasketEnum;
import bu.ist.visreg.basket.BasketSystem;

public class FileBasketSystem extends BasketSystem {

	private File rootDirectory;
	
	public FileBasketSystem(String rootLocation) {
		super.rootLocation = rootLocation;
		this.rootDirectory = new File(rootLocation);
	}

	@Override
	public void load() throws Exception {
		
		getRootDirectory();
		
		for(Basket.BasketEnum be : BasketEnum.values()) {
			
			FileBasket basket = new FileBasket(be, this);
			
			basket.createIfNotExists();
			
			basket.load();
			
			addBasket(basket);
		}
	}

	public File getRootDirectory() {
		return rootDirectory;
	}
}
