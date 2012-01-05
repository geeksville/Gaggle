/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geeksville.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.geeksville.billing.BillingService.RequestPurchase;
import com.geeksville.billing.BillingService.RestoreTransactions;
import com.geeksville.billing.Consts.PurchaseState;
import com.geeksville.billing.Consts.ResponseCode;
import com.geeksville.gaggle.R;

/**
 * A sample application that demonstrates in-app billing.
 */
public class Donate {
  private static final String TAG = "Donate";

  /**
   * Used for storing the log text.
   */
  private static final String LOG_TEXT_KEY = "DUNGEONS_LOG_TEXT";

  /**
   * The SharedPreferences key for recording whether we initialized the
   * database. If false, then we perform a RestoreTransactions request to get
   * all the purchases for this user.
   */
  private static final String DB_INITIALIZED = "db_initialized";

  private DonatePurchaseObserver mPurchaseObserver;
  private Handler mHandler;

  private BillingService mBillingService;
  private PurchaseDatabase mPurchaseDatabase;
  private Activity context;

  /**
   * The developer payload that is sent with subsequent purchase requests.
   */
  private String mPayloadContents = null;

  /**
   * Each product in the catalog is either MANAGED or UNMANAGED. MANAGED means
   * that the product can be purchased only once per user (such as a new level
   * in a game). The purchase is remembered by Android Market and can be
   * restored if this application is uninstalled and then re-installed.
   * UNMANAGED is used for products that can be used up and purchased multiple
   * times (such as poker chips). It is up to the application to keep track of
   * UNMANAGED products for the user.
   */
  private enum Managed {
    MANAGED, UNMANAGED
  }

  /**
   * A {@link PurchaseObserver} is used to get callbacks when Android Market
   * sends messages to this application so that we can update the UI.
   */
  private class DonatePurchaseObserver extends PurchaseObserver {
    public DonatePurchaseObserver(Handler handler) {
      super(context, handler);
    }

    @Override
    public void onBillingSupported(boolean supported) {
      if (Consts.DEBUG) {
        Log.i(TAG, "supported: " + supported);
      }
      if (supported) {
        restoreDatabase();
      }
    }

    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,
        String itemId, int quantity, long purchaseTime, String developerPayload) {
      if (Consts.DEBUG) {
        Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " "
            + purchaseState);
      }

