package com.example.mqttapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {
    private String serverUri = "tcp://152.136.110.145:1883";  //这里可以填上各种云平台的物联网云平的域名+1883端口号，什么阿里云腾讯云百度云天工物接入都可以，
    // 这里我填的是我在我的阿里云服务器上搭建的EMQ平台的地址，
    // 注意：前缀“tcp：//”不可少，之前我没写，怎么都连不上，折腾了好久
    private String userName = "root";                    //然后是你的用户名，阿里云腾讯云百度云天工物接入这些平台你新建设备后就自动生成了
    private String passWord = "Zzj123456";                    //用户名对应的密码，同样各种云平台都会对应生成密码，这里我的EMQ平台没做限制，所以用户名和密码可以随便填写
    private String clientId = "app"+System.currentTimeMillis(); //clientId很重要，不能重复，否则就会连不上，所以我定义成 app+当前时间
    private String mqtt_sub_topic = "test";          //需要订阅的主题
    private String mqtt_pub_topic = "test";          //需要发布的主题

    private MqttClient mqtt_client;                         //创建一个mqtt_client对象
    MqttConnectOptions options;

    private ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeToast("clientID:" + clientId);
        mqtt_init_Connect();

    }

    public void mqtt_init_Connect()
    {
        try {
            //实例化mqtt_client，填入我们定义的serverUri和clientId，然后MemoryPersistence设置clientid的保存形式，默认为以内存保存
            mqtt_client = new MqttClient(serverUri,clientId,new MemoryPersistence());
            //创建并实例化一个MQTT的连接参数对象
            options = new MqttConnectOptions();
            //然后设置对应的参数
            options.setUserName(userName);                  //设置连接的用户名
            options.setPassword(passWord.toCharArray());    //设置连接的密码
            options.setConnectionTimeout(30);               // 设置超时时间，单位为秒
            options.setKeepAliveInterval(50);               //设置心跳,30s
            options.setAutomaticReconnect(true);            //是否重连
            //设置是否清空session,设置为false表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);

            //设置回调
            mqtt_client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    makeToast("connectionLost");
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    //subscribe后得到的消息会执行到这里面
                }
            });
            //连接mqtt服务器
            mqtt_client.connect(options);

        }catch (Exception e) {
            e.printStackTrace();
            makeToast(e.toString());
        }
    }

    public void publishMessage(String topic,String message_str){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(message_str.getBytes());
            if(mqtt_client.isConnected()){
                mqtt_client.publish(topic, message);
            }
        } catch (MqttException e) {
            e.printStackTrace();

        }
    }

    private void makeToast(String toast_str) {
        Toast.makeText(MainActivity.this, toast_str, Toast.LENGTH_LONG).show();
    }
}




