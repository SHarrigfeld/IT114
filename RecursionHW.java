public class RecursionHW {
  
	public static int sum(int num) {
		if (num > 0) {
			return num + sum(num - 1);
		}
		return 0;
	}
   public static int loop(int num)
   {
      int sum=0;
      for(int i=num;i>0; i--)
      {
         sum +=i; 
      }
      return sum; 
   }

	public static void main(String[] args) {
		System.out.println(sum(10));
      System.out.println("______________________________");
      System.out.println(loop(10));
	}
   
}