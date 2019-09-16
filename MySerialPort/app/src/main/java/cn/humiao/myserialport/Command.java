package cn.humiao.myserialport;

/**
 * Created by zhb on 2019/8/6.
 */
public class Command {

    //初始化
    public static String getConveyorInitCommand() {
        String i = "010600653A98";
        return stringTrans(i);

    }

    //05出货命令
    public static String get05Command(String cargowayCode, String boardIndex) {
        String i = boardIndex + "0500" + cargowayCode + "FF00";
        return stringTrans(i);

    }

    //02查询电机状态命令（和货道没有关系）
    public static String get02Command(String boardIndex) {
        String i = boardIndex + "0200010001";
        return stringTrans(i);
    }

    //03查询出货结果命令
    public static String get03Command(String cargowayCode, String boardIndex) {
        String i = boardIndex + "0300" + cargowayCode + "0001";
        return stringTrans(i);

    }

    //传送带开启
    public static String getConveyorRunCommand() {
        String i = "010600020001";
        return stringTrans(i);

    }

    //检查轮询
    public static String conveyorCheck() {
        String i = "010300020000";
        return stringTrans(i);

    }

    public static String conveyorClose() {
        String i = "0106006E0001";
        return stringTrans(i);

    }


    //开门
    public static String openDoor() {
        String i = "010600040000";
        return stringTrans(i);
    }

    //关门
    public static String closeDoor() {
        String i = "010600040001";
        return stringTrans(i);
    }


    private static String stringTrans(String src) {
        byte[] b = DataUtils.hexStringToByteArray(src);
        String crc = DataUtils.getCRC(b);
        return src + crc;
    }

}
