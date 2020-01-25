package bu.ist.visreg.basket.s3;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;

public class S3BasketItem extends BasketItem {

	public S3BasketItem(Basket basket, String pathname, String content) {
		super(basket, pathname, content);
	}

	@Override
	public boolean commitBasketMove(Basket nextBasket) {
		// TODO Auto-generated method stub
		return false;
	}

}
