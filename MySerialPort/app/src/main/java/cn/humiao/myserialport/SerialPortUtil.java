package cn.humiao.myserialport;

import android.os.Handler;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * @author by AllenJ on 2018/4/20.
 * <p>
 * 通过串口用于接收或发送数据
 */

public class SerialPortUtil {

    private static final String TAG = "SERIAL_PORT";

    private SerialPort serialPortUSB0 = null;
    private InputStream inputStreamUSB0 = null;
    private OutputStream outputStreamUSB0 = null;
    private ReceiveUSB0Thread mReceiveUSB0Thread = null;
    private ReceiveTtyS1Thread mReceiveTtyS1Thread = null;
    private boolean isStart = false;

    private SerialPort serialPortS = null;
    private InputStream inputStreamS = null;
    private OutputStream outputStreamS = null;


    private String data; //指令数据
    private String channel; //货道号

    private String command05String; //05指令数据
    private String commandConveyorString; //传送带指令数据
    private String cargowayCode; //货道号
    private String boardIndex; //主板号

    private static StringBuffer contentSB = new StringBuffer();
    private static byte[] readData = new byte[1024];


    private final Object lock = new Object();
    private boolean pause = false;


    /**
     * 调用这个方法实现暂停线程
     */
    private void pauseThread() {
        pause = true;
    }

