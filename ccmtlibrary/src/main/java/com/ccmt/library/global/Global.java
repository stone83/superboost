package com.ccmt.library.global;

import android.app.Service;

import com.ccmt.library.service.AbstractForeignService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Global {

    public static List<Class<? extends Service>> allRunningServices = new ArrayList<Class<? extends Service>>();
    public static String serializableFileDir;
    public static String serializableFileDirNotDelete;
    public static Map<Class<? extends AbstractForeignService>, Boolean> allInitForeignServices = //
            new HashMap<Class<? extends AbstractForeignService>, Boolean>();

}
