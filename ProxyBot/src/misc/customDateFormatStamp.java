package misc;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

public class customDateFormatStamp extends DateFormat {

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo,
			FieldPosition fieldPosition) {
		toAppendTo.append(pad4(date.getYear()));
		toAppendTo.append(pad2(date.getMonth()));
		toAppendTo.append(pad2(date.getDate()));
		toAppendTo.append(pad2(date.getHours()));
		toAppendTo.append(pad2(date.getMinutes()));
		toAppendTo.append(pad2(date.getSeconds()));
		return toAppendTo;
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String pad2(int num) {
		String s = ""+num;
		while (s.length() < 2)
			s = "0" + s;
		return s;
	}
	private static String pad4(int num) {
		String s = ""+num;
		while (s.length() < 4)
			s = "0" + s;
		return s;
	}
}
