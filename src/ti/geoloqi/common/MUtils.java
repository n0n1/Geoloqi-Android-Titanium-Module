package ti.geoloqi.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

/**
 * This is a utility class that contains common functions used by module classes
 */
final public class MUtils {
	public static final String LCAT = MUtils.class.getSimpleName();
	private static final String ATTRIB_ERROR_CODE = "error_code";
	private static final String ATTRIB_ERROR_DESC = "error_description";
	private static final String ATTRIB_RESPONSE = "response";
	private static final String ATTRIB_HEADERS = "headers";
	private static final String ATTRIB_STATUS = "status";

	private static final String JSON_ATT_PROVIDER = "provider";
	private static final String JSON_ATT_PROVIDER_NAME = "name";
	private static final String JSON_ATT_COORDINATES = "coords";
	private static final String JSON_ATT_COORDINATES_ACCURACY = "accuracy";
	private static final String JSON_ATT_COORDINATES_ALTITUDE = "altitude";
	private static final String JSON_ATT_COORDINATES_LATITUDE = "latitude";
	private static final String JSON_ATT_COORDINATES_LONGITUDE = "longitude";
	private static final String JSON_ATT_COORDINATES_SPEED = "speed";

	/**
	 * Private Class Constructor
	 */
	private MUtils() {}

	/**
	 * HttpResponse to KrollDict Converter with default format
	 * 
	 * @param response
	 *            HttpResponse
	 * @return KrollDict
	 */
	public static KrollDict processHttpResponse(HttpResponse response) {
		return processHttpResponse(response);
	}

	/**
	 * HttpResponse to KrollDict Converter
	 * 
	 * @param json
	 *            JSONObject
	 * @param headers
	 *            Header[]
	 * @param status
	 *            StatusLine; null-OK;
	 * @return KrollDict
	 */
	public static KrollDict processHttpResponse(JSONObject json,
			Header[] headers, StatusLine status) {
		KrollDict kd = new KrollDict();
		try {
			// Get headers
			KrollDict headersDict = new KrollDict();
			for (Header h : headers) {
				headersDict.put(h.getName(), h.getValue());
			}
			
			// Include status code
			if (status != null) {
				kd.put(ATTRIB_STATUS, status.getStatusCode());
			} else {
				kd.put(ATTRIB_STATUS, 200);
			}
			
			// Include response headers
			kd.put(ATTRIB_HEADERS, headersDict);
			
			// Include payload
			kd.put(ATTRIB_RESPONSE, new KrollDict(json));
		} catch (JSONException je) {
			MLog.e(LCAT, "JSONException: " + je.toString());
			je.printStackTrace();
		} catch (IllegalStateException ise) {
			MLog.e(LCAT, "IllegalStateException: " + ise.toString());
			ise.printStackTrace();
		} catch (Exception e) {
			MLog.e(LCAT, "Exception: " + e.toString());
			e.printStackTrace();
		}
		return kd;
	}

	/**
	 * Converts json location object to android location object
	 * 
	 * @param location
	 *            JSON Object
	 */
	public static Location parseLocation(Object location) {
		Location loc = null;
		try {
			JSONObject jsonObj = new JSONObject(String.valueOf(location));

			MLog.d(LCAT, "jsonObj=" + jsonObj);

			JSONObject coords = null;
			JSONObject provider = null;
			if (jsonObj != null && jsonObj.has(JSON_ATT_COORDINATES)) {
				coords = jsonObj.getJSONObject(JSON_ATT_COORDINATES);
			}

			if (jsonObj != null && jsonObj.has(JSON_ATT_PROVIDER)) {
				provider = jsonObj.getJSONObject(JSON_ATT_PROVIDER);
			}

			MLog.d(LCAT, "coords=" + coords);
			MLog.d(LCAT, "provider=" + provider);

			String prov = "";
			float acc = 0, speed = 0;
			double lat = 0, longt = 0, alt = 0;

			if (provider != null && provider.has(JSON_ATT_PROVIDER_NAME)) {
				prov = String.valueOf(provider.get(JSON_ATT_PROVIDER_NAME));
			}
			if (coords != null) {
				if (coords.has(JSON_ATT_COORDINATES_ACCURACY)) {
					acc = Float.parseFloat(String.valueOf(coords.get(JSON_ATT_COORDINATES_ACCURACY)));
				}
				if (coords.has(JSON_ATT_COORDINATES_ALTITUDE)) {
					alt = Double.parseDouble(String.valueOf(coords.get(JSON_ATT_COORDINATES_ALTITUDE)));
				}
				if (coords.has(JSON_ATT_COORDINATES_LATITUDE)) {
					lat = Double.parseDouble(String.valueOf(coords.get(JSON_ATT_COORDINATES_LATITUDE)));
				}
				if (coords.has(JSON_ATT_COORDINATES_LONGITUDE)) {
					longt = Double.parseDouble(String.valueOf(coords.get(JSON_ATT_COORDINATES_LONGITUDE)));
				}
				if (coords.has(JSON_ATT_COORDINATES_SPEED)) {
					speed = Float.parseFloat(String.valueOf(coords.get(JSON_ATT_COORDINATES_SPEED)));
				}
			}
			if (!(provider == null && coords == null)) {
				loc = new Location(prov);
				loc.setAccuracy(acc);
				loc.setAltitude(alt);
				loc.setLatitude(lat);
				loc.setLongitude(longt);
				loc.setSpeed(speed);

				MLog.d(LCAT, "Location created=" + loc);
			} else {
				MLog.e(LCAT, "Blank location provided");
			}
		} catch (JSONException je) {
			MLog.e(LCAT, "JSONException occured, unable to create Location");
			je.printStackTrace();
		} catch (Exception e) {
			MLog.e(LCAT, "Exception occured, unable to create Location");
			e.printStackTrace();
		}

		return loc;
	}

