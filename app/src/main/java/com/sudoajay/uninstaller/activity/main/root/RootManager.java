

package com.sudoajay.uninstaller.activity.main.root;

import androidx.annotation.Nullable;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class RootManager {

    private final String[] SU_BINARY_DIRS = {
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
    };
//    private AppExecutors appExecutors;
//    SingleLiveEvent<Boolean> uninstallResult = new SingleLiveEvent<>();

//    public RootManager(AppExecutors appExecutors) {
//        this.appExecutors = appExecutors;
//    }

    public boolean hasRootedPermision() {
        return Shell.SU.available();
    }

    public boolean wasRooted() {
        boolean hasRooted = false;
        for (String path : SU_BINARY_DIRS) {
            File su = new File(path + "/su");
            if (su.exists()) {
                hasRooted = true;
                break;
            } else {
                hasRooted = false;
            }
        }
        return hasRooted;
    }

//    public void removeApps(List<App> appsToRemove) {
//        appExecutors.diskIO().execute(() -> {
//            boolean uninstalledNoProblems = true;
//            for(App app : appsToRemove) {
//                boolean result = true;
//                if(app.isSystemApp()) {
//                    result = uninstallSystemApp(app.getPath());
//                    if(!result)
//                        result = uninstallSystemAppAlternativeMethod(app.getPackageName());
//                }
//                else
//                    result = uninstallUserApp(app.getPackageName());
//                if(!result)
//                    uninstalledNoProblems = false;
//            }
//            uninstallResult.postValue(uninstalledNoProblems);
//        });
//    }

    private boolean uninstallSystemApp(String appApk) {
        executeCommandSU("mount -o rw,remount /system");
        executeCommandSU("rm " + appApk);
        executeCommandSU("mount -o ro,remount /system");
        boolean result = checkUninstallSuccessful(appApk);
        return result;
    }

    private boolean uninstallSystemAppAlternativeMethod(String packageName) {
        String commandOutput = executeCommandSU("pm uninstall --user 0 " + packageName);
        return checkPMCommandSuccesfull(commandOutput);
    }

    private boolean uninstallUserApp(String packageName) {
        String commandOutput = executeCommandSU("pm uninstall " + packageName);
        return checkPMCommandSuccesfull(commandOutput);
    }

    private String executeCommandSU(String command) {
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        try {
            Shell.Pool.SU.run(command, stdout, stderr, true);
        } catch (Shell.ShellDiedException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : stdout) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private String executeCommandSH(String command) {
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        try {
            Shell.Pool.SH.run(command, stdout, stderr, true);
        } catch (Shell.ShellDiedException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : stdout) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private boolean checkUninstallSuccessful(String appApk) {
        String output = executeCommandSH("ls " + appApk);
        return output.trim().isEmpty();
    }

    private boolean checkPMCommandSuccesfull(String commandOutput) {
        return commandOutput != null && commandOutput.toLowerCase().contains("success");
    }

//    public SingleLiveEvent<Boolean> getUninstallResult() {
//        return uninstallResult;
//    }

    public String rebootDevice() {
        return executeCommandSU("reboot");
    }
}
