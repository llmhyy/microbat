package microbat.examples.benchmark;

public class Student {
	int id;
	int score;
	boolean male;
	
	Student friend;

	public Student(int id, int score, boolean male, Student friend) {
		super();
		this.id = id;
		this.score = score;
		this.male = male;
		this.friend = friend;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public Student getFriend() {
		return friend;
	}

	public void setFriend(Student friend) {
		this.friend = friend;
	}
	
	
	
	
}
