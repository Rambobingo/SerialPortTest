package cn.humiao.myserialport;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private Button button;
    private TextView tv;
    private SerialPortUtil serialPortUtil;

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btn);
        tv = findViewById(R.id.tv);
        serialPortUtil = new SerialPortUtil();
        serialPortUtil.openSerialPort();
        //        serialPortUtil.init(BufferUtil.init());
        //注册EventBus
        EventBus.getDefault().register(this);

        serialPortUtil.spitGoods("01", "01");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                serialPortUtil.sendConveyorSerialPort(BufferUtil.conveyor());


            }
        });

        //        Log.e("open", BufferUtil.get02Buffer());

        //        for (int i = 0; i < 5; i++) {
        //            Log.e("i", i + "");
        //            if (i == 3) {
        //                break;
        //            }
        //            try {
        //                Thread.sleep(1000);
        //            } catch (InterruptedException e) {
        //                e.printStackTrace();
        //            }
        //        }
    }

    /**
     * 用EventBus进行线程间通信，也可以使用Handler
     *
     * @param string
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String string) {
        Log.d(TAG, "获取到了从传感器发送到Android主板的串口数据");
        count++;
        if (count < 10) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    if (count <= 2) {
//                        serialPortUtil.spitGoods("01", "01");
//                    } else if (count <= 4) {
//                        serialPortUtil.spitGoods("01", "02");
//                    } else if (count <= 6) {
//                        serialPortUtil.spitGoods("02", "03");
//                    }else {
//                        serialPortUtil.spitGoods("01", "04");
//                    }
                    //                                        serialPortUtil.spitGoods("01", "01");

                }
            }, 1000);
        }
        tv.setText(String.valueOf(count));
    }
}
