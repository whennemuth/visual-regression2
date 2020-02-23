package bu.ist.visreg.job;

import bu.ist.visreg.backstop.BackstopJson;
import bu.ist.visreg.basket.BasketItem;

public class VisRegJob {

	private BasketItem basketItem;
	
	public VisRegJob(BasketItem basketItem) {
		this.basketItem = basketItem;
	}
	
	/**
	 * @throws Exception 
	 */
	public void process() throws Exception {
		System.out.println("Processing: " + basketItem.getPathname());
		
		String json = basketItem.getContent();
		
		JobDefinition def = JobDefinition.getInstance(json);
		
		NodeCommand cmd = null;
		String[] cmdargs = null;
		for(BackstopJson backstopJson : def.getBackstops()) {
			
			BackstopCommandArgs args = new BackstopCommandArgs(backstopJson);
			
			cmdargs = args.getCreateReferenceImagesArgs();
			cmd = new NodeCommand(cmdargs);
			cmd.run();
			
			cmdargs = args.getCreateTestImagesArgs();
			cmd = new NodeCommand(cmdargs);
			cmd.run();
		}
		
		basketItem.setFailed(false);		
	}

}
