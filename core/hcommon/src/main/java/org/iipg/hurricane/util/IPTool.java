/**
 * 
 */
package org.iipg.hurricane.util;

import java.math.BigInteger;

/**
 * @author lixiaojing
 *
 */
public class IPTool {

    public static Long IPv4ToBigInt(String ipv4) {
    	byte[] bs = ipv4ToBytes(ipv4);
    	BigInteger bi = null;
    	if(bs == null) bi = BigInteger.ZERO;
    	else bi = new BigInteger(bs);
    	
    	Long l = bi.longValue();
    	return l;
    }

    public static BigInteger[] IPv6ToBigInt(String ipv6) {
    	BigInteger[] bigs = new BigInteger[2];
    	byte[] bytes = ipv6ToBytes(ipv6);
    	byte[] fore = new byte[9];
    	byte[] back = new byte[9];
    	back[0] = 0;
    	System.arraycopy(bytes, 0, fore, 0, 9);
    	System.arraycopy(bytes, 9, back, 1, 8);    	
    	bigs[0] = new BigInteger(fore);
    	bigs[1] = new BigInteger(back);
        return bigs;
    }

    public static String BigIntToIPv4(Long l){
    	BigInteger bi = BigInteger.valueOf(l);
    	byte[] bytes = bi.toByteArray();
    	int index = bytes.length-1;
    	byte[] tmp = new byte[2];
    	tmp[0] = 0;
    	tmp[1] = bytes[index];
    	BigInteger tmpInt = new BigInteger(tmp);    	
    	String ret = tmpInt.toString();
    	for(int i=1; i<4; i++){
    		if(index>=i){
            	tmp[1] = bytes[index-i];    			
    		}else{
            	tmp[1] = 0;
    		}
    		ret = new BigInteger(tmp) + "." + ret;
    	}
    	return ret;
    }

    public static String BigIntToIPv6(BigInteger[] bis){
    	byte[] bytes0 = bis[0].toByteArray();
    	byte[] bytes1 = bis[1].toByteArray();
    	int index = bytes1.length -1;
    	byte[] tmp = new byte[2];
    	tmp[0] = bytes1[index-1];
    	tmp[1] = bytes1[index];
    	String ret = encodeHex(tmp);
    	
    	for(int i=1; i<4; i++){
    		if(index>=i*2){
    			tmp[1] = bytes1[index-i*2];
    			if(index>=(i*2+1)){
            		tmp[0] = bytes1[index-i*2-1];    				
    			}else{
    				tmp[0] = 0;
    			}
    		}
    		else{
    			tmp[1] = 0;
    			tmp[0] = 0;
    		}
    		
    		ret = encodeHex(tmp) + ":" + ret;
    	}

    	index = bytes0.length -1;
    	for(int i=0; i<4; i++){
    		if(index>=i*2){
    			tmp[1] = bytes0[index-i*2];
    			if(index>=(i*2+1)){
            		tmp[0] = bytes0[index-i*2-1];    				
    			}else{
    				tmp[0] = 0;
    			}
    		}
    		else{
    			tmp[1] = 0;
    			tmp[0] = 0;
    		}
    		ret = encodeHex(tmp) + ":" + ret;
    	}
    	
    	ret = ret.replaceAll("(0000:)+", ":");
    	ret = ret.replaceAll(":0+", ":");
    	return ret;
    }

	private static String encodeHex(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2){
				sb.append(0);
			}

			sb.append(sTemp.toUpperCase());
		}		
		return sb.toString();
	}

    /**
     * ipv4地址转有符号byte[5]
     * @param ipv4 字符串的IPV4地址
     * @return big integer number
     */
    private static byte[] ipv4ToBytes(String ipv4) {
        byte[] ret = new byte[5];
        ret[0] = 0;
        // 先找到IP地址字符串中.的位置
        int position1 = ipv4.indexOf(".");
        int position2 = ipv4.indexOf(".", position1 + 1);
        int position3 = ipv4.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        int tmp = Integer.parseInt(ipv4.substring(0, position1));
        if(tmp>255) return null;
        ret[1] = (byte) tmp;

        tmp = Integer.parseInt(ipv4.substring(position1 + 1,position2));
        if(tmp>255) return null;
        ret[2] = (byte) tmp;
        
        tmp = Integer.parseInt(ipv4.substring(position2 + 1,position3));
        if(tmp>255) return null;
        ret[3] = (byte) tmp;
        
        tmp = Integer.parseInt(ipv4.substring(position3 + 1));
        if(tmp>255) return null;
        ret[4] = (byte) tmp;
        
        return ret;
    }

    /**
     * ipv6地址转有符号byte[17]
     * @param   ipv6 字符串形式的IP地址
     * @return big integer number
     */
    private static byte[] ipv6ToBytes(String ipv6) {
        byte[] ret = new byte[17];
        ret[0] = 0;
        int ib = 16;
        boolean comFlag = false;// ipv4混合模式标记
        if (ipv6.startsWith(":"))// 去掉开头的冒号
            ipv6 = ipv6.substring(1);
        String groups[] = ipv6.split(":");
        for (int ig = groups.length - 1; ig > -1; ig--) {// 反向扫描
            if (groups[ig].contains(".")) {
                // 出现ipv4混合模式
                byte[] temp = ipv4ToBytes(groups[ig]);
                ret[ib--] = temp[4];
                ret[ib--] = temp[3];
                ret[ib--] = temp[2];
                ret[ib--] = temp[1];
                comFlag = true;
            } else if ("".equals(groups[ig])) {
                // 出现零长度压缩,计算缺少的组数
                int zlg = 9 - (groups.length + (comFlag ? 1 : 0));
                while (zlg-- > 0) {// 将这些组置0
                    ret[ib--] = 0;
                    ret[ib--] = 0;
                }
            } else {
                int temp = Integer.parseInt(groups[ig], 16);
                ret[ib--] = (byte) temp;
                ret[ib--] = (byte) (temp >> 8);
            }
        }
        return ret;
    }
    
    public static boolean isIPV6(String ipStr){
    	if(ipStr.contains(":")) return true;
    	else return false;
    }
    
	public static void main(String[] args) {
/*		BigInteger bi = IPTool.IPv4ToBigInt("255.168.56.205");
		System.out.println(bi);
		System.out.println(IPTool.BigIntToIPv4(bi));		
*/
/*		Long bi = IPTool.IPv4ToBigInt("255.168.56.205");
		System.out.println(bi);
		System.out.println(IPTool.BigIntToIPv4(bi));		
*/
		String ipv6Str = "21DA:30:AD:2F3B::9C5A";
		ipv6Str = "21DA:0030:0000:00AD:2F3B:0000:0000:9C5A";
		BigInteger[] bis = IPTool.IPv6ToBigInt(ipv6Str);
		System.out.println(bis[0] + ":" + bis[1]);
		System.out.println(IPTool.BigIntToIPv6(bis));		

	}

}
