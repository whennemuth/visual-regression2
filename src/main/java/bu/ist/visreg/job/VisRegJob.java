package bu.ist.visreg.job;

import bu.ist.visreg.basket.BasketItem;

public class VisRegJob {

	private BasketItem basketItem;
	
	public VisRegJob(BasketItem basketItem) {
		this.basketItem = basketItem;
	}
	
	/**
	 * TODO: Determine if basketItem content denotes a single visual regression parameter list or is many of them (one per line).
	 * Then write code to process it/them.
	 */
	public void process() {
		System.out.println("Processing: " + basketItem.getPathname());
		
		String job = basketItem.getContent();
		
		basketItem.setFailed(false);
		
		
	}

}
