package bu.ist.visreg.backstop;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ViewPort {
	String label;
	Integer width;
	Integer height;
	public String getLabel() {
		return label;
	}
	public ViewPort setLabel(String label) {
		this.label = label;
		return this;
	}
	public Integer getWidth() {
		return width;
	}
	public ViewPort setWidth(Integer width) {
		this.width = width;
		return this;
	}
	public Integer getHeight() {
		return height;
	}
	public ViewPort setHeight(Integer height) {
		this.height = height;
		return this;
	}
	public static ViewPort getDefaultInstance() {
		return new ViewPort()
				.setLabel("laptop")
				.setWidth(1440)
				.setHeight(900);
	}
}