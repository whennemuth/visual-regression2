package bu.ist.visreg.backstop;

import java.util.ArrayList;
import java.util.List;

import bu.ist.visreg.basket.BasketItem;
import bu.ist.visreg.basket.BasketItemSplitter;
import bu.ist.visreg.job.JobDefinition;

public abstract class BackstopSplitter implements BasketItemSplitter {

	@Override
	public List<BasketItem> splitIntoPieces(BasketItem unsplitItem) throws Exception {
		JobDefinition def = JobDefinition.getInstance(unsplitItem.getContent());
		List<BackstopJson> backstops = def.getBackstops();
		List<BasketItem> basketItems = new ArrayList<BasketItem>(backstops.size());
		if(backstops.isEmpty()) {
			return basketItems;
		}
		else if(backstops.size() == 1) {
			// Replace the content of the JobDefinition file with what it should be after converted to a single backtopJson file.
			String json = backstops.get(0).toJson();
			BasketItem item = pieceToBasketItem(unsplitItem, json, unsplitItem.getPathname());
			// item.persist();
			basketItems.add(item);
		}
		else {
			// Break the JobDefinition into a collection of backstopJson files (one for each scenario).
			for(BackstopJson bs : backstops) {
				BasketItem item = pieceToBasketItem(unsplitItem, bs.toJson(), bs.getId());
				// item.persist();
				basketItems.add(item);
			}
		}
		return basketItems;
	}

}