      if (purchaseState == PurchaseState.PURCHASED) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean("has_donated", true).commit();
        thanksForDonating();
      }

      close(); // We don't need our service anymore
    }

    @Override
    public void onRequestPurchaseResponse(RequestPurchase request,
        ResponseCode responseCode) {
      if (Consts.DEBUG) {
        Log.d(TAG, request.mProductId + ": " + responseCode);
      }
      if (responseCode == ResponseCode.RESULT_OK) {
        if (Consts.DEBUG) {
          Log.i(TAG, "purchase was successfully sent to server");
        }
      } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
        if (Consts.DEBUG) {
          Log.i(TAG, "user canceled purchase");
        }
      } else {
        if (Consts.DEBUG) {
          Log.i(TAG, "purchase failed");
        }
      }
    }

    @Override
    public void onRestoreTransactionsResponse(RestoreTransactions request,
        ResponseCode responseCode) {
      if (responseCode == ResponseCode.RESULT_OK) {
        if (Consts.DEBUG) {
          Log.d(TAG, "completed RestoreTransactions request");
        }
        // Update the shared preferences so that we don't perform
        // a RestoreTransactions again.
        SharedPreferences prefs = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(DB_INITIALIZED, true);
        edit.commit();
      } else {
        if (Consts.DEBUG) {
          Log.d(TAG, "RestoreTransactions error: " + responseCode);
        }
      }
    }
  }

  private static class CatalogEntry {
    public String sku;
    public int nameId;
    public Managed managed;

    public CatalogEntry(String sku, int nameId, Managed managed) {
      this.sku = sku;
      this.nameId = nameId;
      this.managed = managed;
    }
  }

  /** An array of product list entries for the products that can be purchased. */
  private static final CatalogEntry[] CATALOG = new CatalogEntry[] { new CatalogEntry(
      "donate_basic", R.string.donation, Managed.MANAGED), };

  private String mItemName;
  private String mSku;

  private boolean isBillingSupported;

  private void promptToDonate() {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder
        .setMessage(R.string.donate_text)
        .setCancelable(false)
        .setPositiveButton(R.string.yes_i_d_like_to_donate,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                FlurryAgent.onEvent("DonateYes");
                String sku = "donate_basic";
                Log.d(TAG, "buying: " + sku);
                dialog.cancel();
                mBillingService.requestPurchase(sku, null);
                // We don't call close, because we want to leave the server
                // running a bit longer (to get the reply about this purchase)
              }
            })
        .setNegativeButton(R.string.no_not_right_now,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                FlurryAgent.onEvent("DonateNo");
                close();
                dialog.cancel();
              }
            });
    AlertDialog alert = builder.create();
    alert.show();
  }

  /**
   * Has the user donated?
   * 
   * @return
   */
  public static boolean isDonated(Context context) {

    // Even though this is an open source app, don't turn off this check and
    // release a version without
    // this donation reminder. The small amount of totally optional donations I
    // receive repays me for
    // the huge amount of time I have invested. -kevin

    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        "has_donated", false);
  }

  public static boolean canPromptToUpdate(Context context) {
    return context.getString(R.string.should_prompt_to_donate).equals("true");
  }

  /**
   * Is it time to show the nag screen?
   * 
   * @return
   */
  private boolean isPromptToUpdate() {

    // Even though this is an open source app, don't turn off this check and
    // release a version without
    // this donation reminder. The small amount of totally optional donations I
    // receive repays me for
    // the huge amount of time I have invested. -kevin

    if (!canPromptToUpdate(context))
      return false;

    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    long lastCheckDate = prefs.getLong("donate_check_date", -1);

    long now = System.currentTimeMillis();

    if (lastCheckDate == -1) { // Don't nag the first time we launch
      prefs.edit().putLong("donate_check_date", now).commit();
      return false;
    }

    long span = now - lastCheckDate;
    long checkInterval = 3 * 24 * 60 * 60 * 1000; // Only nag once every three
                                                  // days
    if (span < checkInterval)
      return false;

    prefs.edit().putLong("donate_check_date", now).commit();

    // Check to see if the market server says we already have donated
    Cursor c = mPurchaseDatabase.queryAllPurchasedItems();
    int numPurchased = c.getCount();
    c.close();

    if (numPurchased > 0) {
      PreferenceManager.getDefaultSharedPreferences(context).edit()
          .putBoolean("has_donated", true).commit();
      thanksForDonating();
      return false;
    }

    return true;
  }

  private void thanksForDonating() {
    // Even though this is an open source app, don't turn off this check and
    // release a version without
    // this donation reminder. The small amount of totally optional donations I
    // receive repays me for
    // the huge amount of time I have invested. -kevin

    Toast.makeText(context, "Thank you for donating!", Toast.LENGTH_LONG)
        .show();
  }

  public void perhapsSplash() {
    // Even though this is an open source app, don't turn off this check and
    // release a version without
    // this donation reminder. The small amount of totally optional donations I
    // receive repays me for
    // the huge amount of time I have invested. -kevin

    // Keep stats on # of emails sent
    if (isDonated(context)) {
      FlurryAgent.onEvent("DonateStart");
      thanksForDonating();
      close();
    } else if (isPromptToUpdate())
      promptToDonate();
    else {
      FlurryAgent.onEvent("NonDonateStart");
      close(); // Not bothering the user this time
    }
  }

  public void splash() {
    if (isDonated(context)) {
      thanksForDonating();
      close();
    } else
      promptToDonate();
  }

  public Donate(Activity context) {
    this.context = context;

    mHandler = new Handler();
    try {
      mPurchaseObserver = new DonatePurchaseObserver(mHandler);

      // If we've already donate no need to start the service
      if (!isDonated(context)) {
        mBillingService = new BillingService();
        mBillingService.setContext(context);

        mPurchaseDatabase = new PurchaseDatabase(context);

        // Check if billing is supported.
        ResponseHandler.register(mPurchaseObserver);
        isBillingSupported = mBillingService.checkBillingSupported();
      }
    } catch (Throwable t) {
      // Android 1.5 throws a VerifyError loading the library
      Log.e(TAG, "Skipping donate due to " + t);
    }
  }

  /** Release critical resources */
  private void close() {

    ResponseHandler.unregister(mPurchaseObserver);
    if (mPurchaseDatabase != null)
      mPurchaseDatabase.close();
    if (mBillingService != null)
      mBillingService.unbind();
  }

  /**
   * If the database has not been initialized, we send a RESTORE_TRANSACTIONS
   * request to Android Market to get the list of purchased items for this user.
   * This happens if the application has just been installed or the user wiped
   * data. We do not want to do this on every startup, rather, we want to do
   * only when the database needs to be initialized.
   */
  private void restoreDatabase() {
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(context);
    boolean initialized = prefs.getBoolean(DB_INITIALIZED, false);
    if (!initialized) {
      mBillingService.restoreTransactions();
      // Toast.makeText(context, R.string.restoring_transactions,
      // Toast.LENGTH_LONG).show();
    }
  }

}
