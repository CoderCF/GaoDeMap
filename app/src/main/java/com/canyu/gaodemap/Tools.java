/** 
 * @fileName:Tools.java 
 * @date:2016年7月20日 
 * @author:ChengFu
 * @Copyright:
 */
package com.canyu.gaodemap;

import java.math.BigDecimal;

public class Tools {
	/** 
	 * 根据两点间经纬度坐标（double值），简单计算两点间距离， 
	 * @param lat1 
	 * @param lng1 
	 * @param lat2 
	 * @param lng2 
	 * @return 距离：单位为千米 
	 */ 
	public static double getDistance(double longitude1, double latitude1,
			double longitude2, double latitude2) {
		//地球半径，单位为米
		//double EARTH_RADIUS = 6378137.0;
		//地球半径，单位为千米
		double EARTH_RADIUS = 6378.137;

		double Lat1 = rad(latitude1);
		double Lat2 = rad(latitude2);
		double a = Lat1 - Lat2;
		double b = rad(longitude1) - rad(longitude2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(Lat1) * Math.cos(Lat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 *根据两点间经纬度坐标（double值），精确计算两点间距离， 
	 * @param lat1 
	 * @param lon1
	 * @param lat2 
	 * @param lon2
	 * @return 距离：单位为千米 
	 */
	public static double computeDistance(double lat1, double lon1,
			double lat2, double lon2) {
		// Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
		// using the "Inverse Formula" (section 4)

		int MAXITERS = 20;
		// Convert lat/long to radians
		lat1 *= Math.PI / 180.0;
		lat2 *= Math.PI / 180.0;
		lon1 *= Math.PI / 180.0;
		lon2 *= Math.PI / 180.0;

		double a = 6378137.0; // WGS84 major axis
		double b = 6356752.3142; // WGS84 semi-major axis
		double f = (a - b) / a;
		double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

		double L = lon2 - lon1;
		double A = 0.0;
		double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
		double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

		double cosU1 = Math.cos(U1);
		double cosU2 = Math.cos(U2);
		double sinU1 = Math.sin(U1);
		double sinU2 = Math.sin(U2);
		double cosU1cosU2 = cosU1 * cosU2;
		double sinU1sinU2 = sinU1 * sinU2;

		double sigma = 0.0;
		double deltaSigma = 0.0;
		double cosSqAlpha = 0.0;
		double cos2SM = 0.0;
		double cosSigma = 0.0;
		double sinSigma = 0.0;
		double cosLambda = 0.0;
		double sinLambda = 0.0;

		double lambda = L; // initial guess
		for (int iter = 0; iter < MAXITERS; iter++) {
			double lambdaOrig = lambda;
			cosLambda = Math.cos(lambda);
			sinLambda = Math.sin(lambda);
			double t1 = cosU2 * sinLambda;
			double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
			double sinSqSigma = t1 * t1 + t2 * t2; // (14)
			sinSigma = Math.sqrt(sinSqSigma);
			cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
			sigma = Math.atan2(sinSigma, cosSigma); // (16)
			double sinAlpha = (sinSigma == 0) ? 0.0 :
				cosU1cosU2 * sinLambda / sinSigma; // (17)
			cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
			cos2SM = (cosSqAlpha == 0) ? 0.0 :
				cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

			double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
			A = 1 + (uSquared / 16384.0) * // (3)
					(4096.0 + uSquared *
							(-768 + uSquared * (320.0 - 175.0 * uSquared)));
			double B = (uSquared / 1024.0) * // (4)
					(256.0 + uSquared *
							(-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
			double C = (f / 16.0) *
					cosSqAlpha *
					(4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
			double cos2SMSq = cos2SM * cos2SM;
			deltaSigma = B * sinSigma * // (6)
					(cos2SM + (B / 4.0) *
							(cosSigma * (-1.0 + 2.0 * cos2SMSq) -
									(B / 6.0) * cos2SM *
									(-3.0 + 4.0 * sinSigma * sinSigma) *
									(-3.0 + 4.0 * cos2SMSq)));

			lambda = L +
					(1.0 - C) * f * sinAlpha *
					(sigma + C * sinSigma *
							(cos2SM + C * cosSigma *
									(-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

			double delta = (lambda - lambdaOrig) / lambda;
			if (Math.abs(delta) < 1.0e-12) {
				break;
			}
		}

		double s = b * A * (sigma - deltaSigma);
		
		//return  get2decimal(s/1000.0);
		return  s;
	}

	/**
	 * 保留两位小数点
	 * @param number
	 * @return
	 */
	public static double get2decimal(double number){
		BigDecimal bd = new BigDecimal(number);
		bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP); 
		
		return bd.doubleValue();
	}
	
}
