package io.branch.referral;

import android.content.Context;
import io.branch.referral.Defines.PreinstallKey;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by --vbajpai on --24/07/2019 at --13:44 for --android-branch-deep-linking-attribution
 */
class BranchPreinstall {

  private String SYSTEM_PROPERTIES_CLASS_KEY = "android.os.SystemProperties";
  private String BRANCH_PREINSTALL_PROP_KEY = "ro.branch.preinstall.apps.path";

  public void getPreinstallSystemData(Context context) {

    // check if the SystemProperties has the branch file path added
    String branchFilePath = checkForBranchPreinstallInSystem();
    if (branchFilePath != null) {
      // after getting the file path get the file contents
      JSONObject branchFileContentJson = null;
      branchFileContentJson = readBranchFile(branchFilePath);
      if (branchFileContentJson != null) {
        // check if the current app package exists in the json
        Iterator<String> keys = branchFileContentJson.keys();

        while (keys.hasNext()) {
          String key = keys.next();
          try {
            if (key.equals("apps") && branchFileContentJson.get(key) instanceof JSONObject) {
              if (branchFileContentJson.getJSONObject(key)
                  .get(SystemObserver.getPackageName(context)) != null) {
                JSONObject branchPreinstallData = branchFileContentJson
                    .getJSONObject(key).getJSONObject(SystemObserver.getPackageName(context));

                // find the preinstalls keys and any custom data
                Iterator<String> preinstallDataKeys = branchPreinstallData.keys();
                while (preinstallDataKeys.hasNext()) {
                  String datakey = preinstallDataKeys.next();
                  if (datakey.equals(PreinstallKey.campaign.getKey())) {
                    Branch.getInstance()
                        .setPreinstallCampaign(branchPreinstallData.get(datakey).toString());
                  } else if (datakey.equals(PreinstallKey.partner.getKey())) {
                    Branch.getInstance()
                        .setPreinstallPartner(branchPreinstallData.get(datakey).toString());
                  } else {
                    Branch.getInstance().setRequestMetadata(datakey, branchPreinstallData.get(datakey).toString());
                  }
                }
              }
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private String checkForBranchPreinstallInSystem() {
    String path = null;
    try {
      path = (String) Class.forName(SYSTEM_PROPERTIES_CLASS_KEY)
          .getMethod("get", String.class).invoke(null, BRANCH_PREINSTALL_PROP_KEY);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return path;
  }

  private JSONObject readBranchFile(String branchFilePath) {

    JSONObject branchFileContentJson = null;
    File yourFile = new File(branchFilePath);
    StringBuilder branchFileContent = new StringBuilder();
    try {
      BufferedReader br = new BufferedReader(new FileReader(yourFile));
      String line;

      while ((line = br.readLine()) != null) {
        branchFileContent.append(line);
      }
      br.close();
    } catch (IOException ignore) {
      // the file does not exists
    }

    try {
      branchFileContentJson = new JSONObject(branchFileContent.toString().trim());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return branchFileContentJson;
  }

}
