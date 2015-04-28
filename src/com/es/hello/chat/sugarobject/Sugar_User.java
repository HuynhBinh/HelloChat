package com.es.hello.chat.sugarobject;

import com.orm.SugarRecord;

//reflect the object store in local phone DB
//object: User
public class Sugar_User extends SugarRecord<Sugar_User>
{

    public int userId;

    public String userLogin;

    public String userPassword;

    public String userWebsite;

    public String userFullName;

    public String userCustomData;

    public Sugar_User()
    {

    }

}
