package com.w1.merchant.android.request;

import android.content.res.Resources;
import android.text.TextUtils;

import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.MenuActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class JSONParsing {
	
	/*public static int getUserEntryCount(String jsonText) {
		int count = 0;
		jsonText.indexOf("TotalCount");
		return count;
	}*/
	
	public static String invoiceError(String jsonText) {
		String result = "";
		JSONArray jArray;
		try {
			//Log.d("1", line);
			jArray = new JSONArray("[ \n" + jsonText + "\n ]");
			JSONObject json_data = jArray.getJSONObject(0);
			result = json_data.getString("ErrorDescription");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String[]> payments(String text) {
    	JSONArray jArray = null;
    	JSONArray jArray2 = null;
    	JSONObject json_data;
    	JSONObject json_data2;
    	String form, fields = "";
    	String fieldType = "";
    	String items = "";
    	ArrayList<String[]> dataPayments = new ArrayList<String[]>();
    	
    	try {
			//Log.d("1", line);
			jArray = new JSONArray("[ \n" + text + "\n ]");
			json_data = jArray.getJSONObject(0);
			form = json_data.getString("Form");
			String[] paymentId = { "PaymentId", json_data.getString("PaymentId") };
			dataPayments.add(paymentId);
			
			jArray = new JSONArray("[ \n" + form + "\n ]");
			json_data = jArray.getJSONObject(0);
			fields = json_data.getString("Fields");
			String[] formId = { "FormId", json_data.getString("FormId") };
			dataPayments.add(formId);
			
			jArray = new JSONArray(fields);
			for (int i = 0; i < jArray.length(); i++) {
		        json_data = jArray.getJSONObject(i);
		        String[] elementPayments = { "", "" };
		        fieldType = json_data.getString("FieldType");
		        if (fieldType.equals("List")) {
		        	items = json_data.getString("Items");
		        	jArray2 = new JSONArray(items);
					for (int j = 0; j < jArray2.length(); j++) {
						json_data2 = jArray2.getJSONObject(j);
						if (json_data2.getBoolean("IsSelected")) {
							elementPayments[0] = json_data.getString("FieldId");
					        elementPayments[1] = json_data2.getString("Value");
					        dataPayments.add(elementPayments);
						}
					}
		        } else if (fieldType.equals("Scalar")) {
		        	elementPayments[0] = json_data.getString("FieldId");
			        elementPayments[1] = json_data.getString("DefaultValue");
			        dataPayments.add(elementPayments);
		        }
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataPayments;
    }
	
	public static ArrayList<Map<String, Object>> userEntry(String jsonText, String userId, Resources resources) {
		//appendLog(jsonText);
    	JSONArray jArray = null;
    	ArrayList<String> numberArray, dateArray, amountArray, 
    			descrArray, stateArray, currencyArray;
        ArrayList<Integer> imgArray;
    	ArrayList<Map<String, Object>> data = null;
    	Map<String, Object> m;
    	numberArray = new ArrayList<String>();
    	dateArray = new ArrayList<String>();
    	amountArray = new ArrayList<String>();
    	descrArray = new ArrayList<String>();
    	stateArray = new ArrayList<String>();
    	currencyArray = new ArrayList<String>();
    	imgArray = new ArrayList<Integer>();
    	String entryStateId, descr, amount, currency;
    	Boolean entryState = false;
    	int start, end;
    	String pattern = "[^0-9]";
    	String descrOnlyDigits = "";
    	    	
    	start = jsonText.indexOf("[");
		end = jsonText.indexOf("]") + 1;
		char[] buffer = new char[end - start];
		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray(new String(buffer));
			for (int i = 0; i < jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        
		        dateArray.add(dateFormat(json_data.getString("CreateDate"), resources));
		        
		        descr = "";
		        try {
		        	descr = json_data.getString("Description");
		        } catch (JSONException e) {
					e.printStackTrace();
				} finally {
					descrArray.add(descr);
				}
		        
		        descrOnlyDigits = descr.replaceAll(pattern, "");
		        if (json_data.getString("OperationTypeId").equals("ProviderPayment")) {
		        	numberArray.add(resources.getString(R.string.output_cash));
				} else if (descrOnlyDigits.isEmpty()) {
		        	numberArray.add(resources.getString(R.string.output_cash));
		        } else {
		        	if (descrOnlyDigits.length() > 12) {
		        		numberArray.add(descrOnlyDigits.substring(descrOnlyDigits.length() - 12));
		        	} else {
		        		numberArray.add(descrOnlyDigits);
		        	}
				}
//		        if (json_data.getString("OperationTypeId").equals("ProviderPayment")) {
//		        	numberArray.add(MenuActivity.getContext().getString(R.string.output_cash));
//				} else {
//					numberArray.add(json_data.getString("FromUserId"));
//				}
		        
		        entryStateId = json_data.getString("EntryStateId");
		        entryState = entryStateId.equals("Processing");
		        if (json_data.getString("FromUserId").equals(userId)) {
		        	amount = Math.round((Float.parseFloat(json_data.getString("Amount")) +
		        			Float.parseFloat(json_data.getString("CommissionAmount")))) + "";
		        } else {
		        	amount = Math.round((Float.parseFloat(json_data.getString("Amount")) -
		        			Float.parseFloat(json_data.getString("CommissionAmount")))) + "";
		        }
//		        if (amount.endsWith(".0")) {
//		        	amount = amount.replace(".0", "");
//				}
		        amount = formatNumber(amount);
		        currency = getCurrencySymbol(json_data.getString("CurrencyId"));
		        if (currency.equals("RUB")) {
		        	currency = "A";
		        } else {
		        	amount += " " + currency;
		        	currency = "";
		        }
//		        if (json_data.getString("OperationTypeId").equals("ProviderPayment") |
//		        		json_data.getString("OperationTypeId").equals("Transfer")) {
		        if (json_data.getString("FromUserId").equals(userId)) {	
		        	amountArray.add("-" + amount);
		        	currencyArray.add("-" + currency);
		        } else if (entryState) {
					amountArray.add("+" + amount);
					currencyArray.add("+" + currency);
		        } else {
					amountArray.add(amount);
					currencyArray.add(currency);
				}
		        
		        if (entryStateId.equals("Accepted")) {
		        	imgArray.add(R.drawable.icon_ok);
		        	stateArray.add(resources.getString(R.string.paid));
		        } else if ((entryStateId.equals("Canceled")) | (entryStateId.equals("Rejected"))) {
		        	imgArray.add(R.drawable.icon_cancel);
		        	stateArray.add(resources.getString(R.string.canceled));
		        } else {
		        	imgArray.add(R.drawable.icon_progress);
		        	stateArray.add(resources.getString(R.string.processing));
		        }
		    }
			
	    	data = new ArrayList<Map<String, Object>>(numberArray.size());
		    for (int j = 0; j < numberArray.size(); j++) {
			      m = new HashMap<String, Object>();
			      m.put(MenuActivity.ATTRIBUTE_NAME_NUMBER, numberArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_DATE, dateArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_IMAGE, imgArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_AMOUNT, amountArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_DESCR, descrArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_STATE, stateArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_RUBL, currencyArray.get(j));
			      data.add(m);
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    return data;
    }
	
	public static ArrayList<String[]> userEntryPeriod(String jsonText) {
    	JSONArray jArray = null;
    	ArrayList<String[]> dataPeriod = new ArrayList<String[]>();
        int start, end;
    	
    	start = jsonText.indexOf("[");
		end = jsonText.indexOf("]") + 1;
		char[] buffer = new char[end - start];
		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray(new String(buffer));
			for (int i = 0; i < jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        String[] elementPeriod = { "", "", "" };
		        elementPeriod[0] = json_data.getString("Amount");
		        elementPeriod[1] = json_data.getString("CreateDate");
		        elementPeriod[2] = json_data.getString("CommissionAmount");
		        dataPeriod.add(elementPeriod);
		   }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    return dataPeriod;
    }
	
	public static int[] userEntryTotal(String jsonText) {
		int[] result = { 0, 0 };
    	JSONArray jArray = null;
    	int start, end;
    	float sum = 0;
    	float comis = 0;
    	
    	start = jsonText.indexOf("[");
		end = jsonText.indexOf("]") + 1;
		char[] buffer = new char[end - start];
		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray(new String(buffer));
			for (int i = 0; i < jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        sum += Float.parseFloat(json_data.getString("Amount"));
		        comis += Float.parseFloat(json_data.getString("CommissionAmount"));
		   }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sum = sum - comis;
		result[0] = Math.round(sum);
		result[1] = Math.round(comis);
	    return result;
    }
	
	public static ArrayList<Map<String, Object>> invoice(String jsonText,
			String hasSuspense, String currencyFilter, Resources resources) {
    	JSONArray jArray = null;
    	ArrayList<String> numberArray, dateArray, descrArray, amountArray,
    			stateArray, currencyArray;
        ArrayList<Integer> imgArray;
    	ArrayList<Map<String, Object>> data = null;
    	Map<String, Object> m;
    	numberArray = new ArrayList<String>();
    	dateArray = new ArrayList<String>();
    	amountArray = new ArrayList<String>();
    	descrArray = new ArrayList<String>();
    	imgArray = new ArrayList<Integer>();
    	stateArray = new ArrayList<String>();
    	currencyArray = new ArrayList<String>();
    	int start, end;
    	String descr, amount, currency;
    	
    	//appendLog(jsonText);
    	start = jsonText.indexOf("[");
		end = jsonText.indexOf("]") + 1;
		char[] buffer = new char[end - start];
		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray(new String(buffer));
			for (int i = 0; i < jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        if (json_data.getString("CurrencyId").equals(currencyFilter)) {
			        descr = "";
			        try {
			        	descr = json_data.getString("Description");
			        } catch (JSONException e) {
						e.printStackTrace();
					} 
			        
			        amount = json_data.getString("Amount");
			        amount = formatNumberNoFract(amount);
	//		        if (amount.endsWith(".0")) {
	//		        	amount = amount.replace(".0", "");
	//				}
			        currency = getCurrencySymbol(json_data.getString("CurrencyId"));
			        if (currency.equals("RUB")) {
			        	currency = "A";
			        } else {
			        	amount += " " + currency;
			        	currency = "";
			        }
			        if (hasSuspense.equals("HasSuspense")) {
			        	if (json_data.getString("HasSuspense").equals("true")) {
							imgArray.add(R.drawable.icon_progress);
							amountArray.add("+" + amount);
							currencyArray.add("+" + currency);
							numberArray.add(json_data.getString("InvoiceId"));
							dateArray.add(dateFormat(json_data.getString("CreateDate"),resources));
							stateArray.add(resources.getString(R.string.not_paid));
							descrArray.add(descr);
			        	}
			        } else {
				        numberArray.add(json_data.getString("InvoiceId"));
						dateArray.add(dateFormat(json_data.getString("CreateDate"), resources));
						descrArray.add(descr);
						if (json_data.getString("InvoiceStateId").equals("Accepted")) {
							imgArray.add(R.drawable.icon_ok);
							amountArray.add(amount);
							currencyArray.add(currency);
							stateArray.add(resources.getString(R.string.paid));
						} else {
							imgArray.add(R.drawable.icon_progress);
							amountArray.add("+" + amount);
							currencyArray.add("+" + currency);
							stateArray.add(resources.getString(R.string.not_paid));
						}
			        }
		        }
			}
			
	    	data = new ArrayList<Map<String, Object>>(numberArray.size());
		    for (int j = 0; j < numberArray.size(); j++) {
			      m = new HashMap<String, Object>();
			      m.put(MenuActivity.ATTRIBUTE_NAME_NUMBER, numberArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_DATE, dateArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_IMAGE, imgArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_AMOUNT, amountArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_RUBL, currencyArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_DESCR, descrArray.get(j));
			      m.put(MenuActivity.ATTRIBUTE_NAME_STATE, stateArray.get(j));
			      data.add(m);
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    return data;
    }
	
	public static ArrayList<String[]> template(String jsonText,Resources resources) {
    	JSONArray jArray = null;
    	ArrayList<String[]> dataArray = new ArrayList<String[]>();
    	int start, end, startNE;
    	String schedule = "";
    	
    	start = jsonText.indexOf("[");
		end = jsonText.indexOf("]") + 1;
		char[] buffer = new char[end - start];
		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray(new String(buffer));
			for (int i = 0; i < jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        String[] element = { "", "", "", "" };
		        element[0] = json_data.getString("Title");
		        element[1] = json_data.getString("ProviderLogoUrl");
		        element[2] = json_data.getString("TemplateId");
		        
		        try {
		        	schedule = json_data.getString("Schedule");
		        	if (!TextUtils.isEmpty(schedule)) {
		        		if (schedule.indexOf("NextExecution") > -1) {
			        		startNE = schedule.indexOf("NextExecution") + 21;
				        	char[] bufferNE = new char[5];
				        	schedule.getChars(startNE, startNE + 5, bufferNE, 0);
				        	element[3] = dateFormatTempl(new String(bufferNE), resources);
		        		}
					}
		        } catch (JSONException e) {
					e.printStackTrace();
				}
		        
		        dataArray.add(element);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    return dataArray;
    }
	
	public static String templateField(String text, String id) {
    	String result = "";
    	int start, end, lengthId;
    	
    	start = text.indexOf(id);
    	end = text.indexOf(",", start);
    	lengthId = id.length();
		char[] buffer = new char[end - start - lengthId - 4];
		text.getChars(start + lengthId + 3, end - 1, buffer, 0);
		result = new String(buffer);			
	    return result;
    }
	
	public static String templateTitle(String text, String id) {
    	String result = "";
    	int start, end, lengthId;
    	
    	start = text.indexOf(id);
    	end = text.indexOf(",", start);
    	lengthId = id.length();
		char[] buffer = new char[end - start - lengthId - 1];
		text.getChars(start + lengthId, end - 1, buffer, 0);
		result = new String(buffer);			
	    return result;
    }
	
	//на выходе готовая фраза расписания
	public static String templateSchedule(String text, Resources resources) {
    	String result = "";
    	String schedule = "";
    	String period = "";
    	String periodEntry = "";
    	String startDate = "";
    	String endDate = "";
    	String days = "";
    	JSONArray jArray = null;
    	JSONObject json_data;
    	
    	try {
			jArray = new JSONArray("[ \n" + text + "\n ]");
			json_data = jArray.getJSONObject(0);
			schedule = json_data.getString("Schedule");
			
			jArray = new JSONArray("[ \n" + schedule + "\n ]");
			json_data = jArray.getJSONObject(0);
			//однократно
			try {
				period = json_data.getString("Once");
				jArray = new JSONArray("[ \n" + period + "\n ]");
				json_data = jArray.getJSONObject(0);
				startDate = json_data.getString("StartDate");
				result = resources.getString(R.string.payment_one, dateDMY(startDate),
						dateHM(startDate));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//ежедневно
			try {
				period = json_data.getString("Daily");
				jArray = new JSONArray("[ \n" + period + "\n ]");
				json_data = jArray.getJSONObject(0);
				startDate = json_data.getString("StartDate");
				try {
					endDate = json_data.getString("EndDate");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (endDate.isEmpty()) {
					result = resources.getString(R.string.payment_begin,
							" " + resources.getString(R.string.daily) + ", ", dateHM(startDate),
							dateDMY(startDate));
				} else {
					result = resources.getString(R.string.payment_begin_end,
							" " + resources.getString(R.string.daily) + ", ", dateHM(startDate),
							dateDMY(startDate), dateDMY(endDate), dateHM(endDate));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//еженедельно
			try {
				period = json_data.getString("Weekly");
				jArray = new JSONArray("[ \n" + period + "\n ]");
				json_data = jArray.getJSONObject(0);
				startDate = json_data.getString("StartDate");
				try {
					endDate = json_data.getString("EndDate");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				periodEntry = json_data.getString("DaysOfWeek");
				jArray = new JSONArray("[ \n" + periodEntry + "\n ]");
				json_data = jArray.getJSONObject(0);
				if (json_data.getString("Monday").equals("true")) {
					days = resources.getString(R.string.monday);
				}
				if (json_data.getString("Tuesday").equals("true")) {
					if (days.length() > 0) {
						days += ", " + resources.getString(R.string.tuesday);
					} else {
						days = resources.getString(R.string.tuesday);
					}
				}
				if (json_data.getString("Wednesday").equals("true")) {
					if (days.length() > 0) {
						days += ", " + resources.getString(R.string.wednesday);
					} else {
						days = resources.getString(R.string.wednesday);
					}
				}
				if (json_data.getString("Thursday").equals("true")) {
					if (days.length() > 0) {
						days += ", " + resources.getString(R.string.thursday);
					} else {
						days = resources.getString(R.string.thursday);
					}
				}
				if (json_data.getString("Friday").equals("true")) {
					if (days.length() > 0) {
						days += ", " + resources.getString(R.string.friday);
					} else {
						days = resources.getString(R.string.friday);
					}
				}
				if (json_data.getString("Saturday").equals("true")) {
					if (days.length() > 0) {
						days += ", " + resources.getString(R.string.saturday);
					} else {
						days = resources.getString(R.string.saturday);
					}
				}
				if (json_data.getString("Sunday").equals("true")) {
					if (days.length() > 0) {
						days += ", " + resources.getString(R.string.sunday);
					} else {
						days = resources.getString(R.string.sunday);
					}
				}
				if (endDate.isEmpty()) {
					result = resources.getString(R.string.payment_begin,
							" " + resources.getString(R.string.weekly) + ", " +
							resources.getString(R.string.on_, days), dateHM(startDate),
							dateDMY(startDate));
				} else {
					result = resources.getString(R.string.payment_begin_end,
							" " + resources.getString(R.string.weekly) + ", " +
							resources.getString(R.string.on_, days), dateHM(startDate),
							dateDMY(startDate), dateDMY(endDate), dateHM(endDate));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//ежемесячно
			try {
				period = json_data.getString("MonthlyDayOfMonth");
				jArray = new JSONArray("[ \n" + period + "\n ]");
				json_data = jArray.getJSONObject(0);
				startDate = json_data.getString("StartDate");
				periodEntry = json_data.getString("DayOfMonth");
				try {
					endDate = json_data.getString("EndDate");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (periodEntry.equals("1")) { //первый день
					if (endDate.isEmpty()) {
						result = resources.getString(R.string.payment_begin,
								" " + resources.getString(R.string.monthly) + " " +
								resources.getString(R.string.first_day), dateHM(startDate),
								dateDMY(startDate));
					} else {
						result = resources.getString(R.string.payment_begin_end,
								" " + resources.getString(R.string.monthly) + " " +
								resources.getString(R.string.first_day), dateHM(startDate),
								dateDMY(startDate), dateDMY(endDate), dateHM(endDate));
					}
				} else if (periodEntry.equals("32")) { //последний день
					if (endDate.isEmpty()) {
						result = resources.getString(R.string.payment_begin,
								" " + resources.getString(R.string.monthly) + " " +
								resources.getString(R.string.last_day), dateHM(startDate),
								dateDMY(startDate));
					} else {
						result = resources.getString(R.string.payment_begin_end,
								" " + resources.getString(R.string.monthly) + " " +
								resources.getString(R.string.last_day), dateHM(startDate),
								dateDMY(startDate), dateDMY(endDate), dateHM(endDate));
					}
				} else { //остальные
					if (endDate.isEmpty()) {
						result = resources.getString(R.string.payment_begin,
								" " + resources.getString(R.string.monthly) + " " +
								resources.getString(R.string.n_day, periodEntry), dateHM(startDate),
								dateDMY(startDate));
					} else {
						result = resources.getString(R.string.payment_begin_end,
								" " + resources.getString(R.string.monthly) + " " +
								resources.getString(R.string.n_day, periodEntry), dateHM(startDate),
								dateDMY(startDate), dateDMY(endDate), dateHM(endDate));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    return result;
    }
	
	public static String[] templateProvider(String text) {
    	String[] result = { "", "", "", "", "", "", "" };
    	String commission = "";
    	JSONArray jArray = null;
    	JSONObject json_data;
    	
    	try {
			//Log.d("1", line);
			jArray = new JSONArray("[ \n" + text + "\n ]");
			json_data = jArray.getJSONObject(0);
			result[0] = json_data.getString("MinAmount");
			result[1] = json_data.getString("MaxAmount");
			commission = json_data.getString("Commission");
			
			jArray = new JSONArray("[ \n" + commission + "\n ]");
			json_data = jArray.getJSONObject(0);
			result[2] = json_data.getString("Cost");
			result[3] = json_data.getString("Max");
			result[4] = json_data.getString("BonusRate");
			result[5] = json_data.getString("Min");
			result[6] = json_data.getString("Rate");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
    }
	
	public static ArrayList<String[]> templateId(String jsonText) {
    	JSONArray jArray = null;
    	ArrayList<String[]> dataArray = new ArrayList<String[]>();
    	int start, end;
    	
    	start = jsonText.indexOf("[");
		end = jsonText.indexOf("]") + 1;
		char[] buffer = new char[end - start];
		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray(new String(buffer));
			for (int i = 0; i < jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        String[] element = { "", "", "" };
		        element[0] = json_data.getString("FieldId");
		        element[1] = json_data.getString("FieldTitle");
		        element[2] = json_data.getString("FieldValue");
		        dataArray.add(element);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    return dataArray;
    }
	
	public static String[] session(String line) {
		String result[] = { "", "", "" };
		JSONArray jArray;
		try {
			//Log.d("1", line);
			jArray = new JSONArray("[ \n" + line + "\n ]");
			JSONObject json_data = jArray.getJSONObject(0);
			result[0] = json_data.getString("UserId");
			result[1] = json_data.getString("Token");
			result[2] = json_data.getString("Timeout");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String paymentsState(String line) {
		String result = "";
		JSONArray jArray;
		try {
			//Log.d("1", line);
			jArray = new JSONArray("[ \n" + line + "\n ]");
			JSONObject json_data = jArray.getJSONObject(0);
			result = json_data.getString("StateId");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String[] captcha(String line) {
		String result[] = { "", "" };
		JSONArray jArray;
		try {
			//Log.d("1", line);
			jArray = new JSONArray("[ \n" + line + "\n ]");
			JSONObject json_data = jArray.getJSONObject(0);
			result[0] = json_data.getString("CaptchaId");
			result[1] = json_data.getString("CaptchaUrl");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String[] profile(String jsonText) {
		String result[] = { "", "", "", "" };
		String typeId;
		//int start, end;
		JSONArray jArray;
		
//		start = jsonText.indexOf("[");
//		end = jsonText.indexOf("]") + 1;
//		char[] buffer = new char[end - start];
//		jsonText.getChars(start, end, buffer, 0);
		try {
			jArray = new JSONArray("[ \n" + jsonText + "\n ]");
			JSONObject json_data = jArray.getJSONObject(0);
			result[3] = json_data.getString("AccountTypeId");
			
			jArray = new JSONArray(json_data.getString("UserAttributes"));
			for (int i=0; i<jArray.length(); i++) {
				json_data = jArray.getJSONObject(i);
				typeId = json_data.getString("UserAttributeTypeId");
				if (typeId.equals("Title")) {
					result[0] = json_data.getString("DisplayValue");
				} else if (typeId.equals("MerchantUrl")) {
					result[1] = json_data.getString("DisplayValue");
				} else if (typeId.equals("MerchantLogo")) {
					result[2] = json_data.getString("DisplayValue");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Log.d("1", "Profile " + result[0] + result[1] + result[2]);
		return result;
	}
	
	public static ArrayList<String[]> balance2(String jsonText) {
		String currencyId;
		JSONArray jArray;
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		try {
			jArray = new JSONArray(jsonText);
			for (int i=0; i<jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        currencyId = json_data.getString("CurrencyId");
		        String[] line = { "", "", "", "", "" };
		        line[0] = currencyId;
		        line[2] = formatNumber(getIntegerPart(json_data.getString("Amount")));
				line[3] = formatNumber(getIntegerPart(json_data.getString("HoldAmount")));
				line[4] = json_data.getString("IsNative");
				if (currencyId.equals("398")) { //казах
					line[1] = "₸";//KZTU+20B8&#8376
				} else if (currencyId.equals("643")) {//рубль
					line[1] = "RUB";//RUBU+20BD&#8381
				} else if (currencyId.equals("710")) {//южноафр
					line[1] = "R";//ZARR
				} else if (currencyId.equals("840")) {//USD
					line[1] = "$";//USD
				} else if (currencyId.equals("972")) {//таджик
					line[1] = "смн.";//TJSсмн.
				} else if (currencyId.equals("974")) {//белорус
					line[1] = "Br";//BrBYR
				} else if (currencyId.equals("978")) {//EUR
					line[1] = "€";//EURU+20AC&#8364
				} else if (currencyId.equals("980")) {//укр
					line[1] = "₴";//UAHU+20B4&#8372
				} else if (currencyId.equals("985")) {//польск
					line[1] = "zł";//PLN
				} else {//?
					line[1] = "?";
				}
				result.add(line);
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Log.d("1", "Balance " + result[0] + result[1] + result[2]);
	    return result;
	}
	
	public static ArrayList<String[]> _balanceTest(String jsonText) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		
			for (int i=0; i < 9; i++) {
		        String[] line = { "", "", "", "", "" };
		        
		        line[2] = formatNumber(getIntegerPart("12345"));
				line[3] = formatNumber(getIntegerPart("0"));
				line[4] = "false";
				if (i == 0) { //казах
					line[0] = "398";
					line[1] = "₸";//KZTU+20B8&#8376
				} else if (i == 1) {//рубль
					line[0] = "643";
					line[1] = "RUB";//RUBU+20BD&#8381
					line[4] = "true";
				} else if (i == 2) {//южноафр
					line[0] = "710";
					line[1] = "R";//ZARR
				} else if (i == 3) {//USD
					line[0] = "840";
					line[1] = "$";//USD
				} else if (i == 4) {//таджик
					line[0] = "972";
					line[1] = "смн.";//TJSсмн.
				} else if (i == 5) {//белорус
					line[0] = "974";
					line[1] = "Br";//BrBYR
				} else if (i == 6) {//EUR
					line[0] = "978";
					line[1] = "€";//EURU+20AC&#8364
				} else if (i == 7) {//укр
					line[0] = "980";
					line[1] = "₴";//UAHU+20B4&#8372
				} else if (i == 8) {//польск
					line[0] = "985";
					line[1] = "zł";//PLN
				} 
				result.add(line);
		    }
		//Log.d("1", "Balance " + result[0] + result[1] + result[2]);
	    return result;
	}
	
	/*public static String[] _balance(String jsonText) {
		String[] result = { "", "", "", "", "", "" };
		String currencyId;
		JSONArray jArray;
		try {
			jArray = new JSONArray(jsonText);
			for (int i=0; i<jArray.length(); i++) {
		        JSONObject json_data = jArray.getJSONObject(i);
		        currencyId = json_data.getString("CurrencyId");
				if (currencyId.equals("643")) { //рубль
					result[0] = formatNumber(getIntegerPart(json_data.getString("Amount")));
					result[3] = formatNumber(getIntegerPart(json_data.getString("HoldAmount")));
				} else if (currencyId.equals("840")) {//USD
					result[1] = formatNumber(getIntegerPart(json_data.getString("Amount")));
					result[4] = formatNumber(getIntegerPart(json_data.getString("HoldAmount")));
				} else if (currencyId.equals("978")) {//EUR
					result[2] = formatNumber(getIntegerPart(json_data.getString("Amount")));
					result[5] = formatNumber(getIntegerPart(json_data.getString("HoldAmount")));
				}
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Log.d("1", "Balance " + result[0] + result[1] + result[2]);
	    return result;
	}*/
	
	public static String getIntegerPart(String in) {
		float inF = Float.parseFloat(in);
		return Math.round(inF) + "";
	}
	
	//форматирование чисел
	public static String formatNumber(String in) {
		DecimalFormat myFormatter = new DecimalFormat("##,###,###.##");
		String split = myFormatter.format(Float.parseFloat(in));
		split = split.replace(",", ".");
		/*String split = "";
		String start = "";
		String end = "";
		//Log.d("1", "in " + in);
		int inLength, position;
		inLength = in.length();
		if (in.indexOf(".") == -1) {
			if (inLength < 4) {
				split = in;
			} else {
				end = in.substring(inLength - 3); 
				start = in.substring(0, inLength - 3);
				split = start + " " + end;
			}
		} else {
			if (inLength < 6) {
				split = in;
			} else {
				position = in.indexOf(".");
				end = in.substring(position - 3);
				start = in.substring(0, position - 3);
				split = start + " " + end;
			}
		}*/
		return split;
	}
	
	//форматирование чисел
		public static String formatNumberNoFract(String in) {
			DecimalFormat myFormatter = new DecimalFormat("##,###,###");
			String split = myFormatter.format(Float.parseFloat(in));
			split = split.replace(",", ".");
			return split;
		}
	
	//форматирование дат для списков
	static String dateFormat(String in, Resources resources) {
		//DatatypeFactory datatypeFactory;
		//XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar("1994-08-10T00:00:00Z");
		String dateOut = "";
		String year, month, day, hour, minute, second;
		long diff = 0; 
		int offSet = 0;
		int diffMin = 0;
		int diffHour = 0;
		
		year = in.substring(0, 4);
		month = in.substring(5, 7);
		day = in.substring(8, 10);
		hour = in.substring(11, 13);
		minute = in.substring(14, 16);
		second = in.substring(17, 19);
		
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 
				Integer.parseInt(day), Integer.parseInt(hour),
				Integer.parseInt(minute), Integer.parseInt(second));
		Date currentDate = new Date();
		offSet = TimeZone.getDefault().getRawOffset() / 3600000;
		
		diff = (currentDate.getTime() - calendar.getTime().getTime()) / 1000;
		diffMin = (int) diff / 60;
		diffHour = (int) diff / 3600;
        
		if (diff < 120) {
			dateOut = resources.getString(R.string.moment_ago);
		} else if (diff < 3600) {
			if ((diffMin == 11) | (diffMin == 12) | (diffMin == 13) | (diffMin == 14)) {
				dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago);
			} else if ((diffMin + "").endsWith("1")) {
				dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago2);
			} else if ((diffMin + "").endsWith("2") | (diffMin + "").endsWith("3") | 
					(diffMin + "").endsWith("4")) {
				dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago3);
			} else {
				dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago);
			}
  		} else if (diff < 7200) {
  			dateOut = resources.getString(R.string.hour_ago);
  		} else if (diff < 18000) {
  			dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago1);
  		} else if (diff < 75600) {
  			dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago2);
  		} else if ((diff > 75599) & (diff < 79200)) {
  			dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago3);
  		} else if (diff < 86400) {
  			dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago1);
  		} else {
			calendar.add(Calendar.HOUR_OF_DAY, offSet);
			minute = calendar.get(Calendar.MINUTE) + "";
			if (minute.length() == 1) {
				minute = "0" + minute;
			}
			dateOut = calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    resources.
					getStringArray(R.array.month_array)[calendar.get(Calendar.MONTH)] +
					", " + calendar.get(Calendar.HOUR_OF_DAY) + ":" +
					minute;
		}
        return dateOut;
	}
	
	// дата dd/MM/yyyy
	static String dateDMY(String in) {
			String dateOut = "";
			String year, month, day, hour, minute, second;
			
			year = in.substring(0, 4);
			month = in.substring(5, 7);
			day = in.substring(8, 10);
			hour = in.substring(11, 13);
			minute = in.substring(14, 16);
			second = in.substring(17, 19);
			
			Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 
					Integer.parseInt(day), Integer.parseInt(hour),
					Integer.parseInt(minute), Integer.parseInt(second));
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy"); 
			dateOut = formatter.format(calendar.getTime());				
			return dateOut;
	}
		
	// дата 
	static String dateHM(String in) {
					String dateOut = "";
					String year, month, day, hour, minute, second;
					
					year = in.substring(0, 4);
					month = in.substring(5, 7);
					day = in.substring(8, 10);
					hour = in.substring(11, 13);
					minute = in.substring(14, 16);
					second = in.substring(17, 19);
					
					Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
					calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 
							Integer.parseInt(day), Integer.parseInt(hour),
							Integer.parseInt(minute), Integer.parseInt(second));
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
					dateOut = formatter.format(calendar.getTime());				
					return dateOut;
	}
	
	    //форматирование дат для списка шаблонов
		static String dateFormatTempl(String in, Resources resources) {
			String dateOut = "";
			String month, day;
			
			month = in.substring(0, 2);
			day = in.substring(3, 5);
			
			dateOut = day + " " + resources.
					getStringArray(R.array.month_array_cut)[Integer.parseInt(month) - 1];
	        return dateOut;
		}
		
		public static void appendLog(String text)
		{       
		   File logFile = new File("sdcard/log.file");
		   if (!logFile.exists())
		   {
		      try
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         e.printStackTrace();
		      }
		   }
		   try
		   {
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		      buf.append(text);
		      buf.newLine();
		      buf.close();
		   }
		   catch (IOException e)
		   {
		      e.printStackTrace();
		   }
		}
		
		public static String getCurrencySymbol(String currencyId) {
			String result = "";
			if (currencyId.equals("398")) { //казах
				result = "₸";//KZTU+20B8&#8376
			} else if (currencyId.equals("643")) {//рубль
				result = "RUB";//RUBU+20BD&#8381
			} else if (currencyId.equals("710")) {//южноафр
				result = "R";//ZARR
			} else if (currencyId.equals("840")) {//USD
				result = "$";//USD
			} else if (currencyId.equals("972")) {//таджик
				result = "смн.";//TJSсмн.
			} else if (currencyId.equals("974")) {//белорус
				result = "Br";//BrBYR
			} else if (currencyId.equals("978")) {//EUR
				result = "€";//EURU+20AC&#8364
			} else if (currencyId.equals("980")) {//укр
				result = "₴";//UAHU+20B4&#8372
			} else if (currencyId.equals("985")) {//польск
				result = "zł";//PLN
			} else {//?
				result = "?";
			}
			return result;
		}
}
