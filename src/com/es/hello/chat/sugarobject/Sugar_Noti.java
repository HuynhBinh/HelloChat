package com.es.hello.chat.sugarobject;

import com.orm.SugarRecord;

//reflect the object store in local phone DB
//object: Notification = push notification from server
public class Sugar_Noti extends SugarRecord<Sugar_Dialog>
{
    public int hashcodeId;

    public int message;

    public Sugar_Noti()
    {

    }

}