package microbat.recommendation;

public class UnknownBug extends Bug{
	
	public UnknownBug(){
		String message = "Sorry, I have not detected any loop pattern for this bug, thus I cannot provide more information for help.";
		super.setMessage(message);
	}
	
}
