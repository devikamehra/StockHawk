package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the CursorRecyclerViewApater.java code and idea.
 */
public abstract class CursorRecyclerViewAdapter <VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

  private Cursor mCursor;
  private boolean dataIsValid;
  private int rowIdColumn;
  private DataSetObserver mDataSetObserver;
  Context context;
  public static final String COLUMN_ID = "_id";

  public CursorRecyclerViewAdapter(Context context, Cursor cursor){
    this.context = context;
    mCursor = cursor;
    dataIsValid = cursor != null;
    rowIdColumn = dataIsValid ? mCursor.getColumnIndex(COLUMN_ID) : -1;
    mDataSetObserver = new NotifyingDataSetObserver();
    if (dataIsValid){
      mCursor.registerDataSetObserver(mDataSetObserver);
    }
  }

  public Cursor getCursor(){
    return mCursor;
  }

  @Override
  public int getItemCount(){
    if (dataIsValid && mCursor != null){
      return mCursor.getCount();
    }
    return 0;
  }

  @Override public long getItemId(int position) {
    if (dataIsValid && mCursor != null && mCursor.moveToPosition(position)){
      return mCursor.getLong(rowIdColumn);
    }
    return 0;
  }

  @Override public void setHasStableIds(boolean hasStableIds) {
    super.setHasStableIds(true);
  }

  public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

  @Override
  public void onBindViewHolder(VH viewHolder, int position) {
    if (!dataIsValid){
      throw new IllegalStateException(context.getString(R.string.invalid_cursor_error));
    }
    if (!mCursor.moveToPosition(position)){
      throw new IllegalStateException(context.getString(R.string.move_cursor_error) + position);
    }

    onBindViewHolder(viewHolder, mCursor);
  }

  public Cursor swapCursor(Cursor newCursor){
    if (newCursor == mCursor){
      return null;
    }
    final Cursor oldCursor = mCursor;
    if (oldCursor != null && mDataSetObserver != null){
      oldCursor.unregisterDataSetObserver(mDataSetObserver);
    }
    mCursor = newCursor;
    if (mCursor != null){
      if (mDataSetObserver != null){
        mCursor.registerDataSetObserver(mDataSetObserver);
      }
      rowIdColumn = newCursor.getColumnIndexOrThrow(COLUMN_ID);
      dataIsValid = true;
      notifyDataSetChanged();
    }else{
      rowIdColumn = -1;
      dataIsValid = false;
      notifyDataSetChanged();
    }
    return oldCursor;
  }

  private class NotifyingDataSetObserver extends DataSetObserver{
    @Override public void onChanged() {
      super.onChanged();
      dataIsValid = true;
      notifyDataSetChanged();
    }

    @Override public void onInvalidated() {
      super.onInvalidated();
      dataIsValid = false;
      notifyDataSetChanged();
    }
  }
}
