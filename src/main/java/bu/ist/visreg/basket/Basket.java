package bu.ist.visreg.basket;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class Basket {
	
	protected BasketSystem parent;
	protected BasketEnum basketEnum;
	protected List<BasketItem> basketItems = new LinkedList<BasketItem>();

	public abstract void createIfNotExists() throws Exception;
	
	public abstract void load() throws Exception;
	
	public abstract String getIdentifier();

	public Basket getNextBasket(BasketItem item) {
		return parent.getBasket(getEnum().getNextBasket(item));
	}
	
	public BasketEnum getEnum() {
		return basketEnum;
	}
	
	public List<BasketItem> getBasketItems() {
		return Collections.unmodifiableList(basketItems);
	}

	public void addBasketItem(BasketItem basketItem) {
		basketItems.add(basketItem);
	}

	public void removeBasketItem(BasketItem basketItem) {
		basketItems.remove(basketItem);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((basketEnum == null) ? 0 : basketEnum.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Basket))
			return false;
		Basket other = (Basket) obj;
		if (basketEnum != other.basketEnum)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}



	public static enum BasketEnum {
		INBOX("inbox"),
		IN_PROCESS("processing"),
		ERROR("error"),
		COMPLETED("completed");
		
		private String name;
		
		private BasketEnum(String name) {
			this.name = name;
		}

		public String getBasketRelativeLocation() {
			return "jobs/" + name + "/";
		}

		public String getBasketName() {
			return name;
		}
		public BasketEnum getNextBasket(BasketItem item) {
			if(item.failed()) {
				return ERROR;
			}
			switch(this) {
				case INBOX : return IN_PROCESS;
				case IN_PROCESS: return COMPLETED;
				default: return null;
			}
		}
	}
}