// IMyAidlInterface.aidl
package com.jonghyeon.aidlservice;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     int add(int a, int b);
     int sub(int a, int b);
     void sendNotification(String a, String b);
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
