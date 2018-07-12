/*
 ============================================================================
 Name        : Linker.c
 Author      : Nitisha
 Version     :
 Copyright   : Your copyright notice
 Description : Hello World in C, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>

int main(int argc, char** argv) {
	int no_modules;
	FILE * fp;
	char ip_name[70];
	char mm_error[100][10],sym_error[50][8];
	int no_warnings=0,no_errors=0;
	int j,l;
	char warnings[20][60],warning[60],errors[20][70];
	strcpy(ip_name,argv[1]);


	fp = fopen (ip_name, "r+");
	fscanf(fp,"%d",&no_modules);
	int mod=0,flag=0;
	int no_symbols,s=0,u=0,no_uses,no_instructions,s_,u_,i=0,i_,num;
	char sym[50][10],use[50][10];
	int loc[50],memory_map[100],module_start[20],temp[10],temp2[10];
	char type[100],t[20],used_symbol[20];
	module_start[0]=0;
	for(j=0;j<100;j++)
		strcpy(mm_error[j],"-1");
	for(j=0;j<50;j++)
		strcpy(use[j],"-1");
	int v,w;
	/****---- Pass 1 ----****/
	for(mod=0;mod<no_modules;mod++)
	{
		v=0;
		for(j=0;j<10;j++)
			temp[j]=-1;
		fscanf(fp,"%d",&no_symbols);
		for(s_=0;s_<no_symbols;s_++)
		{
			fscanf(fp,"%s %d",used_symbol,&l);
			flag=0;
			for(j=0;j<s;j++)
			{
				if(strcmp(sym[j],used_symbol)==0)
				{
					flag=1;
//					printf("\nset");
					strcpy(sym_error[j],"multiple");
					break;
				}
			}
			if(flag==0)
			{strcpy(sym[s],used_symbol);
			loc[s]=l+module_start[mod];
			temp[v++]=s;
			s++;}
			//printf("The symbol %s is located at %d",sym[s-1],loc[s-1]);
		}

		fscanf(fp,"%d",&no_uses);
		w=0;
		for(u_=0;u_<no_uses;u_++,u++)
		{
			fscanf(fp,"%s",used_symbol);
			while(1)
			{
				fscanf(fp,"%d",&num);
				if(num==-1)
					break;
				if(strcmp(use[num+module_start[mod]],"-1")==0)
				{
					temp2[w++]=num;
					strcpy(use[num+module_start[mod]],used_symbol);
				}
				else
				{
					strcpy(mm_error[num+module_start[mod]],"multiple");
				}
				//printf("The symbol used at %d is %s",num+module_start[mod],use[num+module_start[mod]]);
			}
			//use_relative_loc[u]+=module_start[mod];
			//printf("The use %s is located at %d %d",use[u],use_relative_loc[u],minus1);
		}
		fscanf(fp,"%d",&no_instructions);
		//Check definition exceeds module size
		for(j=0;j<v;j++)
		{
			if(loc[temp[j]]-module_start[mod]>=no_instructions)
			{
				strcpy(sym_error[temp[j]],"size");
				loc[temp[j]]=module_start[mod];
			}
		}
		for(j=0;j<w;j++)
		{
			if(temp2[j]>=no_instructions)
			{
				sprintf(errors[no_errors++],"\nError: Use of %s in module %d exceeds module size; use ignored.",use[temp2[w]+module_start[mod]],(mod+1));
				strcpy(use[num+module_start[mod]],"-1");
			}
		}
		for(i_=0;i_<no_instructions;i_++,i++)
		{
			fscanf(fp,"%s %d",&t,&memory_map[i]);
			type[i]=t[0];
			if(type[i]=='R')
			{

				if((memory_map[i]%1000)>=no_instructions)
				{
					strcpy(mm_error[i],"relat");
					memory_map[i]-=(memory_map[i]%1000);
				}
				else
					memory_map[i]+=module_start[mod];
			}
			if(type[i]=='A')
			{
				if((memory_map[i]%1000)>199)
				{strcpy(mm_error[i],"abs");
				memory_map[i]-=(memory_map[i]%1000);
				}
			}
			//printf("%c is at %d ",type[i],memory_map[i]);
		}

		/*printf("\n%d ",i);
		for(j=0;j<i;j++)
		{
			printf("%d ",memory_map[j]);
		}*/
		module_start[mod+1]=no_instructions+module_start[mod];
		//printf("\nThe next module starts at %d",module_start[mod+1]);
	}
	fclose(fp);

	/****---- Pass 2 ----****/

	no_instructions=i;
	no_symbols=s;
	no_uses=u;

	int defined;
	//Check for undefined variables
	for(u=0;u<no_instructions;u++)
	{
		if(strcmp(use[u],"-1")!=0)
		{
			defined=0;
			for(s=0;s<no_symbols;s++)
			{
				if(strcmp(use[u],sym[s])==0)
				{
					//printf("found %s ",use[u]);
					defined=1;
					break;
				}
			}
			if(defined==0)
			{
				//printf("\nset");
				memory_map[u]-=(memory_map[u]%1000);
				strcpy(mm_error[u],"undefined");
			}
		}
	}
	for(i=0;i<no_instructions;i++)
	{
		if(type[i]=='E')
		{
			for(s=0;s<no_symbols;s++)
			{
				if(strcmp(sym[s],use[i])==0)
				{
					//printf("%d ",memory_map[i]);
					memory_map[i]-=(memory_map[i]%1000);
					//printf("%d ",memory_map[i]);
					memory_map[i]+=loc[s];
					//printf("%d ",memory_map[i]);
					break;
				}
			}
		}
	}


	int used=0,definition=no_modules-1;
	//Check for warnings
	for(s=0;s<no_symbols;s++)
	{
		used=0;
		definition=no_modules-1;
		for(u=0;u<no_instructions;u++)
		{
			if(strcmp(use[u],sym[s])==0)
			{
				used=1;
				break;
			}
		}
		if(used==0)
		{
			for(mod=1;mod<no_modules;mod++)
			{
				if(loc[s]<module_start[mod])
				{
					definition=mod-1;
					break;
				}
			}
			sprintf(warning,"\nWarning: %s was defined in module %d but never used.",sym[s],(definition+1));
			//printf("%s",warning);
			strcpy(warnings[no_warnings],warning);
			no_warnings++;
		}
	}


	//printf("\nThe total number of instuctions are %d",no_instructions);
	/****---- Write to file ----****/
	FILE * fp1;
	char op_name[50];
	strcpy(op_name,argv[2]);
	   fp1 = fopen (op_name, "w+");
	   fprintf(fp1, "Symbol Table");
	   for(s=0;s<no_symbols;s++)
	   {
		   fprintf(fp1,"\n%s=%d",sym[s],loc[s]);
		   if(strcmp(sym_error[s],"multiple")==0)
		   {
			   fprintf(fp1," Error: This variable is multiply defined; first value used.");
		   }
		   else if(strcmp(sym_error[s],"size")==0)
		   {
			   fprintf(fp1," Error: Definition exceeds module size; first word in module used.");
		   }
	   }
	   fprintf(fp1, "\n\nMemory Map");
	   for(i=0;i<no_instructions;i++)
	   {
		   fprintf(fp1,"\n%d:\t%d",i,memory_map[i]);
		   if(strcmp(mm_error[i],"-1")!=0)
		   {
			   if(strcmp(mm_error[i],"abs")==0)
			   {
				   fprintf(fp1," Error: Absolute address exceeds machine size; zero used.");
			   }
			   else if(strcmp(mm_error[i],"undefined")==0)
			   {
				   fprintf(fp1," Error: %s is not defined; zero used.",use[i]);
			   }
			   else if(strcmp(mm_error[i],"relat")==0)
			   {
				   fprintf(fp1," Error: Relative address exceeds module size; zero used.");
			   }
			   else if(strcmp(mm_error[i],"multiple")==0)
			   {
				   fprintf(fp1," Error: Multiple variables used in instruction; all but first ignored.");
			   }
		   }
	   }

	   //Add errors
	   if(no_errors>0)
		   fprintf(fp1,"\n");
	   for(j=0;j<no_errors;j++)
	   {
		   fprintf(fp1,"%s",errors[j]);
	   }


	   //Add Warnings
	   if(no_warnings>0)
	   {
		   fprintf(fp1,"\n");
		   for(i=0;i<no_warnings;i++)
			   fprintf(fp1,"%s",warnings[i]);
	   }

	   fclose(fp1);


	   /****---- Compare Outputs ----****/
	   if(argc>3)
	   {
		   char ref_name[50];
		   strcpy(ref_name,argv[3]);
		   check(op_name,ref_name);
	   }
	return EXIT_SUCCESS;
}

void check(char *fname1,char *fname2)
{
	FILE *fp1, *fp2;
   int ch1, ch2;
   //char fname1[40], fname2[40];



   fp1 = fopen(fname1, "r");
   fp2 = fopen(fname2, "r");

   if (fp1 == NULL) {
	  printf("Cannot open %s for reading ", fname1);
	  exit(1);
   } else if (fp2 == NULL) {
	  printf("Cannot open %s for reading ", fname2);
	  exit(1);
   } else {
	   char s1[25],s2[25];
	   while(1)
	   {
		   if(fscanf(fp1,"%s",s1)!=EOF)
		   {
			   if(fscanf(fp2,"%s",s2)!=EOF)
			   {
				   if(strcmp(s1,s2)==0)
					   continue;
				   else
				   {
					   printf("\nThe outputs do not match");
					   return;
				   }
			   }
		   }
		   else if(fscanf(fp2,"%s",s2)==EOF)
		   {
			   printf("\nThe outputs do match");
			   break;
		   }
		   else
		   {
			   printf("\nThe outputs do not match");
			   return;
		   }
	   }
   }
}

