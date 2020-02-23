package bu.ist.visreg.basket;

import java.util.List;

public interface BasketItemSplitter {
	
	public abstract List<BasketItem> splitIntoPieces(BasketItem unsplitItem) throws Exception;

	public abstract BasketItem pieceToBasketItem(BasketItem unsplitItem, String json, String pathname);

}
