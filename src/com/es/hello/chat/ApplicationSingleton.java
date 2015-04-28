package com.es.hello.chat;

import java.util.ArrayList;
import java.util.List;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.users.model.QBUser;

public class ApplicationSingleton
{

    public static String CURRENT_GROUP_NAME = "";

    public static String CURRENT_GROUP_PHOTO = "";

    public static List<QBUser> currentSelectedUsersInGroup = new ArrayList<QBUser>();

    public static List<QBDialog> selectedDialogToCreateGroupChat = new ArrayList<QBDialog>();

    public static boolean isNewGroupPhoto = false;

}
