package beans;

public class ShainBean {
	
	//社員属性(プロパティ)
	private int id;
	private String name;
	private String namekana;
	private int entryyear;
	private String gender;
	private String jobclass;

	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamekana() {
		return namekana;
	}
	public void setNamekana(String namekana) {
		this.namekana = namekana;
	}
	public int getEntryyear() {
		return entryyear;
	}
	public void setEntryyear(int entryyear) {
		this.entryyear = entryyear;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getJobclass(){
		return jobclass;
	}
	public void setJobclass(String jobclass) {
		this.jobclass = jobclass;
	}
	
	
}
