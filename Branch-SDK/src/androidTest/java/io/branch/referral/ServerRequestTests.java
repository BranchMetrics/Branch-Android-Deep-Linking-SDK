package io.branch.referral;

import android.os.Handler;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.mock.MockActivity;
import io.branch.referral.util.BranchCPID;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

@RunWith(AndroidJUnit4.class)
public class ServerRequestTests extends BranchTest{

    @Before
    public void setUp() {
        super.setUp();
        initBranchInstance();
    }
    @After
    public void tearDown() throws InterruptedException {
        branch.setNetworkTimeout(PrefHelper.TIMEOUT);
        super.tearDown();
    }

    @Test
    public void testTimedOutInitSessionCallbackInvoked() throws InterruptedException {
        branch.setNetworkTimeout(10);// forces timeouts
        initSessionResumeActivity();
        activityScenario.onActivity(new ActivityScenario.ActivityAction<MockActivity>() {
            @Override
            public void perform(final MockActivity activity) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Assert.assertTrue(activity.initSessionCallbackInvoked);
                    }
                }, 200);
            }
        });
    }

    @Test
    public void testTimedOutCrossPlatformIdsCallbackInvoked() throws InterruptedException {
        initSessionResumeActivity();
        branch.setNetworkTimeout(10);// forces timeouts

        final CountDownLatch lock = new CountDownLatch(1);
        Branch.getInstance().getCrossPlatformIds(new ServerRequestGetCPID.BranchCrossPlatformIdListener() {
            @Override
            public void onDataFetched(BranchCPID branchCPID, BranchError error) {
                lock.countDown();
                Assert.assertEquals(BranchError.ERR_BRANCH_REQ_TIMED_OUT, error.getErrorCode());
            }
        });
        Assert.assertTrue(lock.getCount() != 0 || lock.await(TEST_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testTimedOutLastAttributedTouchDataCallbackInvoked() throws InterruptedException {
        initSessionResumeActivity();
        branch.setNetworkTimeout(10);// forces timeouts

        final CountDownLatch lock1 = new CountDownLatch(1);
        Branch.getInstance().getLastAttributedTouchData(new ServerRequestGetLATD.BranchLastAttributedTouchDataListener() {
            @Override
            public void onDataFetched(JSONObject jsonObject, BranchError error) {
                Assert.assertEquals(BranchError.ERR_BRANCH_REQ_TIMED_OUT, error.getErrorCode());
                lock1.countDown();
            }
        });

        Assert.assertTrue(lock1.getCount() != 0 || lock1.await(TEST_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testTimedOutCreditHistoryCallbackInvoked() throws InterruptedException {
        initSessionResumeActivity();
        branch.setNetworkTimeout(10);// forces timeouts

        final CountDownLatch lock2 = new CountDownLatch(1);
        Branch.getInstance().getCreditHistory(new Branch.BranchListResponseListener() {
            @Override
            public void onReceivingResponse(JSONArray list, BranchError error) {
                Assert.assertEquals(BranchError.ERR_BRANCH_REQ_TIMED_OUT, error.getErrorCode());
                lock2.countDown();
            }
        });
        Assert.assertTrue(lock2.getCount() != 0 || lock2.await(TEST_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testTimedOutGenerateShortUrlCallbackInvoked() throws InterruptedException {
        initSessionResumeActivity();
        branch.setNetworkTimeout(10);// forces timeouts

        final CountDownLatch lock3 = new CountDownLatch(1);
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle("My Content Title")
                .setContentDescription("My Content Description")
                .setContentImageUrl("https://lorempixel.com/400/400")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("key1", "value1"));
        LinkProperties linkProperties = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")
                .addControlParameter("$web_only", "true")
                .addControlParameter("$desktop_url", "http://example.com/home")
                .addControlParameter("custom", "data")
                .addControlParameter("custom_random", java.lang.Long.toString(Calendar.getInstance().getTimeInMillis()));
        buo.generateShortUrl(getTestContext(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                Assert.assertEquals(BranchError.ERR_BRANCH_REQ_TIMED_OUT, error.getErrorCode());
                lock3.countDown();
            }
        });
        Assert.assertTrue(lock3.getCount() != 0 || lock3.await(TEST_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS));
    }
}
