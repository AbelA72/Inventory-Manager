import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

class Item{
	String itemName = "";
	int itemQuantity = 0;
	String itemUnit = "";
	ArrayList invenList = new ArrayList();

	Item(File file){
		File file1 = new file();
		Scanner fileScan = new Scanner(file);
		itemName = fileScan.next();
		itemQuantity = fileScan.nextInt();
		itemUnit = fileScan.next();
		invenList.add();
	}


	
}
/*
class Recipe{
	Scanner rscnr = new Scanner(new File("Recipe.txt"));
	string foodName = rscnr.nextString();
	int foodQuantity = rscnr.nextInt();
	int foodUnit = rscnr.nextInt();
}

class Order{
	bill = false 
	while (bill == false){
		
	}
}
*/

public class Manage{
	static public void main(String[] args) {
		
		while (fileScan.hasNext()){
			Item item = new Item("inventory.txt");	
			invenList.add(item);
			System.out.println(invenList);
		}
	}
}
	
