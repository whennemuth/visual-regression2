package bu.ist.visreg.basket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bu.ist.visreg.basket.Basket.BasketEnum;
import bu.ist.visreg.basket.filesystem.FileBasketSystem;
import bu.ist.visreg.basket.s3.S3BasketSystem;
import bu.ist.visreg.basket.s3.S3Bucket;
import software.amazon.awssdk.regions.Region;

public abstract class BasketSystem {

	protected List<Basket> baskets = new ArrayList<Basket>();
	protected String rootLocation;
	
	public abstract void load() throws Exception;
	
	public static BasketSystem getInstance(BasketType basketType, String rootLocation) throws Exception {
		
		BasketSystem bs = null;
		switch(basketType) {
			case FILESYSTEM:
				bs = new FileBasketSystem(rootLocation);
				break;
			case S3:
				S3Bucket bucket = new S3Bucket(rootLocation, Region.US_EAST_1, true);
				bs = new S3BasketSystem(bucket);
				break;
		}
				
		bs.load();
		
		return bs;
	}
	
	public Basket getBasket(BasketEnum be) {
		for (Basket basket : baskets) {
			if(basket.getEnum().equals(be)) {
				return basket;
			}
		}
		return null;
	}
	
	public String getRootLocation() {
		return rootLocation;
	}
	
	public List<Basket> getBaskets() {
		return Collections.unmodifiableList(baskets);
	}
	
	public void addBasket(Basket basket) {
		baskets.add(basket);
	};
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootLocation == null) ? 0 : rootLocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BasketSystem))
			return false;
		BasketSystem other = (BasketSystem) obj;
		if (rootLocation == null) {
			if (other.rootLocation != null)
				return false;
		} else if (!rootLocation.equals(other.rootLocation))
			return false;
		return true;
	}


	
	public static enum BasketType {
		S3, FILESYSTEM;
		public static BasketType getValue(String val) {
			for(BasketType location : BasketType.values()) {
				if(val == null) return null;
				if(val.equalsIgnoreCase(location.name())) {
					return location;
				}
			}
			return null;
		}
	}
}
