

Step one:
SwitchMainEnter.getInstance().initOCR(this,AK ,SK); 

Step two:
SwitchMainEnter.getInstance().goToWeb(context,H5url,null);
Context是上下文,H5url是从后台返回的运营版URL,null是当前H5没有标题.
到这里为止,大部分功能已经可以使用了.下面是H5和原生壳交互的方法具体使用.


GetDeviceInfo()方法 参数为空 可以直接获取当前设备的信息;

isSuperman()方法 参数为空 返回是true 可判断当前是移动端打开;

isPerformance()方法 参数为空 根据当前设备性能返回 A+ A B+ B C五个评级 C是4.4以下包括4.4的手机版本,是自动跳转到浏览器打开;

LinkTo(String url)方法 参数是String类型 url是H5端需要通过原生壳打开的链接 此方法可以通过原生壳跳到外部浏览器打开相应的链接; 

doDiscern(String value)方法 参数是String类型 value是H5端传入原生壳的参数,front是识别身份证正面,back是识别身份证反面,bank是识别银行卡;


finishActivity()方法 关闭当前

call(String number)方法 参数是String类型 number是H5端传入原生壳的参数,可通过原生壳拨打相应电话
















