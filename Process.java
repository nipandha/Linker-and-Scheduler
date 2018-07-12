package data;

public class Process {

	public int finishing_time,turnaround_time,IO_time,waiting_time,C_remaining,A,B,C,M,burst_cycles,block_cycles;
	public String state;
	/**
	 * @param args
	 */
	public Process(int a,int b,int c,int m)
	{
		A=a;
		B=b;
		C=c;
		C_remaining=c;
		M=m;
		burst_cycles=0;
		block_cycles=0;
		state="unstarted";
		IO_time=0;
		waiting_time=0;
	}
	public void printSummary(int i)
	{
		System.out.println("\nProcess "+Integer.toString(i)+":");
		System.out.println("\t(A,B,C,M) = ("+Integer.toString(A)+","
				+Integer.toString(B)+","+Integer.toString(C)+","
				+Integer.toString(M)+")");
		System.out.println("\tFinishing Time: "+Integer.toString(finishing_time));
		System.out.println("\tFinishing Time: "+Integer.toString(turnaround_time));
		System.out.println("\tI/O Time: "+Integer.toString(IO_time));
		System.out.println("\tWaiting Time: "+Integer.toString(waiting_time));
	}
	
	public void printProcess()
	{
		System.out.print("(");
		System.out.print(A);
		System.out.print("  ");
		System.out.print(B);
		System.out.print("  ");
		System.out.print(C);
		System.out.print("  ");
		System.out.print(M);
		System.out.print(")  ");
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
