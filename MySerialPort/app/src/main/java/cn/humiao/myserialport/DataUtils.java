package cn.humiao.myserialport;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 串口数据转换工具类
 * Created by Administrator on 2016/6/2.
 */
public class DataUtils {
    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    public static int isOdd(int num) {
        return num & 1;
    }

    //-------------------------------------------------------
    //Hex字符串转int
    public static int HexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    public static String IntToHex(int intHex) {
        return Integer.toHexString(intHex);
    }

    //-------------------------------------------------------
    //Hex字符串转byte
    public static byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    //1字节转2个Hex字符
    public static String Byte2Hex(Byte inByte) {
        return String.format("%02x", new Object[]{inByte}).toUpperCase();
    }

    //-------------------------------------------------------
    //字节数组转转hex字符串
    public static String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : inBytArr) {
            strBuilder.append(Byte2Hex(Byte.valueOf(valueOf)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //字节数组转转hex字符串，可选长度
    public static String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(Byte2Hex(Byte.valueOf(inBytArr[i])));
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    //转hex字符串转字节数组
    public static byte[] HexToByteArr(String inHex) {
        byte[] result;
        int hexlen = inHex.length();
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * 按照指定长度切割字符串
     *
     * @param inputString 需要切割的源字符串
     * @param length      指定的长度
     * @return
     */
    public static List<String> getDivLines(String inputString, int length) {
        List<String> divList = new ArrayList<>();
        int remainder = (inputString.length()) % length;
        // 一共要分割成几段
        int number = (int) Math.floor((inputString.length()) / length);
        for (int index = 0; index < number; index++) {
            String childStr = inputString.substring(index * length, (index + 1) * length);
            divList.add(childStr);
        }
        if (remainder > 0) {
            String cStr = inputString.substring(number * length, inputString.length());
            divList.add(cStr);
        }
        return divList;
    }

    /**
     * 计算长度，两个字节长度
     *
     * @param val value
     * @return 结果
     */
    public static String twoByte(String val) {
        if (val.length() > 4) {
            val = val.substring(0, 4);
        } else {
            int l = 4 - val.length();
            for (int i = 0; i < l; i++) {
                val = "0" + val;
            }
        }
        return val;
    }

    /**
     * 校验和
     *
     * @param cmd 指令
     * @return 结果
     */
    public static String sum(String cmd) {
        List<String> cmdList = DataUtils.getDivLines(cmd, 2);
        int sumInt = 0;
        for (String c : cmdList) {
            sumInt += DataUtils.HexToInt(c);
        }
        String sum = DataUtils.IntToHex(sumInt);
        sum = DataUtils.twoByte(sum);
        cmd += sum;
        return cmd.toUpperCase();
    }

    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    /**
     * byte[]数组转换为16进制的字符串
     *
     * @param bytes 要转换的字节数组
     * @return 转换后的结果
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * 计算CRC16校验码  http://www.ip33.com/crc.html
     *
     * @param bytes
     * @return
     */
    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        //高低位转换，看情况使用（譬如本人这次对led彩屏的通讯开发就规定校验码高位在前低位在后，也就不需要转换高低位)
        //        CRC = ((CRC & 0x0000FF00) >> 8) | ((CRC & 0x000000FF) << 8);
        String result = Integer.toHexString(CRC).toUpperCase();
        if (!TextUtils.isEmpty(result)) {
            //            Log.e("res", result);
            if (result.length() == 3) {
                result = "0" + result;
            }
            String pre = result.substring(0, 2);
            //            Log.e("pre", pre);
            String suf = result.substring(2);
            result = suf + pre;
            //            Log.e("suf", suf);
        }
        return result;
    }

}
