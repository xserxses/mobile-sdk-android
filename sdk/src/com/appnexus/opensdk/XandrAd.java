/*
 *    Copyright 2022 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdk;

import android.content.Context;

import com.appnexus.opensdk.utils.Clog;
import com.appnexus.opensdk.utils.HTTPGet;
import com.appnexus.opensdk.utils.HTTPResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the responsibility of caching the memberId and memberId list,
 * based on which the Impression type will be decided.
 * */
public class XandrAd {

    /**
    * memberId will be set using the {@link XandrAd#init(int, Context, boolean, InitListener)}
    **/
    private static int memberId = -1;
    /**
     * Cached member IDs list
     * */
    private static List<Integer> cachedViewableImpressionMemberIds = new ArrayList<>();
    /**
     * Viewable Impression Config URL
     * */
    private static final String viewableImpressionConfigUrl = "https://acdn.adnxs.com/mobile/viewableimpression/member_list_array.json";
    private static boolean isSdkInitialised;
    private static boolean areMemberIdsCached;

    /**
     * Initialize Xandr Ads SDK
     * @param memberId for initialising the XandrAd,
     * @param context for pre-caching the content.
     * @param preCacheContent enable / disable pre-caching of the content.provides flexibility to pre-cache content, such as fetch userAgent, fetch AAID and activate OMID. Pre-caching will make the future ad requests faster.
     * @param initListener for listening to the completion event.
     * */
    public static void init(int memberId, Context context, boolean preCacheContent, final InitListener initListener) {
        isSdkInitialised = !preCacheContent || context == null;
        areMemberIdsCached = cachedViewableImpressionMemberIds.size() > 0;
        if (!isSdkInitialised) {
            SDKSettings.init(context, new InitListener() {
                @Override
                public void onInitFinished(boolean success) {
                    isSdkInitialised = true;
                    if (initListener != null && areMemberIdsCached) {
                        initListener.onInitFinished(success);
                    }
                }
            });
        }
        XandrAd.memberId = memberId;
        if (!areMemberIdsCached) {
            if (context != null && !SharedNetworkManager.getInstance(context).isConnected(context)) {
                if (initListener != null) {
                    initListener.onInitFinished(false);
                    return;
                }
            }
            HTTPGet fetchJson = new HTTPGet() {
                @Override
                protected void onPostExecute(HTTPResponse response) {
                    if (response != null && response.getResponseBody() != null) {
                        try {
                            JSONArray memberIdArray = new JSONArray(response.getResponseBody());
                            if (memberIdArray.length() > 0) {
                                cachedViewableImpressionMemberIds.clear();
                                for (int idx = 0; idx < memberIdArray.length(); idx++) {
                                    cachedViewableImpressionMemberIds.add(Integer.valueOf(memberIdArray.getString(idx)));
                                }
                            }
                        } catch (JSONException e) {
                            Clog.e(Clog.baseLogTag, Clog.getString(R.string.fetch_viewable_impression_member_id_error));
                        }
                    }

                    areMemberIdsCached = true;
                    if (initListener != null && isSdkInitialised) {
                        initListener.onInitFinished(true);
                    }
                }

                @Override
                protected String getUrl() {
                    return viewableImpressionConfigUrl;
                }
            };
            fetchJson.execute();
        }
    }

    /**
     * API to check if the given memberId is contained in the cached ids
     * @return true / false based on the check, if the memberId is contained in the cached ids list
     * */
    public static boolean doesContainMemberId(int memberId) {
        return cachedViewableImpressionMemberIds.contains(Integer.valueOf(memberId));
    }

    /**
     * API to check if the XandrAd is already initialised or not
     * @return true / false based on the check, if the XandrAd is initialised or not
     * */
    public static boolean isInitialised() {
        return memberId != -1;
    }

    /**
     * API to check if the give buyer ID is eligible for Viewable Impression or not
     * @return true / false based on, if the buyerMemberId is same as the set memberId
     *                      OR the buyerMemberId is contained within the cached list of member IDs
     * */
    public static boolean isEligibleForViewableImpression(int buyerMemberId) {
        return doesContainMemberId(buyerMemberId) || buyerMemberId == memberId;
    }

    /**
     * API to reset the set memberId and clear the cached member IDs list.
     * */
    public static void reset() {
        memberId = -1;
        cachedViewableImpressionMemberIds.clear();
    }

    public static void throwUninitialisedException() {
        //Todo: Add a reference to the doc link in the Exception
        throw new IllegalStateException("Xandr SDK must be initialised before making an Ad Request.");
    }
}
