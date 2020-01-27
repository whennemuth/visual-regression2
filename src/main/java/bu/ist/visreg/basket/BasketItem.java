package bu.ist.visreg.basket;

import bu.ist.visreg.basket.Basket.BasketEnum;

public abstract class BasketItem {

	protected boolean failed;
	protected Basket basket;
	protected String pathname;
	protected String content;
	
	public BasketItem(Basket basket, String pathname, String content) {
		this.basket = basket;
		this.pathname = pathname;
		this.content = content;
	}
	public boolean failed() {
		return failed;
	}
	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	public Basket getBasket() {
		return basket;
	}
	public void setBasket(Basket basket) {
		this.basket = basket;
	}
	public String getPathname() {
		return pathname;
	}
	public String getContent() {
		return content;
	}

	public boolean inProcessingBasket() {
		return basket.getEnum().equals(BasketEnum.IN_PROCESS);
	}
	
	public boolean gotoNextBasket() throws Exception {
		Basket nextBasket = basket.getNextBasket(this);
		if(nextBasket != null && ! nextBasket.equals(basket)) {			
			basket.removeBasketItem(this);			
			nextBasket.addBasketItem(this);
			this.basket = nextBasket;
			
			try {
				commitBasketMove(nextBasket);
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				System.err.println(String.format(
						"ERROR! Failed to move %s to %s",
						pathname,
						nextBasket.getIdentifier()));
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * The basket system has been updated to reflect items moving between baskets at this point, 
	 * but it is just a representation of the real thing, an underlying storage system (ie: files, 
	 * cloudstorage, etc). So, move the actual item.
	 */	
	public abstract void commitBasketMove(Basket nextBasket) throws Exception;
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((basket == null) ? 0 : basket.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((pathname == null) ? 0 : pathname.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BasketItem))
			return false;
		BasketItem other = (BasketItem) obj;
		if (basket == null) {
			if (other.basket != null)
				return false;
		} else if (!basket.equals(other.basket))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (pathname == null) {
			if (other.pathname != null)
				return false;
		} else if (!pathname.equals(other.pathname))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "BasketItem [pathname=" + pathname + ", failed=" + failed + "]";
	}
	
	
}
