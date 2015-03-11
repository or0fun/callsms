package com.fang.call;

import android.provider.CallLog;

/**
 * 通话记录类型
 * Created by fang on 3/11/15.
 */
public class CallLogType {

    /** Call log type for incoming calls. */
    public static final int INCOMING_TYPE = CallLog.Calls.INCOMING_TYPE;
    /** Call log type for outgoing calls. */
    public static final int OUTGOING_TYPE = CallLog.Calls.OUTGOING_TYPE;
    /** Call log type for missed calls. */
    public static final int MISSED_TYPE = CallLog.Calls.MISSED_TYPE;
    /**
     * Call log type for voicemails.
     * @hide
     */
    public static final int VOICEMAIL_TYPE = 4;


    /** add contact */
    public static final int ADD_TYPE = 10;

}
