package code;

public class ASearchResult {

	public String roadName;
	public double distance;
	public boolean markedForDeletion;

	public ASearchResult(String roadName, double distance, boolean markedForDeletion) {
		this.roadName = roadName;
		this.distance = distance;
		this.markedForDeletion = markedForDeletion;
	}
}
