package com.techassisto.mrinmoy.assisto.utils;

/**
 * Created by Mrinmoy on 6/2/2017.
 */

public class Constants {
    //IP ADDR
    // TEMPORARY CODE
    public static final String SERVER_ADDR = "http://192.168.1.3:7087/";
    //public static final String SERVER_ADDR = "http://139.59.69.91/";

    //NETWORK STATUS
    public class Status {
        public static final int OK = 200;

        public static final int ERR_UNKNOWN = -100;
        public static final int ERR_NETWORK = -101;
        public static final int ERR_INVALID = -102;

    }

    // SHARED PREFERENCE
    public class UserPref {
        public static final String SP_NAME = "UserSharedPreference";
        public static final String SP_USERNAME = "UserName";
        public static final String SP_PASSWORD = "Password";
        public static final String SP_UTOKEN = "JWTToken";
        public static final String SP_TENANT = "TenantInfo";
    }
}
