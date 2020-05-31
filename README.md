# The-IOT-System-based-on-MQTT-V3.0

## MQTTAPP

2020.05.30-19：55

​		实现连接功能，但是无法自动重连以及连接成功回调。

2020.05.31-12：58

​		实现发布功能。



这里对待完善的点进行总结：

1.MqttCallbackExtended的实现-->更多的回调函数以及断线重连的实现

2.subscribeTopic的实现-->主题订阅（这个作为拓展功能，主要是想把MQTT的功能完全实现）



这里记录一个假设，以上两点的实现,可能都与org.eclipse.paho.android.service-1.1.1.jar的应用有关,但由于我在使用该jar包时,应用闪退,故先做放弃,此处有一个疑点,就是在无service应用时,依然实现了publishMessage功能,关于service的使用,以后再做讨论。



