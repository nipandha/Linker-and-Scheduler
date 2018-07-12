import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import data.Process;

public class Fcfs {

	public int current_process,current_cycle,current_random,
	no_processes,terminated,used_cycles,io_cycles;
	List<Integer> ready_processes;
	public List<Process> processes;
	List<Integer> l;
	boolean blocked,verbose;
	public String input_file;
	public String getInput_file() {
		return input_file;
	}
	public void setInput_file(String input_file) {
		this.input_file = input_file;
	}
	/**
	 * @param args
	 */
	public Fcfs()
	{
		current_process=0;
		current_cycle=0;
		current_random=0;
		terminated=0;
		used_cycles=0;
		io_cycles=0;
		verbose=false;
		readRandom();
		ready_processes=new ArrayList<Integer>();
	}
	public void readProcesses()
	{
		processes=new ArrayList<Process>(); 
		try {
			Scanner scanner = new Scanner(new File(input_file));
			no_processes=scanner.nextInt();
			for(int i=0;i<no_processes;i++)
			{
				int a,b,c,m;
				String s= scanner.next();
				s=s.replace("(", "");
				a=Integer.parseInt(s);
				s=scanner.next();
				b=Integer.parseInt(s);
				s=scanner.next();
				c=Integer.parseInt(s);
				s=scanner.next();
				s=s.replace(")","");
				m=Integer.parseInt(s);
				Process p=new Process(a,b,c,m);
				processes.add(p);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void readRandom()
	{
		l=new ArrayList<Integer>();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("data/Random");
		Scanner scanner = new Scanner(in);
		while(scanner.hasNextInt())
		{
		     l.add(scanner.nextInt());
		}
		scanner.close();
	}
	public void printRandom()
	{		
		for(int i : l)
		{
			System.out.println(i);
		}
	}
	public int randomOs(int U)
	{
		int X=l.get(current_random);
		current_random+=1;
		return 1+(X%U);
		
	}
	public void printProcesses()
	{
		System.out.print("\nThe original input was: ");
		System.out.print(no_processes);
		System.out.print("  ");
		for(int i=0;i<no_processes;i++)
		{
			processes.get(i).printProcess();
		}
		bubblesort();
		System.out.print("\nThe sorted input is:  ");
		System.out.print(no_processes);
		System.out.print("  ");
		for(int i=0;i<no_processes;i++)
		{
			processes.get(i).printProcess();
		}
	}
	public void bubblesort()
	{
		int flag;
		Process temp;
		for(int i=0;i<no_processes;i++)
		{
			flag=0;
			for(int j=0;j<no_processes-i-1;j++)
			{
				if(processes.get(j).A>processes.get(j+1).A)
				{
					flag+=1;
					temp=processes.get(j);
					processes.set(j, processes.get(j+1));
					processes.set(j+1,temp);
				}
			}
			if(flag==0)
				break;
		}
	}
	public void printProcessesState()
	{
		System.out.print("\nBefore cycle\t");
		System.out.print(current_cycle);
		System.out.print(":\t");
		for(int i=0;i<no_processes;i++)
		{
			Process p=processes.get(i);
			System.out.print(p.state);
			if(p.state.equals("running")||p.state.equals("ready")||
					p.state.equals("unstarted")||p.state.equals("terminated"))
				System.out.print("\t"+Integer.toString(p.burst_cycles)+".\t");
			if(p.state.equals("blocked"))
				System.out.print("\t"+Integer.toString(p.block_cycles)+".\t");
		}
		
	}
	
	public void updateState(int x) {
		// TODO Auto-generated method stub
		Process p=processes.get(x);
		if(p.state.equals("blocked"))
		{
			blocked=true;
			p.block_cycles-=1;
			p.IO_time++;
			if(p.block_cycles==0)
			{
				p.state="ready";
				//System.out.println("\nAdded "+Integer.toString(x));
				ready_processes.add(x);
			}
		}
		else if(p.state.equals("ready"))
			p.waiting_time++;
		else if(p.state.equals("unstarted")&&(p.A<=current_cycle))
		{
			p.state="ready";
			ready_processes.add(x);
		}
	}
	public void updateCurrentProcess()
	{
		
		if (ready_processes.size()>0) 
		{
			current_process = ready_processes.get(0);
			ready_processes.remove(0);
			Process p = processes.get(current_process);
			p.state = "running";
			if (p.burst_cycles == 0) {
				if (p.A <= current_cycle) {
					int v = randomOs(p.B);
					if (v <= p.C_remaining)
						p.burst_cycles = v;
					else
						p.burst_cycles = p.C_remaining;
					p.block_cycles = p.burst_cycles * p.M;

				}
			}
		}
		else
			current_process=-1;
	}
	public void incrementCurrentProcess()
	{
		current_process=(current_process+1)%no_processes;
	}
	public void updateStateCurrentProcess(int x)
	{
		Process p=processes.get(x);
		if(p.state.equals("unstarted")||p.state.equals("ready"))
		{
			if(p.A<=current_cycle)
			{
				p.state="running";
				int v=randomOs(p.B);
				if(v<=p.C_remaining)
					p.burst_cycles=v;
				else
					p.burst_cycles=p.C_remaining;
				p.block_cycles=p.burst_cycles*p.M;
				
			}
		}
		else if (p.state.equals("running"))
		{
			used_cycles++;
			
			p.burst_cycles-=1;
			p.C_remaining-=1;
			if(p.burst_cycles==0)
			{
				//System.out.println("\nBlock cycles are: "+Integer.toString(p.block_cycles));
				if(p.C_remaining==0)
				{
					terminate(p);
					terminated+=1;
				}
				else if(p.block_cycles>0)
				{
					
					p.state="blocked";
				}
				else
				{
					p.state="ready";
					ready_processes.add(x);
				}
				
			}
		}
	}
	public void terminate(Process p) {
		// TODO Auto-generated method stub
		p.state="terminated";
		p.finishing_time=current_cycle;
		p.turnaround_time=p.finishing_time-p.A;
	}
	public void printSummary()
	{
		System.out.println("\nSummary Data:");
		System.out.println("\tFinishing Time: "+Integer.toString(current_cycle));
		System.out.println("\tCPU Utilization: "+ Float.toString((float)used_cycles/current_cycle));
		System.out.println("\tI/O Utilization: "+ Float.toString((float)io_cycles/current_cycle));
		System.out.println("\tThroughput: "+Float.toString(((float)no_processes*100/current_cycle))+" processes per hundred cycles");
		float avg_t=0,avg_w=0;
		for(int i=0;i<no_processes;i++)
		{
			avg_t+=processes.get(i).turnaround_time;
			avg_w+=processes.get(i).waiting_time;
		}
		avg_t/=no_processes;
		avg_w/=no_processes;
		System.out.println("\tAverage Turnaround Time: "+Float.toString(avg_t));
		System.out.println("\tAverage Waiting Time: "+Float.toString(avg_w));
	}
	public void fcfs()
	{
		
		for(int t=0;t<Integer.MAX_VALUE;t++)
		{
			current_cycle=t;
			blocked=false;
			if(verbose)
				printProcessesState();
			//update states..if current,state==blocked find next.. till found
			
			
			for(int i=0;i<no_processes;i++)
			{
				if((i==current_process)&&(!processes.get(current_process).state.equals("blocked")))
					updateStateCurrentProcess(current_process);
				else 
					updateState(i);
			}
			if(current_process==-1)
			{
				updateCurrentProcess();
				
			}	
			else if(!processes.get(current_process).state.equals("running"))
			{
				updateCurrentProcess();
			}
			/*if((t%10)==0)
			{
				System.out.println("See op ");
				Scanner scanner = new Scanner(System.in);
				String username = scanner.nextLine();
			}*/
			if(blocked)
				io_cycles++;
			if(terminated==no_processes)
			{
				System.out.println("\nThe scheduling algorithm used was First Come First Served");
				break;
			}
		}
		for(int i=0;i<no_processes;i++)
			processes.get(i).printSummary(i);
		printSummary();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Fcfs s=new Fcfs();
		if(args[0].equals("--verbose"))
		{
			s.setInput_file(args[1]);
			s.verbose=true;
		}
		else
			s.setInput_file(args[0]);
		s.readProcesses();
		s.printProcesses();
		s.fcfs();
		
	}

}
