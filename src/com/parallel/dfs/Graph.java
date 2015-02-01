package com.parallel.dfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Graph {
	private int size;//Number of Nodes
	private int[][] vertices;//Adjacency Matrix
	private Stack<Integer> globalStack;
	private List<Stack<Integer>> localStacks;
	public List<Stack<Integer>> getLocalStacks() {
		return localStacks;
	}
	public void setLocalStacks(List<Stack<Integer>> localStacks) {
		this.localStacks = localStacks;
	}

	private boolean[] visited;
	private boolean isDone;
	private int counter;
	public Graph(int size,boolean[] visited,int numberOfProcessors){
		this.size = size;
		localStacks = new ArrayList<Stack<Integer>>(numberOfProcessors);
		for(int i=0;i<numberOfProcessors;i++){
			localStacks.add(new Stack<Integer>());
		}
		vertices = new int[size][size];
		this.visited = visited;
		isDone = false;
		globalStack = new Stack<Integer>();
		globalStack.push(size-1);
		counter = 0;
		for(int i = 0; i<this.size; i++)
			for(int j = 0; j<this.size; j++){
				Random boolNumber = new Random();
                boolean edge = boolNumber.nextBoolean();
                if(i==j)
                	vertices[i][j]=1;
                else	
                	vertices[i][j]=edge ? 1 : 0;
			}
	}
	public int getSize(){
		return size;
	}
	
	public synchronized boolean getVisited(int index){
		return visited[index];
	}
	
	public synchronized void setVisited(int index, boolean value){
		visited[index] = value;
	}
	
	public synchronized void pushStack(Stack<Integer> tmp){
		while(!tmp.isEmpty()){
			globalStack.push(tmp.pop());
		}
	}
	
	public boolean isNeighbour(int node, int neighbour){
		return vertices[node][neighbour]==1 ? true : false;
	}
	
	public synchronized void incrementCounter(){
		counter++;
	}
	
	public boolean isDone() {
		return isDone;
	}
	
	public synchronized void dfs(){
		while(!isDone && globalStack.empty()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int index = (int)(Thread.currentThread().getId());
		if(!globalStack.isEmpty()){
			boolean popped = false;
			int node = globalStack.pop();
			popped = true;
			while(visited[node]){
				if(globalStack.empty()){
					isDone = true;
					popped = false;
					break;
				}else{
					node = globalStack.pop();
					popped = true;
				}
			}
			if(popped){
				visited[node] = true;
				counter++;
				boolean flag = false;
				for(int i = 0;i<size;i++){
					if(node==i)continue;
					if(isNeighbour(node, i) && !visited[i] && !flag){
						localStacks.get(index).push(i);
						flag = true;
					}
					if(isNeighbour(node, i) && !visited[i] && flag){
						globalStack.push(i);
					}
				}
			}
		}
		if(globalStack.empty())
			isDone = true;
		if(isDone && counter<size){
			isDone = false;
			for(int i=0;i<size;i++){
				if(!visited[i])
					globalStack.push(i);
			}
		}
		notifyAll();
	}
	
}
