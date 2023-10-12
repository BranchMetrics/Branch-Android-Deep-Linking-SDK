package io.branch.referral;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Class that observes activity life cycle events and determines when to start and stop
 * session.</p>
 */
class BranchActivityLifecycleObserver implements Application.ActivityLifecycleCallbacks {
    private int activityCnt_ = 0; //Keep the count of visible activities.

    //Set of activities observed in this session, note storing it as Activity.toString() ensures
    // that multiple instances of the same activity are counted individually.
    private Set<String> activitiesOnStack_ = new HashSet<>();

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        BranchLogger.v("onActivityCreated, activity = " + activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        BranchLogger.v("onActivityStarted, activity = " + activity);
        Branch branch = Branch.getInstance();
        if (branch == null) {
            return;
        }

        // technically this should be in onResume but it is effectively the same to have it here, plus
        // it allows us to use currentActivityReference_ in session initialization code
        branch.currentActivityReference_ = new WeakReference<>(activity);

        activityCnt_++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        BranchLogger.v("onActivityResumed, activity = " + activity);
        Branch branch = Branch.getInstance();
        if (branch == null) return;

        if (branch.getInitState() == Branch.SESSION_STATE.UNINITIALISED && !Branch.disableAutoSessionInitialization) {
            if (Branch.getPluginName() != null) {
                BranchLogger.v("onActivityResumed called and SESSION_STATE = UNINITIALISED, however this is a " + Branch.getPluginName() + " plugin, so we are NOT initializing session on user's behalf");
            }
        }

        // must be called after session initialization, which relies on checking whether activity
        // that is initializing the session is being launched from stack or anew
        activitiesOnStack_.add(activity.toString());
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        BranchLogger.v("onActivityPaused, activity = " + activity);
        Branch branch = Branch.getInstance();
        if (branch == null) return;

        /* Close any opened sharing dialog.*/
        if (branch.getShareLinkManager() != null) {
            branch.getShareLinkManager().cancelShareLinkDialog(true);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        BranchLogger.v("onActivityStopped, activity = " + activity);
        Branch branch = Branch.getInstance();
        if (branch == null) return;

        activityCnt_--; // Check if this is the last activity. If so, stop the session.
        if (activityCnt_ < 1) {
            branch.closeSessionInternal();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        BranchLogger.v("onActivityDestroyed, activity = " + activity);
        Branch branch = Branch.getInstance();
        if (branch == null) return;

        if (branch.getCurrentActivity() == activity) {
            branch.currentActivityReference_.clear();
        }

        activitiesOnStack_.remove(activity.toString());
    }
}