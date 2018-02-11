package com.techassisto.mrinmoy.assisto.utils;

/**
 * Created by Mrinmoy on 6/2/2017.
 */

public class Constants {
    //IP ADDR
//    public static final String SERVER_ADDR = "http://192.168.1.2:7087/";
//    public static final String SERVER_ADDR = "http://192.168.43.18:8000/";
//        public static final String SERVER_ADDR = "http://192.168.31.246:8000/";
    public static final String SERVER_ADDR = "https://www.techassisto.com/";

    //NETWORK STATUS
    public class Status {
        public static final int OK = 200;

        public static final int ERR_UNKNOWN = -100;
        public static final int ERR_NETWORK = -101;
        public static final int ERR_INVALID = -102;

    }

    // SHARED PREFERENCE
    public class UserPref {
        public static final String SP_NAME        = "UserSharedPreference";
        public static final String SP_USERNAME    = "UserName";
        public static final String SP_PASSWORD    = "Password";
        public static final String SP_UTOKEN      = "JWTToken";
        public static final String SP_TENANT      = "TenantInfo";
        public static final String SP_PRINTER      = "Printer";
    }

    // STATES
    public static String[] STATE_LIST = {"None", "Jammu & Kashmir", "Himachal Pradesh", "Punjab",
            "Chandigarh", "Uttarkhand", "Haryana", "National Capital Territory of Delhi",
            "Rajashtan", "Uttar Pradesh", "Bihar", "Sikkim", "Arunachal Pradesh", "Nagaland",
            "Manipur", "Mizoram", "Tripura", "Meghalaya", "Assam", "West Bengal", "Jharkhand",
            "Odisha", "Chattisgarh", "Madhya Pradesh", "Gujrat", "Daman & Diu", "Dadra & Nagar Haveli",
            "Maharashtra", "Andhra Pradesh", "Karnataka", "Goa", "Lakshadweep", "Kerala",
            "Tamil Nadu", "Puducherry", "Andaman & Nicobar Island", "Telengana", "Uttarkhand"};
}
