package bu.ist.visreg.basket.s3;

import bu.ist.visreg.basket.Basket;
import bu.ist.visreg.basket.BasketItem;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Basket extends Basket {

	private String bucketPath;
	private S3Object subfolder;
	private S3Bucket bucket;
	
	public S3Basket(BasketEnum basketEnum, S3BasketSystem parent) {
		super.basketEnum = basketEnum;
		super.parent = parent;
		this.bucket = ((S3BasketSystem) parent).getBucket();
	}

	@Override
	public void createIfNotExists() throws Exception {
		getIdentifier();
		
		subfolder = bucket.getSubFolder(basketEnum.getBasketRelativeLocation());
		if(subfolder == null) {
			bucket.createSubfolder(basketEnum.getBasketRelativeLocation());
			subfolder = bucket.getSubFolder(basketEnum.getBasketRelativeLocation());
		}
	}

	@Override
	public void load() {
		// RESUME NEXT: for each bucket subfolder, load the contents of those that match in name to basketEnum.getBasketName()
		
		bucket.getS3Objects().values().stream()
			.filter(o -> o.key().startsWith(subfolder.key()))
			.forEach(o -> {
				String content = null;
				// RESUME NEXT: Download/Stream the actual file content down from S3 into this variable.
				BasketItem bi = new S3BasketItem(this, o.key(), content);
				addBasketItem(bi);
			});
		
//		for(File f : folder.listFiles()) {
//			BasketItem bi = new FileBasketItem(this, f.getAbsolutePath(), getFileContent(f));
//			addBasketItem(bi);
//		}
	}

	@Override
	public String getIdentifier() {
		if(bucketPath == null) {
			bucketPath = parent.getRootLocation() + "/" + super.basketEnum.getBasketRelativeLocation();
		}
		return bucketPath;
	}

}
