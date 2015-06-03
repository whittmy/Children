package children.lemoon.reqbased.entry;

//ok
import java.io.Serializable;

public class InterceptTime implements Serializable {

	private static final long serialVersionUID = -7730984304948219353L;
	String date;
	String day;
	String hours;
	String minutes;
	String month;
	String seconds;
	String time;
	String timezoneOffset;
	String year;

	public String getDate() {
		return this.date;
	}

	public String getDay() {
		return this.day;
	}

	public String getHours() {
		return this.hours;
	}

	public String getMinutes() {
		return this.minutes;
	}

	public String getMonth() {
		return this.month;
	}

	public String getSeconds() {
		return this.seconds;
	}

	public String getTime() {
		return this.time;
	}

	public String getTimezoneOffset() {
		return this.timezoneOffset;
	}

	public String getYear() {
		return this.year;
	}

	public void setDate(String paramString) {
		this.date = paramString;
	}

	public void setDay(String paramString) {
		this.day = paramString;
	}

	public void setHours(String paramString) {
		this.hours = paramString;
	}

	public void setMinutes(String paramString) {
		this.minutes = paramString;
	}

	public void setMonth(String paramString) {
		this.month = paramString;
	}

	public void setSeconds(String paramString) {
		this.seconds = paramString;
	}

	public void setTime(String paramString) {
		this.time = paramString;
	}

	public void setTimezoneOffset(String paramString) {
		this.timezoneOffset = paramString;
	}

	public void setYear(String paramString) {
		this.year = paramString;
	}

	public String toString() {
		return this.year + "-" + this.month + "-" + this.date + " " + this.hours + ":" + this.minutes;
	}
}
