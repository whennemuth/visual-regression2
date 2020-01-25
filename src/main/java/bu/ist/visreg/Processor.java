package bu.ist.visreg;

import java.util.ArrayList;
import java.util.List;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.Basket.BasketEnum;
import bu.ist.visreg.basket.BasketItem;
import bu.ist.visreg.basket.BasketSystem;
import bu.ist.visreg.basket.BasketSystem.BasketType;
import bu.ist.visreg.job.VisRegJob;
import bu.ist.visreg.util.ArgumentParser;

public class Processor {

	private BasketSystem basketSystem;
	
	public Processor(BasketSystem basketSystem) {
		this.basketSystem = basketSystem;
	}

	private static Processor parseargs(String[] args) {
		
		Processor processor = null;
		ArgumentParser parser = new ArgumentParser(args);
		
		if(parser.has("b|basket-type", "r|root")) {
			BasketSystem bs = null;
			try {
				bs = BasketSystem.getInstance(
					BasketType.getValue(parser.getString("basket-type")),
					parser.getString("root"));
			} 
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			processor = new Processor(bs);
		}
		else {
			System.err.println("Missing/Insufficient parameters!");
			System.exit(1);
		}
		
		return processor;
	}
	
	private static void printUsage() {
		
	}

	public void process(BasketEnum basketType) {
		if(basketSystem == null) {
			System.err.println("No basket system. Exiting...");
			return;
		}
		Basket basket = basketSystem.getBasket(basketType);
		System.out.println("Processing basket: " + basket.getIdentifier());
		
		List<BasketItem> basketItems = new ArrayList<BasketItem>(basket.getBasketItems());
		if(basketItems.isEmpty()) {
			System.out.println("No items to process in the " + basket.getEnum().getBasketName() + " basket.");
		}
		else {
			outerloop:
			for(BasketItem basketItem : basketItems) {
				try {
					do {
						if(basketItem.inProcessingBasket()) {
							new VisRegJob(basketItem).process();
						}
					} while(basketItem.gotoNextBasket());
				} 
				catch (Exception e) {
					e.printStackTrace();
					break outerloop;
				}
			}			
		}
	}
	
	
	/**
	 * RESUME NEXT: Test this for input parameters "c:\\whennemuth\\scrap\\visreg", "filessytem"
	 * @param args
	 */
	public static void main(String[] args) {
		
		Processor processor = parseargs(args);

		if(processor == null) {		
			printUsage();			
		} 
		else {			
			processor.process(BasketEnum.INBOX);			
		}		
	}
}
