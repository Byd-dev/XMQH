package com.pro.switchlibrary;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SDCheckEntity {


    /**
     * code : 200
     * key : ez
     * default : jytqh.com
     * data : [{"code":"ok","id":1,"name":"外汇原油大师","type":"Android","value":"https://dl11.zhejv.com"},{"code":"ok","id":2,"name":"牛牛期货策略","type":"Android","value":"https://dl7.zhejv.com"},{"code":"ok","id":3,"name":"汇科红犀理财","type":"Android","value":"https://gf10.zhejv.com"},{"code":"ok","id":4,"name":"微盈e交易","type":"Android","value":"https://dl14.zhejv.com"},{"code":"ok","id":5,"name":"中资远期策略","type":"Android","value":"https://dl13.zhejv.com"},{"code":"ok","id":6,"name":"掌上原油宝","type":"Android","value":"https://gf10.zhejv.com"},{"code":"ok","id":7,"name":"期货掌上通","type":"Android","value":"https://gf10.zhejv.com"},{"code":"ok","id":8,"name":"期货原油交易大师","type":"Android","value":"https://dl2.zhejv.com"},{"code":"ok","id":9,"name":"呆呆赚期货","type":"Android","value":"https://dl10.zhejv.com"},{"code":"ok","id":10,"name":"聚汇期货","type":"Android","value":"https://gf9.zhejv.com"},{"code":"ok","id":11,"name":"聚期货","type":"Android","value":"https://www.zhejv.com","value1":"http://www.baidu.com"},{"code":"ok","id":14,"app_id":"cdqh","app_name":"财达期货","type":"reviewed","value":"https://dl23.chunlvbank.com"}]
     * msg : 查询成功
     */

    private int code;
    private String key;
    @SerializedName("default")
    private String defaultX;
    private String msg;
    private List<DataBean> data;

    @Override
    public String toString() {
        return "SDCheckEntity{" +
                "code=" + code +
                ", key='" + key + '\'' +
                ", defaultX='" + defaultX + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultX() {
        return defaultX;
    }

    public void setDefaultX(String defaultX) {
        this.defaultX = defaultX;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        @Override
        public String toString() {
            return "DataBean{" +
                    "code='" + code + '\'' +
                    ", id=" + id +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", value1='" + value1 + '\'' +
                    ", app_id='" + app_id + '\'' +
                    ", app_name='" + app_name + '\'' +
                    '}';
        }

        /**
         * code : ok
         * id : 1
         * name : 外汇原油大师
         * type : Android
         * value : https://dl11.zhejv.com
         * value1 : http://www.baidu.com
         * app_id : cdqh
         * app_name : 财达期货
         */



        private String code;
        private int id;
        private String name;
        private String type;
        private String value;
        private String value1;
        private String app_id;
        private String app_name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public String getApp_name() {
            return app_name;
        }

        public void setApp_name(String app_name) {
            this.app_name = app_name;
        }
    }
}
