package bu.ist.visreg.basket.s3;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;

public class S3BasketItem extends BasketItem {

	private S3Bucket bucket;

	public S3BasketItem(Basket basket, String pathname, String content) {
		super(basket, pathname, content);
		this.bucket = ((S3Basket) basket).getBucket();
	}

	@Override
	public void commitBasketMove(Basket nextBasket) throws Exception {
		super.pathname = bucket.moveObject(super.pathname, nextBasket.getIdentifier());
	}

}
