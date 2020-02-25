package microbat.examples.benchmark;

public class Benchmark5 {
	public int test(int[] a, Student s){
		if(a[3] == 20){
			if(s.male){
				if(s.score > 10){
					return 5;
				}
				else{
					if(s.friend.id == 5){
						return 1;
					}
				}
			}
			
			return 0;
		}
		
		return 1;
	}
}
