package bu.ist.visreg.job;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;

import bu.ist.visreg.backstop.BackstopJson;
import bu.ist.visreg.backstop.Scenario;
import bu.ist.visreg.util.JsonUtils;

/**
 * A JobDefinition has two functions:
 * 
 * 1) A JobDefinition is basically an extension to a BackstopJson file providing an additional top level "inheritance" scenario for all
 * the scenarios in the scenarios collection to inherit properties from that they don't explicitly declare themselves. The resulting
 * scenario will in turn inherit from a predefined default scenario any properties left over that are still not explicitly declared.
 * 
 * 2) Can break itself into multiple BackstopJson configurations such that each of those configurations has only one scenario in its
 * scenarios collection. The idea behind this is to be able to implement batching of scenarios at a higher level and not at the
 * configuration level. For example, a docker container can be run once for one any given scenario that came with the original job
 * definition independent of the other scenarios that accompanied.
 *  
 * @author wrh
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDefinition extends BackstopJson {

	private Scenario scenarioInheritance;
	
	public static JobDefinition getInstance(String json) throws JsonMappingException, JsonProcessingException {
		JobDefinition def = null;
		if(json != null) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			def = mapper.readValue(json, JobDefinition.class);
		}
		return def;
	}
	
	public JobDefinition() {
		/* Avoids com.fasterxml.jackson.databind.exc.MismatchedInputException by offering a default constructor for deserialization */
	}
	
	/**
	 * A JobDefinition is basically
	 * @return
	 * @throws Exception
	 */
	@JsonIgnore
	public List<BackstopJson> getBackstops() throws Exception {
		ArrayList<BackstopJson> backstops = new ArrayList<BackstopJson>();
		for(Scenario scenario1 : getScenarios()) {
			/**
			 * RESUME NEXT:
			 * 2) Then process backstops in VisRegJob.process(). NOTE: How to get the docker container to operate once for each backstop in backstops? (The container is already running)
			 */
			ObjectMapper objectMapper = new ObjectMapper();
			
			// 1) Merge this instance with the default instance of BackstopJson (non-null values of this instance prevail over default values);
			ObjectReader objectReader = objectMapper.readerForUpdating(BackstopJson.getDefaultInstance());
			BackstopJson backstop = objectReader.readValue(this.toJson());
			
			// 2) Merge the inherited scenario with an instance of a default scenario (non-null inherited values prevail over default values).
			objectReader = objectMapper.readerForUpdating(Scenario.getDefaultInstance());
			Scenario scenario2 = objectReader.readValue(getScenarioInheritance().toJson());
			
			// 3) Merge the scenario for this iteration with the anything it can "inherit" from the inheritance scenario.
			objectReader = objectMapper.readerForUpdating(scenario2);
			Scenario scenario3 = objectReader.readValue(scenario1.toJson());
			
			// 4) Remove the default scenario.
			backstop.setDefaultScenario(null);
			
			// 5) Replace the scenarios collection with a single entry collection containing the merged scenario.
			backstop.clearScenarios().addScenario(scenario3);
			
			// 6) Add the resulting backstop json object to the collection.
			backstops.add(backstop);
		}
		return backstops;
	}

	public void setScenarioInheritance(Scenario scenarioInheritance) {
		this.scenarioInheritance = scenarioInheritance;
	}

	public Scenario getScenarioInheritance() {
		return scenarioInheritance;
	}

	@JsonIgnore
	public String toJson() throws JsonProcessingException {
		return JsonUtils.toJson(this);
	}
	
	public static void main(String[] args) throws Exception {
		// 1) Object to json: Set the properties of an "empty" JobDefinition instance and print out the json it produces.
		JobDefinition def = new JobDefinition();
		def.setId("MyJob");
		def.setScenarioInheritance(Scenario.getDefaultInstance().setLabel("Default-Label"));
		def.addScenario(new Scenario()
				.setLabel("label1")
				.setReferenceUrl("https://ref-domain.bu.edu/main.htm")
				.setUrl("https://ref-domain.bu.edu/main-test.htm")
				.setLoginUrl("https://ref-domain.bu.edu/login.htm"));		
		def.addScenario(new Scenario()
				.setLabel("label2")
				.setReferenceUrl("https://ref-domain.bu.edu/some/other/page.htm")
				.setUrl("https://ref-domain.bu.edu/some/other/page-test.htm"));		
		def.addScenario(new Scenario()
				.setLabel("label3")
				.setReferenceUrl("https://ref-domain.bu.edu/another/web/page.htm")
				.setUrl("https://ref-domain.bu.edu/another/web/page-test.htm"));		
		String json = def.toJson();
		System.out.println(json);
		System.out.println();
		
		// 2) Json to object: Use the json output above to feed into the factory method that produces an instance out of json.
		JobDefinition def2 = JobDefinition.getInstance(json);
		System.out.println(def2.toJson());
		
		int counter = 1;
		for(BackstopJson bs : def2.getBackstops()) {
			System.out.println();
			System.out.println("BACKSTOPJSON " + String.valueOf(counter++) + ":");
			JsonUtils.printJson(bs);
		}
	}
	
}
