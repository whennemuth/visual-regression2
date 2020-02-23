package bu.ist.visreg;

import java.util.ArrayList;
import java.util.List;

import bu.ist.visreg.backstop.BackstopSplitter;
import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.Basket.BasketEnum;
import bu.ist.visreg.basket.BasketItem;
import bu.ist.visreg.basket.BasketItemSplitter;
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
			BasketItemSplitter splitter = new BackstopSplitter() {
				@Override public BasketItem pieceToBasketItem(BasketItem bi, String json, String pathname) {
					return bi.getSplitItem(json, pathname);
				}				
			};
			try {
				bs = BasketSystem.getInstance(
					BasketType.getValue(parser.getString("b|basket-type")),
					parser.getString("r|root"),
					splitter);
			} 
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			processor = new Processor(bs);
		}
		else {
			System.err.println("Missing/Insufficient parameters!");
		}
		
		return processor;
	}
	
	private static void printUsage() {
		System.out.println("\n"
				+ "Usage:\n"
				+ "  b|basket-type: "
				+ BasketType.concatenatedList()
				+ "\n"
				+ "  r|root: Path where the basket system physically exists.\n"
				+ "\n"
				+ "Examples:\n"
				+ "  --basket-type " + BasketType.FILESYSTEM.name() + " --root /opt/visual-regression/baskets\n"
				+ "  --basket-type " + BasketType.S3.name() + " --root my-bucket\n");
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
	 * @param args
	 */
	public static void main(String[] args) {
		
		Processor processor = parseargs(args);

		if(processor == null) {		
			printUsage();
			System.exit(1);
		} 
		else {			
			processor.process(BasketEnum.INBOX);			
		}		
	}
}
