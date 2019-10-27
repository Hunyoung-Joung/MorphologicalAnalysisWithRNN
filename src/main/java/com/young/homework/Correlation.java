package com.young.homework;


import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class Correlation {

	public static void main(String[] args) {

		// Variable
		double n = 29;
		// deviation x, deviation y, average avg
		double x = 0, y = 0, cov = 1.079;
		// Avg ( deviation x X deviation x)
		cov = (x*y)/n;
		double i = cov*n;
	}
	
}

/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
class Correlation_ {
    /**
     * 변동계수 : 표준편차 / 평균
     * @param array 값
     * @param option  1 : 표본의 표준 편차 / 0 : 모집단 전체의 표준 편차
     * @return
     */
    public static BigDecimal coeOfVar(BigDecimal[] array, BigDecimal option){
        BigDecimal stdvData = getStdv(array, option);
        BigDecimal avgData = getAvg(array);
       
        BigDecimal coeVar = stdvData.divide(avgData, MathContext.DECIMAL64);
       
        return coeVar;
    }
   
    /**
     * 상관계수  = sum(((기준단위값 - 기준단위값의 평균)*
     *                 (비교단위값 - 비교단위값의 평균))/(갯수 - option))/
     *           (기준값의 표준편차 * 비교단위값의 표준편차)
     * @param array
     * @return
     */
    public static BigDecimal getCor(BigDecimal[][] array){
      
        BigDecimal[] realAmt = array[0];
        BigDecimal[] itmVal  = array[1];
      
        BigDecimal avgRealAmt = getAvg(realAmt);
        BigDecimal avgitmVal  = getAvg(itmVal);
       
        int n = 0;
        for (n = 0; n < realAmt.length; n++) {
            if(array[n]==null){
                break;
            }
        }
        BigDecimal dataSizeN = BigDecimal.valueOf(n-1);
       
        BigDecimal stdvRealAmt = getStdv(realAmt, BigDecimal.ONE);
        BigDecimal stdvitmVal  = getStdv(itmVal, BigDecimal.ONE);
       
        BigDecimal numerator = BigDecimal.ZERO;
       
        for (int i = 0; i < n; i++) {
            BigDecimal a = realAmt[i].subtract(avgRealAmt);
            BigDecimal b = itmVal[i].subtract(avgitmVal);
            BigDecimal mulAB = a.multiply(b);
            BigDecimal divAB = mulAB.divide(dataSizeN, MathContext.DECIMAL64);
           
            numerator = numerator.add(divAB);
        }
        BigDecimal denominator = stdvRealAmt.multiply(stdvitmVal);
       
        return numerator.divide(denominator, MathContext.DECIMAL64);
       
    }
   
    /**
     * 표준편차 : 분산의 제곱근
     * @param array 값
     * @param option  1 : 표본의 표준 편차 / 0 : 모집단 전체의 표준 편차
     * @return
     */
    public static BigDecimal getStdv(BigDecimal[] array, BigDecimal option){
       
        BigDecimal varData = getVar(array, option);
        double stdvData = Math.sqrt(varData.doubleValue()); // 표준편차

        return BigDecimal.valueOf(stdvData);
    }
   
    /**
     * 분산 = sum(단위값 - 단위값의 평균) / 갯수 - option
     * @param array 값
     * @param option  1 : 표본의 표준 편차 / 0 : 모집단 전체의 표준 편차
     * @return
     */
    public static BigDecimal getVar(BigDecimal[] array, BigDecimal option){
        BigDecimal varData = BigDecimal.ZERO;
        BigDecimal sumData = BigDecimal.ZERO;
        //입력데이타의 갯수
        int dataSize = array.length;
        BigDecimal avgData = getAvg(array);
       
        if (dataSize > 2) {
            int i = 0;
            for (i = 0; i < dataSize; i++) {
                if(array[i]==null){
                    break;
                }
              BigDecimal diff = array[i].subtract(avgData); //단위값  - 평균
              sumData = sumData.add(diff.multiply(diff)); //제곱
            }
            BigDecimal dataSizeN = BigDecimal.valueOf(i).subtract(option);
            varData = sumData.divide(dataSizeN, MathContext.DECIMAL64);
        }
        return varData;
    }
   
    /**
     * 산술평균
     * @param array 값
     * @return
     */
    public static BigDecimal getAvg(BigDecimal[] array) {
        BigDecimal avgData = BigDecimal.ZERO;
        BigDecimal sumData = BigDecimal.ZERO;
        int i = 0;
        for (i = 0; i < array.length; i++){
            if(array[i]== null){
                break;
            }
            sumData = sumData.add(array[i]);
        }
        avgData = sumData.divide(BigDecimal.valueOf(i), MathContext.DECIMAL64);
        return avgData;
      }
   
    /**
     * 합계
     * @param array 값
     * @return
     */
    public static BigDecimal getSum(BigDecimal[] array) {
        BigDecimal sumData = BigDecimal.ZERO;
        int i = 0;
        for (i = 0; i < array.length; i++){
            if(array[i]== null){
                break;
            }
            sumData = sumData.add(array[i]);
        }
        return sumData;
      }

}