	/**
	 * Android location to KrollDict convertor
	 * 
	 * @param loc
	 *            Android location
	 * @return KrollDict
	 */
	public static KrollDict locationToDictionary(Location loc) {
		KrollDict kd = new KrollDict(2);
		KrollDict kdCoord = new KrollDict(5);
		KrollDict kdProvider = new KrollDict(1);

		if (loc != null) {
			kdProvider.put(JSON_ATT_PROVIDER_NAME, loc.getProvider());
			kdCoord.put(JSON_ATT_COORDINATES_ACCURACY, loc.getAccuracy());
			kdCoord.put(JSON_ATT_COORDINATES_ALTITUDE, loc.getAltitude());
			kdCoord.put(JSON_ATT_COORDINATES_LATITUDE, loc.getLatitude());
			kdCoord.put(JSON_ATT_COORDINATES_LONGITUDE, loc.getLongitude());
			kdCoord.put(JSON_ATT_COORDINATES_SPEED, loc.getSpeed());
			kd.put(JSON_ATT_COORDINATES, kdCoord);
			kd.put(JSON_ATT_PROVIDER, kdProvider);
		}
		return kd;
	}

	/**
	 * This method converts HashMap to JSON Object
	 * 
	 * @param object
	 *            Hashmap object
	 * @return JSONObject
	 */
	public static JSONObject convertToJSON(Object object) {
		JSONObject json = null;
		try {
			json = TiConvert.toJSON((HashMap<String, Object>) object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * This method converts an JSON Object array to JSON array
	 * 
	 * @param object
	 *            An array of JSON objects
	 * @return JSONArray
	 */
	public static JSONArray convertToJSONArray(Object object) {
		JSONArray json = null;
		try {
			if (object instanceof Object[]) {
				json = TiConvert.toJSONArray((Object[]) object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * This method generates error object to be sent to js when firing
	 * onValidate event
	 * 
	 * @param code
	 *            Error code to be sent
	 * @param description
	 *            Error Description to be sent
	 * @return Error object wrapped as KrollDict object
	 */
	public static KrollDict generateErrorObject(String code, String description) {
		KrollDict kd = new KrollDict(2);
		kd.put(ATTRIB_ERROR_CODE, code);
		kd.put(ATTRIB_ERROR_DESC, description);
		return kd;
	}

	/**
	 * This method checks if the callback object provided is of type
	 * HashMap<String,KrollFunction> or not
	 * 
	 * @param callback
	 *            Object passed by developer
	 * @return true if valid object is received else false
	 */
	public static boolean checkCallbackObject(Object callback) {
		boolean status = false;
		if (callback != null && callback instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) callback;
			Iterator<?> itcallback = map.keySet().iterator();
			Object key = null, value = null;
			int counter = map.size();
			if (map.size() == counter) {
				while (itcallback.hasNext()) {
					key = itcallback.next();
					value = map.get(key);
					if (value instanceof KrollFunction) {
						counter--;
					}
				}
				if (counter == 0) {
					status = true;
				}
			}
		}

		return status;
	}
}