    /**
     * 调用这个方法实现恢复线程的运行
     */
    private void resumeThread() {
        pause = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * 注意：这个方法只能在run方法里调用，不然会阻塞主线程，导致页面无响应
     */
    private void onPause() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 打开串口，接收数据
     * 通过串口，接收单片机发送来的数据
     */
    public void openSerialPort() {
        try {
            serialPortUSB0 = new SerialPort(new File("/dev/ttyUSB0"), 38400, 0);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            inputStreamUSB0 = serialPortUSB0.getInputStream();
            outputStreamUSB0 = serialPortUSB0.getOutputStream();

            serialPortS = new SerialPort(new File("/dev/ttyS1"), 38400, 0);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            inputStreamS = serialPortS.getInputStream();
            outputStreamS = serialPortS.getOutputStream();

            isStart = true;


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭串口
     * 关闭串口中的输入输出流
     */
    public void closeSerialPort() {
        Log.i("test", "关闭串口");
        try {
            if (inputStreamUSB0 != null) {
                inputStreamUSB0.close();
            }
            if (outputStreamUSB0 != null) {
                outputStreamUSB0.close();
            }
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param cargowayCode 货道号
     * @param boardIndex   主板号
     */

    public void spitGoods(String cargowayCode, String boardIndex) {

        try {

            this.boardIndex = boardIndex;
            this.cargowayCode = cargowayCode;

            String command05 = Command.get05Command(cargowayCode, boardIndex);
            this.command05String = command05;

            Log.e(TAG, "begin send 05 ->" + command05);

            byte[] sendData = DataUtils.HexToByteArr(command05String);
            outputStreamUSB0.write(sendData);
            outputStreamUSB0.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        getUSB0SerialPort();

    }

    /**
     * 发送05数据
     * 通过串口，发送数据到单片机
     *
     * @param data    要发送的数据
     * @param channel 货道号
     */
    public void send05SerialPort(final String data, final String channel) {
        try {
            this.data = data;
            Log.e("05", data);
            this.channel = channel;
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamUSB0.write(sendData);
            outputStreamUSB0.flush();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (pause) {
                        resumeThread();
                    }
                }
            }, 2000);

            getTtyS1SerialPort();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送02数据
     * 通过串口，发送数据到单片机
     *
     * @param data 要发送的数据
     */
    public void send02SerialPort(final String data) {
        try {
            Log.e("02", data);
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamUSB0.write(sendData);
            outputStreamUSB0.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送03数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void send03SerialPort(final String data) {
        try {
            Log.e("03", data);
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamUSB0.write(sendData);
            outputStreamUSB0.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送初始化数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void initConveyor(final String data) {
        try {
            Log.e("初始化", data);
            this.data = data;
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamS.write(sendData);
            outputStreamS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 发送传送带数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void sendConveyorSerialPort(final String data) {
        try {
            Log.e("传送带", data);
            this.commandConveyorString = data;
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamS.write(sendData);
            outputStreamS.flush();

            if (boardIndex.equals("01") || boardIndex.equals("04")) {
                Log.e("111", "1111");
                Thread.sleep(12000);
            } else {
                Log.e("222", "222");
                Thread.sleep(8000);
            }
            sendConveyorClose(Command.conveyorClose());
            Thread.sleep(1000);
            sendOpenDoorSerialPort(Command.openDoor());
            Thread.sleep(10000);
            sendCloseDoorSerialPort(Command.closeDoor());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //        getTtyS1SerialPort();

    }

    /**
     * 发送传送带check数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void sendConveyorCheck(final String data) {
        try {
            Log.e("传送带check", data);
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamS.write(sendData);
            outputStreamS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 发送传送带关闭数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void sendConveyorClose(final String data) {
        try {
            Log.e("传送带关闭", data);
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamS.write(sendData);
            outputStreamS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 发送开门数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void sendOpenDoorSerialPort(final String data) {
        try {
            Log.e("开门", data);
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamS.write(sendData);
            outputStreamS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送关门数据
     * 通过串口，发送数据到单片机;
     *
     * @param data 要发送的数据
     */
    public void sendCloseDoorSerialPort(final String data) {
        try {
            Log.e("关门", data);
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStreamS.write(sendData);
            outputStreamS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void postDelayed(long delayMillis) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (inputStreamUSB0 == null) {
                    return;
                }
                byte[] readData = new byte[1024];
                try {
                    int size = inputStreamUSB0.read(readData);
                    if (size > 0) {
                        String readString = DataUtils.ByteArrToHex(readData, 0, size);
                        Log.e("rs", readString);
                        if (readString.equals(data)) { //05出货命令的Response
                            try {
                                Thread.sleep(100);
                                send02SerialPort(BufferUtil.get02Buffer());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        if (readString.startsWith("0102") && readString.length() > 8) { //电机运行状态
                            if (readString.substring(7, 8).equals("1")) { //出货中
                                try {
                                    Thread.sleep(500);
                                    send02SerialPort(BufferUtil.get02Buffer());
                                    Log.e("02", "s0002");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {   //出货完成
                                Log.e("02", "else0002");
                                send03SerialPort(BufferUtil.get03Buffer(channel)); //查询出货结果
                            }
                            return;
                        }


                        if (readString.startsWith("0103") && readString.length() > 10) { //读出货结果返回值
                            String status = readString.substring(9, 10);
                            switch (status) {
                                case "0":
                                    EventBus.getDefault().post(status);
                                    break;
                                case "1":
                                    break;
                                case "2":
                                    break;
                                case "3":
                                    break;
                                case "4":
                                    break;
                                case "5":
                                    break;
                                case "6":
                                    break;
                                case "9":
                                    break;
                                case "A":
                                    break;
                                case "B":
                                    break;
                                case "C":
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else {
                        send03SerialPort(BufferUtil.get03Buffer(channel)); //查询出货结果
                    }

                } catch (IOException e) {
                    Log.e("err", "err");
                    e.printStackTrace();
                }
            }
        }, delayMillis);
    }

    private void getUSB0SerialPort() {
        if (mReceiveUSB0Thread == null) {
            mReceiveUSB0Thread = new ReceiveUSB0Thread();
        }
        if (!mReceiveUSB0Thread.isAlive()) {
            mReceiveUSB0Thread.start();
        }
    }


    private void getTtyS1SerialPort() {
        if (mReceiveTtyS1Thread == null) {
            mReceiveTtyS1Thread = new ReceiveTtyS1Thread();
        }
        if (!mReceiveTtyS1Thread.isAlive()) {
            mReceiveTtyS1Thread.start();
        }
    }

    /**
     * 接收USB0串口数据的线程
     */
    private class ReceiveUSB0Thread extends Thread {
        @Override
        public void run() {
            super.run();
            //条件判断，只要条件为true，则一直执行这个线程
            while (isStart) {
                //                while (pause) {
                //                    onPause();
                //                }

                //                try {
                //                    Thread.sleep(100);
                //                } catch (InterruptedException e) {
                //                    e.printStackTrace();
                //                }

                if (inputStreamUSB0 == null) {
                    return;
                }


                try {
                    int size;
                    while ((size = inputStreamUSB0.read(readData)) > 0) {
                        String readString = DataUtils.ByteArrToHex(readData, 0, size);
                        contentSB.append(readString);
                        Log.d("SerialPort", "receive data: " + readString + "  contentSB  " + contentSB.toString());

                        if (1 == 1) {
                            Thread.sleep(30000);
                            sendConveyorSerialPort(Command.getConveyorRunCommand());
                            isStart = false;
                            return;
                        }


                        if (contentSB.toString().contains(command05String)) {
                            Thread.sleep(100);
                            Log.d("SerialPort", "eq send02SerialPort ");
                            send02SerialPort(Command.get02Command(boardIndex));
                            contentSB.setLength(0);
                            continue;
                        }

                        if (contentSB.toString().startsWith(boardIndex + "02") && contentSB.toString().length() == 12) { //电机运行状态
                            if (contentSB.toString().substring(7, 8).equals("1")) { //出货中
                                Log.e("02", "if");
                                Thread.sleep(1000);
                                send02SerialPort(Command.get02Command(boardIndex));
                            } else {   //出货完成
                                Thread.sleep(1000);
                                send03SerialPort(Command.get03Command(cargowayCode, boardIndex)); //查询出货结果
                            }
                            contentSB.setLength(0);
                            continue;
                        }


                        if (contentSB.toString().startsWith(boardIndex + "03") && contentSB.toString().length() == 14) { //读出货结果返回值
                            String status = contentSB.toString().substring(9, 10);
                            contentSB.setLength(0);
                            Log.e("status", status);
                            switch (status) {
                                case "0":
//                                    initConveyor(Command.getConveyorInitCommand());
//                                    Thread.sleep(1000);
//                                    sendConveyorSerialPort(Command.getConveyorRunCommand());
                                    //                                    EventBus.getDefault().post("00");
                                    break;
                                case "1":
                                    EventBus.getDefault().post("01");
                                    //                                    Thread.sleep(100);
                                    //                                    sendConveyorSerialPort(Command.getConveyorRunCommand());
                                    break;
                                case "2":
                                    break;
                                case "3":
                                    break;
                                case "4":
                                    //                                            initConveyor(Command.getConveyorInitCommand());
                                    //                                    Thread.sleep(100);
                                    //                                    sendConveyorSerialPort(Command.getConveyorRunCommand());
                                    EventBus.getDefault().post("04");
                                    break;
                                case "5":
                                    break;
                                case "6":
                                    break;
                                case "9":
                                    break;
                                case "A":
                                    break;
                                case "B":
                                    break;
                                case "C":
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    /**
     * 接收ttyS1串口数据的线程
     */
    private class ReceiveTtyS1Thread extends Thread {
        @Override
        public void run() {
            super.run();
            //条件判断，只要条件为true，则一直执行这个线程
            while (isStart) {
                if (inputStreamS == null) {
                    return;
                }

                try {
                    int size;
                    while ((size = inputStreamS.read(readData)) > 0) {
                        String readString = DataUtils.ByteArrToHex(readData, 0, size);
                        contentSB.append(readString);
                        Log.d("SerialPort", "receive data: " + readString + "  contentSBTtyS1  " + contentSB.toString());

                        if (contentSB.toString().contains(commandConveyorString)) {
                            //                            sendConveyorCheck(Command.conveyorCheck());
                            //                            if (boardIndex.equals("01")) {
                            //                                Thread.sleep(12000);
                            //                            } else {
                            //                                Thread.sleep(7000);
                            //                            }
                            //
                            //                            sendConveyorClose(Command.conveyorClose()); //关闭传送带指令
                            //
                            //                            Thread.sleep(1000);
                            //                            sendOpenDoorSerialPort(Command.openDoor()); //开门指令
                            //
                            //                            Thread.sleep(10000);
                            //                            sendCloseDoorSerialPort(Command.closeDoor()); //关门指令

                            contentSB.setLength(0);
                            continue;
                        }

                        if (contentSB.toString().startsWith("0103") && contentSB.toString().length() == 14) {
                            if (contentSB.toString().substring(9, 10).equals("1")) {

                                Thread.sleep(1000);
                                sendConveyorCheck(Command.conveyorCheck());

                            } else {   //出货完成
                                Log.e("ggg", "ggg");
                                //                                Thread.sleep(1000);
                                //                                sendConveyorClose(Command.conveyorClose()); //关闭传送带指令
                                //
                                //                                Thread.sleep(1000);
                                //                                sendOpenDoorSerialPort(Command.openDoor()); //开门指令
                                //
                                //                                Thread.sleep(10000);
                                //                                sendCloseDoorSerialPort(Command.closeDoor()); //关门指令
                            }
                            contentSB.setLength(0);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

}
