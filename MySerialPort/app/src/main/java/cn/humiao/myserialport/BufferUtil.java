package cn.humiao.myserialport;

/**
 * Created by zhb on 2019/8/6.
 */
public class BufferUtil {

    //初始化
    public static String init() {
        String i = "010600653A98";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    //05出货命令
    public static String get05Buffer(String index) {
        String i = "010500" + index + "FF00";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    //02查询电机状态命令（和货道没有关系）
    public static String get02Buffer() {
        String i = "010200010001";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    //03查询出货结果命令
    public static String get03Buffer(String index) {
        String i = "010300" + index + "0001";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    //传送带开启
    public static String conveyor() {
        String i = "010600020001";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    //检查轮询
    public static String check() {
        String i = "010300020000";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    public static String conveyorClose() {
        String i = "0106006E0001";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }


    //开门
    public static String openDoor() {
        String i = "010600040000";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }

    //关门
    public static String closeDoor() {
        String i = "010600040001";
        byte[] b = DataUtils.hexStringToByteArray(i);
        String crc = DataUtils.getCRC(b);
        return i + crc;
    }


}
