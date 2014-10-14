package Utility;

import java.util.Comparator;
import ltpService.Word;

public class ComparatorWord implements Comparator{
	public int compare(Object arg0, Object arg1) {
		Word x1 = (Word)arg0;
		Word x2 = (Word)arg1;

		int result = 0;
		if(x1.getID() > x2.getID())
			result = 1;
		else if(x1.getID() < x2.getID())
			result = -1;
		return result;
	}
}
