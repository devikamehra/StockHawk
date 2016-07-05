package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoricData;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static final String QUERY_PARAMETER = "query";
  public static final String COUNT_PARAMETER = "count";
  public static final String RESULTS_PARAMETER = "results";
  public static final String QUOTE_PARAMETER = "quote";

  public static final String CHANGE_PARAMETER = "Change";
  public static final String SYMBOL_PARAMETER = "symbol";
  public static final String BID_PRICE_PARAMETER = "Bid";
  public static final String CHANGE_IN_PERCENT_PARAMETER = "ChangeinPercent";

  public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(String JSON, boolean isHistoric){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(QUERY_PARAMETER);
        int count = Integer.parseInt(jsonObject.getString(COUNT_PARAMETER));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject(RESULTS_PARAMETER)
              .getJSONObject(QUOTE_PARAMETER);
          if (isHistoric) {
            batchOperations.add(buildBatchOperationHistoric(jsonObject));
          }else{
            batchOperations.add(buildBatchOperation(jsonObject));
          }
        } else{
          resultsArray = jsonObject.getJSONObject(RESULTS_PARAMETER).getJSONArray(QUOTE_PARAMETER);

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              if (isHistoric) {
                batchOperations.add(buildBatchOperationHistoric(jsonObject));
              }else{
                batchOperations.add(buildBatchOperation(jsonObject));
              }
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, e.toString());
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
      bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
      return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuilder changeBuffer = new StringBuilder(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString(CHANGE_PARAMETER);
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(SYMBOL_PARAMETER));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(BID_PRICE_PARAMETER)));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString(CHANGE_IN_PERCENT_PARAMETER), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static ContentProviderOperation buildBatchOperationHistoric(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.QuotesHistoric.CONTENT_URI);

    try{
      builder.withValue(QuoteHistoricData.SYMBOL, jsonObject.getString("Symbol"));
      builder.withValue(QuoteHistoricData.DATE, jsonObject.getString("Date"));
      builder.withValue(QuoteHistoricData.OPEN, jsonObject.getString("Open"));
      builder.withValue(QuoteHistoricData.HIGH, jsonObject.getString("High"));
      builder.withValue(QuoteHistoricData.LOW, jsonObject.getString("Low"));
      builder.withValue(QuoteHistoricData.CLOSE, jsonObject.getString("Close"));

    }catch (JSONException e){
      e.printStackTrace();
    }

    return builder.build();
  }
}
