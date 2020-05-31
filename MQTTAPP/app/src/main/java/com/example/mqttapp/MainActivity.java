package com.example.mqttapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {
    private String serverUri = "tcp://152.136.110.145:1883";    //这里可以填上各种云平台的物联网云平的域名+1883端口号，什么阿里云腾讯云百度云天工物接入都可以，
                                // 注意：前缀“tcp：//”不可少
    private String userName = "root";                           //然后是你的用户名，阿里云腾讯云百度云天工物接入这些平台你新建设备后就自动生成了
    private String passWord = "passWord";                      //用户名对应的密码，同样各种云平台都会对应生成密码
    private String clientId = "app"+System.currentTimeMillis(); //clientId很重要，不能重复，否则就会连不上，所以我定义成 app+当前时间
    private String mqtt_sub_topic = "test";                     //需要订阅的主题
    //private String mqtt_pub_topic = "test";                   //需要发布的主题

    private Boolean IsConnect=false;

    private MqttClient mqtt_client;                             //创建一个mqtt_client对象
    MqttConnectOptions options;

    private Button Connect;
    private Button Publish;
    private Button subscribe;

    private EditText PubMessage;
    private EditText PubTopic;
    private EditText SubTopic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeToast("clientID:" + clientId);
        Connect=(Button)findViewById(R.id.ConnectionButton);
        PubTopic=(EditText)findViewById(R.id.PublishTopicEditText);
        PubMessage=(EditText)findViewById(R.id.PublishMessageEditText);
        Publish=(Button)findViewById(R.id.PublishMessageButton);
//        SubTopic=(EditText)findViewById(R.id.SubscribeTopicEditText);
//        subscribe=(Button)findViewById(R.id.SubscribeTopicButton);
    }
    /*连接按钮触发*/
    public void ConnectionButtonClick(View v)
    {
        mqtt_init_Connect();
        if(mqtt_client.isConnected()&&IsConnect==false)
        {
            makeToast("ConnectTRUE");
            Connect.setText("已连接");
            IsConnect=true;
        }
        else if(mqtt_client.isConnected()&&IsConnect==true)
        {
            disconnect();
            makeToast("DisconnectTRUE");
            Connect.setText("连接");
            IsConnect=false;
        }
        else
        {
            makeToast("ConnectFALSE");
            IsConnect=false;
        }
    }

    /*发布按钮触发*/
    public void PublishMessageButtonClick(View v)
    {
        if(IsConnect)
        {
            publishMessage(PubTopic.getText().toString(),PubMessage.getText().toString());
            makeToast("PublishMessageButtonClick");
        }
        else
        {
            makeToast("WithoutConnect");
        }
    }

//    /*订阅按钮触发*/
//    public void subscribeTopicButtonClick(View v)
//    {
//        if(IsConnect)
//        {
//            makeToast("subscribeTopicButtonClicked");
//            subscribeTopic();
//        }
//        else
//        {
//            makeToast("WithoutConnect");
//        }
//    }

    /*连接初始化*/
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
            mqtt_client.setCallback(new MqttCallback/*Extended*/() {
               /*@Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    //连接成功
                    makeToast("connectionsuccess");
                }*/
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    makeToast("connectionLost");
                    mqtt_init_Connect();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    makeToast("publishMessage Success");
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    makeToast(message.toString());

                }
            });
            //连接mqtt服务器
            mqtt_client.connect(options);

        }catch (Exception e) {
            e.printStackTrace();
            makeToast(e.toString());
        }
    }

    /*发布*/
    public void publishMessage(String topic, String message_str){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(message_str.getBytes());
            if(mqtt_client.isConnected()){
                mqtt_client.publish(topic, message);
            }
        } catch (MqttException e) {
            e.printStackTrace();
            makeToast(e.toString());
        }
    }

//    /*订阅*/
//    private void subscribeTopic(){
//        try{
//            mqtt_client.subscribe(mqtt_sub_topic,1);
//        } catch (Exception e){
//            e.printStackTrace();
//            makeToast(e.toString());
//            }
//    }

    /*断开连接*/
    public void disconnect() {
        try {
            mqtt_client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            makeToast(e.toString());
        }
    }

    /*APP提示封装*/
    private void makeToast(String toast_str) {
        Toast.makeText(MainActivity.this, toast_str, Toast.LENGTH_LONG).show();
    }
}




